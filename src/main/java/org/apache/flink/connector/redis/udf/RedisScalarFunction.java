package org.apache.flink.connector.redis.udf;

import org.apache.flink.connector.redis.client.RedisClientProxy;
import org.apache.flink.connector.redis.options.RedisOptions;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

/**
 * RedisScalarFunction.
 */
public abstract class RedisScalarFunction extends ScalarFunction {
	private final RedisOptions options;

	protected transient RedisClientProxy client;

	protected RedisScalarFunction(RedisOptions options) {
		this.options = options;
	}

	@Override
	public void open(FunctionContext context) throws Exception {
		super.open(context);
		client = options.createClient();
	}

	@Override
	public void close() throws Exception {
		super.close();
		client.close();
	}
}
