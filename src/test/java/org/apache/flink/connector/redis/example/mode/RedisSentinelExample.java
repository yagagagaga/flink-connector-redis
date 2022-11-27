package org.apache.flink.connector.redis.example.mode;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import static org.apache.flink.connector.redis.table.RedisTableFactory.IDENTIFIER;

/**
 * A example for Redis single mode using flink-sql.
 */
public class RedisSentinelExample {
	public static void main(String[] args) {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(new Configuration());
		StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

		String master = "mymaster";
		String sentinel = "localhost:26379";

		tableEnv.executeSql(""
				+ "CREATE TABLE t_redis_input (\n"
				+ "` key`   STRING,\n"
				+ "` value` STRING\n"
				+ ") WITH (\n"
				+ "  'connector' = '" + IDENTIFIER + "',\n"
				+ "  'redis.deploy-mode' = 'sentinel',\n"
				+ "  'redis.master' = '" + master + "',\n"
				+ "  'redis.sentinels' = '" + sentinel + "',\n"
				+ "  'redis.data-type' = 'string',\n"
				+ "  'redis.key-pattern' = 'string_*'\n"
				+ ")");

		tableEnv.executeSql("SELECT * FROM t_redis_input").print();
	}
}
