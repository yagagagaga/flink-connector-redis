package org.apache.flink.connector.redis.options;

import org.apache.flink.calcite.shaded.com.google.common.collect.Maps;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.connector.redis.client.JedisSentinelPools;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.Hashing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Option utils for JedisClientOptions table source sink.
 */
public final class JedisClientOptions implements Serializable {

	public static final ConfigOption<String> REDIS_HOST = ConfigOptions
			.key("redis.host")
			.stringType()
			.noDefaultValue()
			.withDescription("Required Redis host.");
	public static final ConfigOption<Integer> REDIS_PORT = ConfigOptions
			.key("redis.port")
			.intType()
			.noDefaultValue()
			.withDescription("Required Redis port.");
	public static final ConfigOption<String> REDIS_HOST_AND_PORTS = ConfigOptions
			.key("redis.host-and-ports")
			.stringType()
			.noDefaultValue()
			.withDescription("Required Redis hosts.");
	public static final ConfigOption<String> REDIS_MASTER = ConfigOptions
			.key("redis.master")
			.stringType()
			.noDefaultValue()
			.withDescription("Required Redis masters.");
	public static final ConfigOption<String> REDIS_MASTERS = ConfigOptions
			.key("redis.masters")
			.stringType()
			.noDefaultValue()
			.withDescription("Required Redis masters.");
	public static final ConfigOption<String> REDIS_SENTINELS = ConfigOptions
			.key("redis.sentinels")
			.stringType()
			.noDefaultValue()
			.withDescription("Required Redis sentinel connection string.");
	public static final ConfigOption<String> REDIS_USER = ConfigOptions
			.key("redis.user")
			.stringType()
			.noDefaultValue()
			.withDescription("Required Redis user string.");
	public static final ConfigOption<String> REDIS_PASSWORD = ConfigOptions
			.key("redis.password")
			.stringType()
			.noDefaultValue()
			.withDescription("Required Redis password string.");
	public static final ConfigOption<Duration> REDIS_CLIENT_TIMEOUT = ConfigOptions
			.key("redis.client.timeout")
			.durationType()
			.defaultValue(Duration.ofSeconds(30))
			.withDescription("Required Redis client timeout duration.");
	public static final ConfigOption<Long> MAX_WAIT_MILLIS = ConfigOptions
			.key("redis.pool.maxWaitMillis")
			.longType()
			.defaultValue(10000L)
			.withDescription("Required Redis sentinel pool max wait millis.");
	public static final ConfigOption<Boolean> TEST_WHILE_IDLE = ConfigOptions
			.key("redis.pool.testWhileIdle")
			.booleanType()
			.defaultValue(false)
			.withDescription("Required Redis connection pool test while idle.");
	public static final ConfigOption<Long> TIME_BETWEEN_EVICTION_RUNS_MILLIS = ConfigOptions
			.key("redis.pool.timeBetweenEvictionRunsMillis")
			.longType()
			.defaultValue(30000L)
			.withDescription("Required Redis connection pool time between eviction runs millis.");
	public static final ConfigOption<Integer> NUM_TESTS_PER_EVICTION_RUN = ConfigOptions
			.key("redis.pool.numTestsPerEvictionRun")
			.intType()
			.defaultValue(-1)
			.withDescription("Required Redis connection pool num tests per eviction run.");
	public static final ConfigOption<Long> MIN_EVICTABLE_IDLE_TIME_MILLIS = ConfigOptions
			.key("redis.pool.minEvictableIdleTimeMillis")
			.longType()
			.defaultValue(60000L)
			.withDescription("Required Redis connection pool min evictable idle time millis.");
	public static final ConfigOption<Integer> MAX_TOTAL = ConfigOptions
			.key("redis.pool.maxTotal")
			.intType()
			.defaultValue(2)
			.withDescription("Required Redis connection pool max total.");
	public static final ConfigOption<Integer> MAX_IDLE = ConfigOptions
			.key("redis.pool.maxIdle")
			.intType()
			.defaultValue(1)
			.withDescription("Required Redis connection pool max idle.");
	public static final ConfigOption<Integer> MIN_IDLE = ConfigOptions
			.key("redis.pool.minIdle")
			.intType()
			.defaultValue(1)
			.withDescription("Required Redis connection pool min idle.");
	public static final ConfigOption<Boolean> TEST_ON_BORROW = ConfigOptions
			.key("redis.pool.testOnBorrow")
			.booleanType()
			.defaultValue(true)
			.withDescription("Required Redis connection pool test on borrow.");
	public static final ConfigOption<Boolean> TEST_ON_RETURN = ConfigOptions
			.key("redis.pool.testOnReturn")
			.booleanType()
			.defaultValue(true)
			.withDescription("Required Redis connection pool test on return.");

