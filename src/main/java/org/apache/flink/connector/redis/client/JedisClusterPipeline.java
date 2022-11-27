package org.apache.flink.connector.redis.client;

import org.apache.commons.io.IOUtils;
import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.Builder;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisClusterConnectionHandler;
import redis.clients.jedis.JedisClusterInfoCache;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisNoReachableClusterNodeException;
import redis.clients.jedis.util.JedisClusterCRC16;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline for Redis Cluster.
 */
public class JedisClusterPipeline extends Pipeline {

	/**
	 * Redis cluster cache information object.
	 */
	private final JedisClusterInfoCache clusterInfoCache;
	/**
	 * Redis链接处理对象 继承于JedisClusterConnectionHandler,对其提供友好的调用方法.
	 */
	private final JedisSlotBasedConnectionHandler connectionHandler;

	private final ConcurrentHashMap<JedisPool, Jedis> clients = new ConcurrentHashMap<>();
	private final Map<Client, Queue<Response<?>>> pipelinedResponses = new HashMap<>();
	private Client currentClient;

	/**
	 * 构造方法.
	 * 通过JedisCluster获取JedisClusterInfoCache和JedisSlotBasedConnectionHandler.
	 */
	@SuppressWarnings("java:S3011")
	public JedisClusterPipeline(JedisCluster jedisCluster) throws NoSuchFieldException, IllegalAccessException {

		final Field connectionHandlerField = BinaryJedisCluster.class.getDeclaredField("connectionHandler");
		connectionHandlerField.setAccessible(true);
		this.connectionHandler = (JedisSlotBasedConnectionHandler) connectionHandlerField.get(jedisCluster);
		final Field cacheField = JedisClusterConnectionHandler.class.getDeclaredField("cache");
		cacheField.setAccessible(true);
		this.clusterInfoCache = (JedisClusterInfoCache) cacheField.get(this.connectionHandler);
	}

	/**
	 * 获取JedisPool.
	 * 第一次获取不到尝试刷新缓存的SlotPool再获取一次.
	 */
	private JedisPool getJedisPool(String key) {
		/** 通过key计算出slot */
		int slot = JedisClusterCRC16.getSlot(key);
		/** 通过slot获取到对应的Jedis连接池 */
		JedisPool jedisPool = clusterInfoCache.getSlotPool(slot);
		if (null != jedisPool) {
			return jedisPool;
		} else {
			/** 刷新缓存的SlotPool */
			connectionHandler.renewSlotCache();
			jedisPool = clusterInfoCache.getSlotPool(slot);
			if (jedisPool != null) {
				return jedisPool;
			} else {
				throw new JedisNoReachableClusterNodeException("No reachable node in cluster for slot " + slot);
			}
		}
	}

	private JedisPool getJedisPool(byte[] key) {
		/** 通过key计算出slot */
		int slot = JedisClusterCRC16.getSlot(key);
		/** 通过slot获取到对应的Jedis连接池 */
		JedisPool jedisPool = clusterInfoCache.getSlotPool(slot);
		if (null != jedisPool) {
			return jedisPool;
		} else {
			/** 刷新缓存的SlotPool */
			connectionHandler.renewSlotCache();
			jedisPool = clusterInfoCache.getSlotPool(slot);
			if (jedisPool != null) {
				return jedisPool;
			} else {
				throw new JedisNoReachableClusterNodeException("No reachable node in cluster for slot " + slot);
			}
		}
	}

	@Override
	@SuppressWarnings("java:S1874")
	public synchronized void sync() {
		if (pipelinedResponses.isEmpty()) {
			return;
		}
		for (Map.Entry<Client, Queue<Response<?>>> entry : pipelinedResponses.entrySet()) {
			final Client client = entry.getKey();
			final Queue<Response<?>> responses = entry.getValue();
			final List<Object> many = client.getMany(responses.size());
			for (Object o : many) {
				responses.poll().set(o);
			}
		}
		clients.values().forEach(IOUtils::closeQuietly);
		pipelinedResponses.clear();
		clients.clear();
	}

	@Override
	@SuppressWarnings("java:S1874")
	public List<Object> syncAndReturnAll() {
		if (pipelinedResponses.isEmpty()) {
			return Collections.emptyList();
		}
		final LinkedList<Object> result = new LinkedList<>();
		for (Map.Entry<Client, Queue<Response<?>>> entry : pipelinedResponses.entrySet()) {
			final Client client = entry.getKey();
			final Queue<Response<?>> responses = entry.getValue();
			final List<Object> many = client.getMany(responses.size());
			for (Object o : many) {
				final Response<?> poll = responses.poll();
				poll.set(o);
				result.add(poll.get());
			}
		}
		clients.values().forEach(IOUtils::closeQuietly);
		pipelinedResponses.clear();
		clients.clear();
		return result;
	}

	@Override
	protected void clean() {
		pipelinedResponses.clear();
	}

	@Override
	protected Response<?> generateResponse(Object data) {
		throw new IllegalStateException("Deprecated method, don't call me");
	}

	@Override
	protected <T> Response<T> getResponse(Builder<T> builder) {
		Response<T> lr = new Response<>(builder);
		pipelinedResponses.computeIfAbsent(currentClient, k -> new LinkedList<>()).add(lr);
		return lr;
	}

	@Override
	protected boolean hasPipelinedResponse() {
		return !pipelinedResponses.isEmpty();
	}

	@Override
	protected int getPipelinedResponseLength() {
		throw new IllegalStateException("Deprecated method, don't call me");
	}

	@Override
	protected Client getClient(String key) {
		final JedisPool jedisPool = getJedisPool(key);
		if (!clients.containsKey(jedisPool)) {
			final Jedis resource = jedisPool.getResource();
			clients.put(jedisPool, resource);
			currentClient = resource.getClient();
		} else {
			currentClient = clients.get(jedisPool).getClient();
		}
		return currentClient;
	}

	@Override
	protected Client getClient(byte[] key) {
		final JedisPool jedisPool = getJedisPool(key);
		if (!clients.containsKey(jedisPool)) {
			final Jedis resource = jedisPool.getResource();
			clients.put(jedisPool, resource);
			currentClient = resource.getClient();
		} else {
			currentClient = clients.get(jedisPool).getClient();
		}
		return currentClient;
	}
}
