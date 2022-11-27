package org.apache.flink.connector.redis.udf;

import org.apache.flink.connector.redis.options.RedisOptions;

import java.util.Map;

/**
 * RedisHsetFunction.
 */
public class RedisHsetFunction extends RedisScalarFunction {

	public RedisHsetFunction(RedisOptions options) {
		super(options);
	}

	public java.lang.Long eval(byte[] var0, byte[] var1, byte[] var2) {
		return client.hset(var0, var1, var2);
	}

	public java.lang.Long eval(byte[] var0, Map<byte[], byte[]> var1) {
		return client.hset(var0, var1);
	}

	public java.lang.Long eval(String var0, java.lang.String var1, java.lang.String var2) {
		return client.hset(var0, var1, var2);
	}

	public java.lang.Long eval(String var0, Map<String, String> var1) {
		return client.hset(var0, var1);
	}

}