	private static final long serialVersionUID = 1L;

	@Nullable
	private final String host;
	@Nullable
	private final Integer port;
	private final Set<String> hosts;
	@Nullable
	private final String masterName;
	private final List<String> masterNames;
	@Nonnull
	private final Set<String> sentinels;
	private final int connectionTimeout;
	private final int soTimeout;
	private final int infiniteSoTimeout;
	private final int maxAttempts;
	@Nullable
	private final String user;
	@Nullable
	private final String password;
	private final int database;
	@Nullable
	private final String clientName;
	private final int sentinelConnectionTimeout;
	private final int sentinelSoTimeout;
	@Nullable
	private final String sentinelUser;
	@Nullable
	private final String sentinelPassword;
	@Nullable
	private final String sentinelClientName;

	// for connect pool
	private final int maxTotal;
	private final int maxIdle;
	private final int minIdle;
	private final long maxWaitMillis;
	private final boolean testOnBorrow;
	private final boolean testOnReturn;
	private final boolean testWhileIdle;
	private final long minEvictableIdleTimeMillis;
	private final long timeBetweenEvictionRunsMillis;
	private final int numTestsPerEvictionRun;

	public JedisClientOptions(Properties properties) {
		this(Configuration.fromMap(Maps.fromProperties(properties)));
	}

	public JedisClientOptions(ReadableConfig tableOptions) {
		this.host = tableOptions.getOptional(REDIS_HOST).orElse(null);
		this.port = tableOptions.getOptional(REDIS_PORT).orElse(null);
		this.hosts = Arrays.stream(tableOptions.getOptional(REDIS_HOST_AND_PORTS).orElse("").split(",")).collect(Collectors.toSet());
		this.masterName = tableOptions.getOptional(REDIS_MASTER).orElse(null);
		this.masterNames = Arrays.asList(tableOptions.getOptional(REDIS_MASTERS).orElse("").split(","));
		this.sentinels = Arrays.stream(tableOptions.getOptional(REDIS_SENTINELS).orElse("").split(",")).collect(Collectors.toSet());
		this.connectionTimeout = (int) tableOptions.get(REDIS_CLIENT_TIMEOUT).toMillis();
		this.soTimeout = connectionTimeout;
		this.infiniteSoTimeout = 0;
		this.maxAttempts = 5;
		this.user = tableOptions.getOptional(REDIS_USER).orElse(null);
		this.password = tableOptions.getOptional(REDIS_PASSWORD).orElse(null);
		this.database = Protocol.DEFAULT_DATABASE;
		this.clientName = null;
		this.sentinelConnectionTimeout = Protocol.DEFAULT_TIMEOUT;
		this.sentinelSoTimeout = Protocol.DEFAULT_TIMEOUT;
		this.sentinelUser = null;
		this.sentinelPassword = null;
		this.sentinelClientName = null;

		// for connect pool
		this.maxTotal = tableOptions.get(MAX_TOTAL);
		this.maxIdle = tableOptions.get(MAX_IDLE);
		this.minIdle = tableOptions.get(MIN_IDLE);
		this.maxWaitMillis = tableOptions.get(MAX_WAIT_MILLIS);
		this.testOnBorrow = tableOptions.get(TEST_ON_BORROW);
		this.testOnReturn = tableOptions.get(TEST_ON_RETURN);
		this.testWhileIdle = tableOptions.get(TEST_WHILE_IDLE);
		this.minEvictableIdleTimeMillis = tableOptions.get(MIN_EVICTABLE_IDLE_TIME_MILLIS);
		this.timeBetweenEvictionRunsMillis = tableOptions.get(TIME_BETWEEN_EVICTION_RUNS_MILLIS);
		this.numTestsPerEvictionRun = tableOptions.get(NUM_TESTS_PER_EVICTION_RUN);
	}

	public JedisPool createSingleJedisPool() {
		if (host == null) {
			throw new IllegalArgumentException(String.format("You must set %s if you want connect redis.", REDIS_HOST.key()));
		}
		if (port == null) {
			throw new IllegalArgumentException(String.format("You must set %s if you want connect redis.", REDIS_PORT.key()));
		}

		JedisPoolConfig config = new JedisPoolConfig();
		config.setBlockWhenExhausted(true);
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMinIdle(minIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnReturn(testOnReturn);
		config.setTestWhileIdle(testWhileIdle);
		config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);

		return new JedisPool(config, host, port, connectionTimeout, user, password, database, clientName);
	}

