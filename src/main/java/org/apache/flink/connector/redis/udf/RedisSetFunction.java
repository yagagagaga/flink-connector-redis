package org.apache.flink.connector.redis.udf;

import org.apache.flink.connector.redis.options.RedisOptions;

/**
 * RedisSetFunction.
 */
public class RedisSetFunction extends RedisScalarFunction {

	public RedisSetFunction(RedisOptions options) {
		super(options);
	}

	public java.lang.String eval(java.lang.String var0, java.lang.String var1) {
		return client.set(var0, var1);
	}

	public java.lang.String eval(byte[] var0, byte[] var1) {
		return client.set(var0, var1);
	}

}
