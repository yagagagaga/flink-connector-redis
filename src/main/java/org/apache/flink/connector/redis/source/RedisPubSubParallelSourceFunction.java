package org.apache.flink.connector.redis.source;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.connector.redis.options.RedisOptions;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;

/**
 * RedisPubSubParallelSourceFunction for standard input stream.
 */
public class RedisPubSubParallelSourceFunction<T> extends RedisPubSubSourceFunction<T>
		implements ParallelSourceFunction<T> {
	public RedisPubSubParallelSourceFunction(RedisOptions redisOptions, DeserializationSchema<T> deserializationSchema) {
		super(redisOptions, deserializationSchema);
	}
}
