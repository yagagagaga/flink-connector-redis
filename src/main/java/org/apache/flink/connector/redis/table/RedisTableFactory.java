package org.apache.flink.connector.redis.table;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.connector.redis.options.RedisDataType;
import org.apache.flink.connector.redis.options.RedisDeployMode;
import org.apache.flink.connector.redis.options.RedisOptions;
import org.apache.flink.connector.redis.sink.RedisTableSink;
import org.apache.flink.connector.redis.source.RedisTableSource;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.format.EncodingFormat;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.DeserializationFormatFactory;
import org.apache.flink.table.factories.DynamicTableSinkFactory;
import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.factories.SerializationFormatFactory;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.ArrayType;
import org.apache.flink.table.types.logical.BinaryType;
import org.apache.flink.table.types.logical.DoubleType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.MapType;
import org.apache.flink.table.types.logical.VarBinaryType;
import org.apache.flink.table.types.logical.VarCharType;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

import static org.apache.flink.connector.redis.options.JedisClientOptions.MAX_IDLE;
import static org.apache.flink.connector.redis.options.JedisClientOptions.MAX_TOTAL;
import static org.apache.flink.connector.redis.options.JedisClientOptions.MAX_WAIT_MILLIS;
import static org.apache.flink.connector.redis.options.JedisClientOptions.MIN_EVICTABLE_IDLE_TIME_MILLIS;
import static org.apache.flink.connector.redis.options.JedisClientOptions.MIN_IDLE;
import static org.apache.flink.connector.redis.options.JedisClientOptions.NUM_TESTS_PER_EVICTION_RUN;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_CLIENT_TIMEOUT;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_HOST;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_HOST_AND_PORTS;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_MASTER;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_MASTERS;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_PASSWORD;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_PORT;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_SENTINELS;
import static org.apache.flink.connector.redis.options.JedisClientOptions.REDIS_USER;
import static org.apache.flink.connector.redis.options.JedisClientOptions.TEST_ON_BORROW;
import static org.apache.flink.connector.redis.options.JedisClientOptions.TEST_ON_RETURN;
import static org.apache.flink.connector.redis.options.JedisClientOptions.TEST_WHILE_IDLE;
import static org.apache.flink.connector.redis.options.JedisClientOptions.TIME_BETWEEN_EVICTION_RUNS_MILLIS;
import static org.apache.flink.connector.redis.options.JedisLookupOptions.REDIS_LOOKUP_ASYNC;
import static org.apache.flink.connector.redis.options.JedisLookupOptions.REDIS_LOOKUP_CACHE_MAX_ROWS;
import static org.apache.flink.connector.redis.options.JedisLookupOptions.REDIS_LOOKUP_CACHE_TTL;
import static org.apache.flink.connector.redis.options.JedisOpOptions.REDIS_DATA_TYPE;
import static org.apache.flink.connector.redis.options.JedisOpOptions.REDIS_IGNORE_ERROR;
import static org.apache.flink.connector.redis.options.JedisOpOptions.REDIS_KEY_PATTERN;
import static org.apache.flink.connector.redis.options.JedisOpOptions.REDIS_PUBSUB_PUBLISH_CHANNEL;
import static org.apache.flink.connector.redis.options.JedisOpOptions.REDIS_PUBSUB_SUBSCRIBE_PATTERNS;
import static org.apache.flink.connector.redis.options.RedisDataType.PUBSUB;
import static org.apache.flink.connector.redis.options.RedisOptions.REDIS_DEPLOY_MODE;

/**
 * RedisTableFactory for Redis service.
 */
public class RedisTableFactory implements DynamicTableSourceFactory, DynamicTableSinkFactory {

	public static final String IDENTIFIER = "redis";

