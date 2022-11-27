package org.apache.flink.connector.redis.example.udf;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.redis.options.RedisOptions;
import org.apache.flink.connector.redis.udf.FlinkRedisUdf;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.util.Properties;

import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_HOST;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_PORT;

/**
 * RedisUdfExample.
 */
public class RedisUdfExample {
	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(new Configuration());
		StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

		String host = "192.168.1.140";
		String port = "6380";

		final Properties properties = new Properties();
		properties.put(REDIS_HOST.key(), host);
		properties.put(REDIS_PORT.key(), port);
		final RedisOptions options = new RedisOptions(properties);

		FlinkRedisUdf.registerTo(tableEnv, options);

		tableEnv.executeSql("select redis_get('{string}_541')").print();
	}
}
