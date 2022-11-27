package org.apache.flink.connector.redis.options;

/**
 * Enum type for Redis deploy mode.
 */
public enum RedisDeployMode {
	CLUSTER,
	SENTINEL,
	SINGLE,
	SHARDED
}
