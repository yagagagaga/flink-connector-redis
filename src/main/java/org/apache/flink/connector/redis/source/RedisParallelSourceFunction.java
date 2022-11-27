package org.apache.flink.connector.redis.source;

import org.apache.flink.connector.redis.options.RedisOptions;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;

/**
 * RedisParallelSourceFunction for Redis client.
 */
public class RedisParallelSourceFunction<T> extends RedisSourceFunction<T> implements ParallelSourceFunction<T> {
	public RedisParallelSourceFunction(RedisRecordProducer<T> producer, RedisOptions redisOptions) {
		super(producer, redisOptions);
	}
}