	public JedisSentinelPool createSentinelJedisPool() {
		if (masterName == null) {
			throw new IllegalArgumentException(""
					+ "You must set " + REDIS_HOST.key()
					+ " if you want connect sentinel-redis.");
		}

		JedisPoolConfig config = new JedisPoolConfig();
		config.setBlockWhenExhausted(true);
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMinIdle(minIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnReturn(testOnReturn);
		config.setTestWhileIdle(testWhileIdle);
		config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);

		return new JedisSentinelPool(masterName, sentinels, config, connectionTimeout, soTimeout, user, password,
				database, clientName, sentinelConnectionTimeout, sentinelSoTimeout, sentinelUser, sentinelPassword,
				sentinelClientName);
	}

	public JedisCluster createJedisCluster() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setBlockWhenExhausted(true);
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMinIdle(minIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnReturn(testOnReturn);
		config.setTestWhileIdle(testWhileIdle);
		config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		final Set<HostAndPort> hostAndPorts = hosts.stream().map(HostAndPort::parseString).collect(Collectors.toSet());

		return new JedisCluster(hostAndPorts, connectionTimeout, soTimeout, infiniteSoTimeout, maxAttempts, user,
				password, clientName, config);
	}

	@Nonnull
	public JedisSentinelPools createJedisSentinelPools() {
		return Objects.requireNonNull(createJedisSentinelPools(0, 1));
	}

	@Nullable
	public JedisSentinelPools createJedisSentinelPools(int subtaskIdx, int parallelism) {
		if (parallelism < 1 || subtaskIdx < 0) {
			throw new IllegalArgumentException("subtaskIdx must greater than 0, parallelism must greater than 1.");
		}
		final List<String> targetMasters = new ArrayList<>();
		for (String mn : masterNames) {
			if (Math.abs(Hashing.MURMUR_HASH.hash(mn)) % parallelism == subtaskIdx) {
				targetMasters.add(mn);
			}
		}

		if (targetMasters.isEmpty()) {
			return null;
		}

		JedisPoolConfig config = new JedisPoolConfig();
		config.setBlockWhenExhausted(true);
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMinIdle(minIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnReturn(testOnReturn);
		config.setTestWhileIdle(testWhileIdle);
		config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		return new JedisSentinelPools(targetMasters, sentinels, config, connectionTimeout, soTimeout, infiniteSoTimeout,
				user, password, database, clientName, sentinelConnectionTimeout, sentinelSoTimeout, sentinelUser,
				sentinelPassword, sentinelClientName);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JedisClientOptions that = (JedisClientOptions) o;
		return connectionTimeout == that.connectionTimeout &&
				soTimeout == that.soTimeout &&
				infiniteSoTimeout == that.infiniteSoTimeout &&
				database == that.database &&
				sentinelConnectionTimeout == that.sentinelConnectionTimeout &&
				sentinelSoTimeout == that.sentinelSoTimeout &&
				maxTotal == that.maxTotal &&
				maxIdle == that.maxIdle &&
				minIdle == that.minIdle &&
				maxWaitMillis == that.maxWaitMillis &&
				testOnBorrow == that.testOnBorrow &&
				testOnReturn == that.testOnReturn &&
				testWhileIdle == that.testWhileIdle &&
				minEvictableIdleTimeMillis == that.minEvictableIdleTimeMillis &&
				timeBetweenEvictionRunsMillis == that.timeBetweenEvictionRunsMillis &&
				numTestsPerEvictionRun == that.numTestsPerEvictionRun &&
				masterNames.equals(that.masterNames) &&
				sentinels.equals(that.sentinels) &&
				Objects.equals(user, that.user) &&
				Objects.equals(password, that.password) &&
				Objects.equals(clientName, that.clientName) &&
				Objects.equals(sentinelUser, that.sentinelUser) &&
				Objects.equals(sentinelPassword, that.sentinelPassword) &&
				Objects.equals(sentinelClientName, that.sentinelClientName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(masterNames, sentinels, connectionTimeout, soTimeout, infiniteSoTimeout, user, password,
				database, clientName, sentinelConnectionTimeout, sentinelSoTimeout, sentinelUser, sentinelPassword,
				sentinelClientName, maxTotal, maxIdle, minIdle, maxWaitMillis, testOnBorrow, testOnReturn,
				testWhileIdle, minEvictableIdleTimeMillis, timeBetweenEvictionRunsMillis, numTestsPerEvictionRun);
	}
}
