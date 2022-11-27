package org.apache.flink.connector.redis.sink;

import org.apache.flink.api.common.functions.Function;

import redis.clients.jedis.PipelineBase;

/**
 * RedisRecordConsumer.
 */
@FunctionalInterface
public interface RedisRecordConsumer<T> extends Function {
	void apply(T aRecord, PipelineBase client);

	/**
	 * KeySelector.
	 */
	@FunctionalInterface
	interface KeySelector<I, K> extends Function {
		K apply(I in);
	}

	/**
	 * ValueSelector.
	 */
	@FunctionalInterface
	interface ValueSelector<I, V> extends Function {
		V apply(I in);
	}
}
