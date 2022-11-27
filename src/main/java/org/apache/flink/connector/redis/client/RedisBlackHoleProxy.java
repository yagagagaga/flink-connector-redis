package org.apache.flink.connector.redis.client;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.PipelineBase;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * Redis command container if we want to connect to a Redis cluster.
 */
class RedisBlackHoleProxy implements RedisClientProxy {

	@Override
	public PipelineBase pipelined() {
		return null;
	}

	@Override
	public ScanResult<byte[]> scan(byte[] cursor, ScanParams params) {
		return null;
	}

	@Override
	public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
		// do nothing.
	}

	@Override
	public void close() {
		// do nothing.
	}
}
