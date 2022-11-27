package org.apache.flink.connector.redis.udf;

import org.apache.flink.connector.redis.options.RedisOptions;

/**
 * RedisHgetFunction.
 */
public class RedisHgetFunction extends RedisScalarFunction {

	public RedisHgetFunction(RedisOptions options) {
		super(options);
	}

	public byte[] eval(byte[] var0, byte[] var1) {
		return client.hget(var0, var1);
	}

	public java.lang.String eval(java.lang.String var0, java.lang.String var1) {
		return client.hget(var0, var1);
	}

}