	public static void validateTableSourceSinkOptions(ReadableConfig tableOptions, DataType physicalDataType) {
		// validate Redis deploy mode
		final RedisDeployMode deployMode = tableOptions.get(REDIS_DEPLOY_MODE);
		switch (deployMode) {
			case SINGLE: {
				final String host = tableOptions.get(REDIS_HOST);
				if (StringUtils.isBlank(host)) {
					String message = String.format("You must set %s if you want connect redis.", REDIS_HOST.key());
					throw new ValidationException(message);
				}
				final Integer port = tableOptions.get(REDIS_PORT);
				if (port == null || port < 0 || port >= 65536) {
					String message = String.format(
							"You must set %s in a valid range if you want connect redis.", REDIS_PORT.key());
					throw new IllegalArgumentException(message);
				}
				break;
			}
			case SENTINEL: {
				final String master = tableOptions.get(REDIS_MASTER);
				if (StringUtils.isBlank(master)) {
					String message = String.format(
							"You must set %s if you want connect redis in sentinel mode.", REDIS_MASTER.key());
					throw new ValidationException(message);
				}
				final String sentinels = tableOptions.get(REDIS_SENTINELS);
				if (StringUtils.isBlank(sentinels)) {
					String message = String.format(
							"You must set %s if you want connect redis in sentinel mode.", REDIS_SENTINELS.key());
					throw new ValidationException(message);
				}
				break;
			}
			case CLUSTER:
				final String hostAndPorts = tableOptions.get(REDIS_HOST_AND_PORTS);
				if (StringUtils.isBlank(hostAndPorts)) {
					String message = String.format(
							"You must set %s if you want connect redis in cluster mode.",
							REDIS_HOST_AND_PORTS.key());
					throw new ValidationException(message);
				}
				break;
			case SHARDED: {
				final String masters = tableOptions.get(REDIS_MASTERS);
				if (StringUtils.isBlank(masters)) {
					String message = String.format(
							"You must set %s if you want connect redis in sentinel mode.", REDIS_MASTERS.key());
					throw new ValidationException(message);
				}
				final String sentinels = tableOptions.get(REDIS_SENTINELS);
				if (StringUtils.isBlank(sentinels)) {
					String message = String.format(
							"You must set %s if you want connect redis in sentinel-sharded mode.",
							REDIS_SENTINELS.key());
					throw new ValidationException(message);
				}
				break;
			}
			default:
				throw new ValidationException(deployMode + " doesn't support at this moment.");
		}

		// validate Redis data type
		validateRedisDataType(tableOptions, physicalDataType);
	}

