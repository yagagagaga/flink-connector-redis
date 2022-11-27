package org.apache.flink.connector.redis.udf;

import org.apache.flink.connector.redis.options.RedisOptions;

/**
 * RedisGetFunction.
 */
public class RedisGetFunction extends RedisScalarFunction {

	public RedisGetFunction(RedisOptions options) {
		super(options);
	}

	public java.lang.String eval(java.lang.String var0) {
		return client.get(var0);
	}

	public byte[] eval(byte[] var0) {
		return client.get(var0);
	}

}
