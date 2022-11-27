package org.apache.flink.connector.redis.example.mode;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import static org.apache.flink.connector.redis.table.RedisTableFactory.IDENTIFIER;

/**
 * A example for Redis single mode using flink-sql.
 */
public class RedisSingleExample {
	public static void main(String[] args) {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(new Configuration());
		env.setParallelism(1);
		StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

		String host = "localhost";
		String port = "6379";

		tableEnv.executeSql(""
				+ "CREATE TABLE t_redis_input (\n"
				+ "` key`   STRING,\n"
				+ "` value` STRING\n"
				+ ") WITH (\n"
				+ "  'connector' = '" + IDENTIFIER + "',\n"
				+ "  'redis.deploy-mode' = 'single',\n"
				+ "  'redis.host' = '" + host + "',\n"
				+ "  'redis.port' = '" + port + "',\n"
				+ "  'redis.data-type' = 'string',\n"
				+ "  'redis.key-pattern' = 'string_*'\n"
				+ ")");

		tableEnv.executeSql("SELECT * FROM t_redis_input").print();
	}
}