	private static void validateRedisDataType(ReadableConfig tableOptions, DataType physicalDataType) {
		final RedisDataType dataType = tableOptions.get(REDIS_DATA_TYPE);

		ObjIntConsumer<List<LogicalType>> fieldTypeChecker = (children, targetFiledNum) -> {
			if (children.size() != targetFiledNum) {
				String message = String.format(
						"If you set redis dataType = %s, you must make sure your table only have %d fields.",
						dataType,
						targetFiledNum);
				throw new ValidationException(message);
			}
			for (LogicalType child : children) {
				if (!children.get(0).equals(child)) {
					String message = String.format(""
							+ "If you set redis dataType = %s, "
							+ "you must make sure all your key and value(or elements of value) "
							+ "are the same type.", dataType);
					throw new ValidationException(message);
				}
			}
			for (LogicalType logicalType : children) {
				if (!(logicalType instanceof VarCharType)
						&& !(logicalType instanceof BinaryType)
						&& !(logicalType instanceof VarBinaryType)) {
					String message = String.format(""
							+ "If you set redis dataType = %s, "
							+ "you must make sure your fields type is one of "
							+ "STRING/VARCHAR/BINARY/VARBINARY/BYTES.", dataType);
					throw new ValidationException(message);
				}
			}
		};

		final List<LogicalType> children = physicalDataType.getChildren()
				.stream()
				.map(DataType::getLogicalType)
				.collect(Collectors.toList());
		switch (dataType) {
			case STRING: {
				fieldTypeChecker.accept(children, 2);
				break;
			}
			case HASH: {
				switch (children.size()) {
					case 2:
						if (!(children.get(1) instanceof MapType)) {
							String message = String.format(""
									+ "If you set redis dataType = %s "
									+ "and if your table has 2 field, "
									+ "you must make sure your 2nd field type is MAP.", dataType);
							throw new ValidationException(message);
						}
						final MapType mapType = (MapType) children.get(1);
						final LogicalType keyType = children.get(0);
						final LogicalType fieldType = mapType.getKeyType();
						final LogicalType valueType = mapType.getValueType();
						fieldTypeChecker.accept(Arrays.asList(keyType, fieldType, valueType), 3);
						break;
					case 3:
						fieldTypeChecker.accept(children, 3);
						break;
					default:
						String message = String.format(""
								+ "If you set redis dataType = %s, "
								+ "you must make sure your table only have 2 or 3 fields.", dataType);
						throw new ValidationException(message);
				}
				break;
			}
			case LIST:
			case SET: {
				if (children.size() != 2) {
					String message = String.format(""
							+ "If you set redis dataType = %s, "
							+ "you must make sure you have 2 field.", dataType);
					throw new ValidationException(message);
				}
				if (children.get(1) instanceof ArrayType) {
					final LogicalType elementType = ((ArrayType) children.get(1)).getElementType();
					fieldTypeChecker.accept(Arrays.asList(children.get(0), elementType), 2);
				} else {
					fieldTypeChecker.accept(Arrays.asList(children.get(0), children.get(1)), 2);
				}
				break;
			}
			case SORTED_SET: {
				switch (children.size()) {
					case 2:
						if (!(children.get(1) instanceof ArrayType)) {
							String message = String.format(""
									+ "If you set redis dataType = %s and if your table has 2 field, "
									+ "you must make sure your 2nd field type is ARRAY<ROW>.", dataType);
							throw new ValidationException(message);
						}
						final ArrayType arrayType = (ArrayType) children.get(1);
						final LogicalType elementType = arrayType.getElementType();
						final List<LogicalType> elemChildren = elementType.getChildren();
						if (elemChildren.size() != 2 || !(elemChildren.get(0) instanceof DoubleType)) {
							String message = String.format(""
									+ "If you set redis dataType = %s and if your table has 2 field, "
									+ "you must make sure your 2nd field type is ARRAY<ROW>. And the type of ROW has "
									+ "2 fields, and 1st field of ROW must be DOUBLE"
									+ "(which indicate score in redis sorted set).", dataType);
							throw new ValidationException(message);
						}
						final LogicalType keyType = children.get(0);
						fieldTypeChecker.accept(Arrays.asList(keyType, elemChildren.get(1)), 2);
						break;
					case 3:
						if (!(children.get(1) instanceof DoubleType)) {
							String message = String.format(""
									+ "If you set redis dataType = %s and if your table has 3 field, "
									+ "you must make sure your 2nd field type is DOUBLE"
									+ "(which indicate score in redis sorted set).", dataType);
							throw new ValidationException(message);
						}
						fieldTypeChecker.accept(Arrays.asList(children.get(0), children.get(2)), 2);
						break;
					default:
						String message = String.format(""
								+ "If you set redis dataType = %s, "
								+ "you must make sure your table only have 2 or 3 fields.", dataType);
						throw new ValidationException(message);
				}
				break;
			}
			case PUBSUB: {
				final String format = tableOptions.get(FactoryUtil.FORMAT);
				if (StringUtils.isEmpty(format)) {
					String message = String.format(""
							+ "If you set redis dataType = %s, "
							+ "you must make sure your have set `%s`.", dataType, FactoryUtil.FORMAT.key());
					throw new ValidationException(message);
				}
				final Optional<String> optional1 = tableOptions.getOptional(REDIS_PUBSUB_SUBSCRIBE_PATTERNS);
				final Optional<String> optional2 = tableOptions.getOptional(REDIS_PUBSUB_PUBLISH_CHANNEL);
				if (!optional1.isPresent() && !optional2.isPresent()) {
					String message = String.format(""
									+ "If you set redis dataType = %s, "
									+ "you need to set `%s` or `%s`."
							, dataType
							, REDIS_PUBSUB_SUBSCRIBE_PATTERNS.key()
							, REDIS_PUBSUB_SUBSCRIBE_PATTERNS.key());
					throw new ValidationException(message);
				}
				break;
			}
			default:
				throw new ValidationException(dataType + " doesn't support at this moment.");
		}
	}

