package org.apache.flink.connector.redis.example.mode;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import static org.apache.flink.connector.redis.table.RedisTableFactory.IDENTIFIER;

/**
 * A example for Redis cluster mode using flink-sql.
 */
public class RedisClusterExample {
	public static void main(String[] args) {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
		env.disableOperatorChaining();
		StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

		String hostAndPorts = ""
				+ "localhost:6380,"
				+ "localhost:6381,"
				+ "localhost:6382,"
				+ "localhost:6383,"
				+ "localhost:6384,"
				+ "localhost:6385";

		tableEnv.executeSql(""
				+ "CREATE TABLE t_redis_input (\n"
				+ "` key`   STRING,\n"
				+ "` value` STRING\n"
				+ ") WITH (\n"
				+ "  'connector' = '" + IDENTIFIER + "',\n"
				+ "  'redis.deploy-mode' = 'cluster',\n"
				+ "  'redis.host-and-ports' = '" + hostAndPorts + "',\n"
				+ "  'redis.data-type' = 'string',\n"
				+ "  'redis.key-pattern' = '{string}_*'\n"
				+ ")");

		tableEnv.executeSql("SELECT * FROM t_redis_input").print();
	}
}