	private void validateExceptFormat(FactoryUtil.TableFactoryHelper helper) {
		final Optional<String> format = helper.getOptions().getOptional(FactoryUtil.FORMAT);
		if (format.isPresent()) {
			helper.validateExcept(format.get());
		} else {
			helper.validate();
		}
	}

	@Override
	public DynamicTableSource createDynamicTableSource(Context context) {
		final FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
		validateExceptFormat(helper);

		final DataType physicalDataType = context.getCatalogTable().getSchema().toPhysicalRowDataType();

		final ReadableConfig tableOptions = helper.getOptions();
		validateTableSourceSinkOptions(tableOptions, physicalDataType);

		final RedisOptions redisOptions = new RedisOptions(tableOptions);
		if (redisOptions.dataType() == PUBSUB) {
			final DecodingFormat<DeserializationSchema<RowData>> decodingFormat = helper.discoverDecodingFormat(
					DeserializationFormatFactory.class,
					FactoryUtil.FORMAT);
			return new RedisTableSource(physicalDataType, decodingFormat, redisOptions);
		} else {
			return new RedisTableSource(physicalDataType, null, redisOptions);
		}
	}

	@Override
	public DynamicTableSink createDynamicTableSink(Context context) {
		final FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
		validateExceptFormat(helper);

		final DataType physicalDataType = context.getCatalogTable().getSchema().toPhysicalRowDataType();

		final ReadableConfig tableOptions = helper.getOptions();
		validateTableSourceSinkOptions(tableOptions, physicalDataType);

		final RedisOptions redisOptions = new RedisOptions(tableOptions);
		EncodingFormat<SerializationSchema<RowData>> encodingFormat = redisOptions.dataType() == PUBSUB
				? helper.discoverEncodingFormat(SerializationFormatFactory.class, FactoryUtil.FORMAT)
				: null;

		return new RedisTableSink(physicalDataType, encodingFormat, redisOptions);
	}

	@Override
	public String factoryIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public Set<ConfigOption<?>> requiredOptions() {
		final HashSet<ConfigOption<?>> options = new HashSet<>();
		options.add(REDIS_DATA_TYPE);
		options.add(REDIS_DEPLOY_MODE);
		return options;
	}

	@Override
	public Set<ConfigOption<?>> optionalOptions() {
		final Set<ConfigOption<?>> options = new HashSet<>();
		options.add(FactoryUtil.FORMAT);

		options.add(REDIS_HOST);
		options.add(REDIS_PORT);
		options.add(REDIS_HOST_AND_PORTS);
		options.add(REDIS_MASTER);
		options.add(REDIS_MASTERS);
		options.add(REDIS_SENTINELS);

		options.add(REDIS_KEY_PATTERN);
		options.add(REDIS_PUBSUB_SUBSCRIBE_PATTERNS);
		options.add(REDIS_PUBSUB_PUBLISH_CHANNEL);
		options.add(REDIS_IGNORE_ERROR);

		options.add(REDIS_USER);
		options.add(REDIS_PASSWORD);
		options.add(REDIS_CLIENT_TIMEOUT);
		options.add(MAX_WAIT_MILLIS);
		options.add(TEST_WHILE_IDLE);
		options.add(TIME_BETWEEN_EVICTION_RUNS_MILLIS);
		options.add(NUM_TESTS_PER_EVICTION_RUN);
		options.add(MIN_EVICTABLE_IDLE_TIME_MILLIS);
		options.add(MAX_TOTAL);
		options.add(MAX_IDLE);
		options.add(MIN_IDLE);
		options.add(TEST_ON_BORROW);
		options.add(TEST_ON_RETURN);

		options.add(REDIS_LOOKUP_CACHE_MAX_ROWS);
		options.add(REDIS_LOOKUP_CACHE_TTL);
		options.add(REDIS_LOOKUP_ASYNC);
		return options;
	}
}
