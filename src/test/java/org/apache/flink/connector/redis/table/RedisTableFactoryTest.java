package org.apache.flink.connector.redis.table;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.redis.options.RedisOptions;
import org.apache.flink.connector.redis.sink.RedisSinkFunction;
import org.apache.flink.connector.redis.sink.RedisTableSink;
import org.apache.flink.connector.redis.source.RedisPubSubSourceFunction;
import org.apache.flink.connector.redis.source.RedisTableSource;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.CatalogTableImpl;
import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.format.EncodingFormat;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.sink.SinkFunctionProvider;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.SourceFunctionProvider;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.factories.TestFormatFactory;
import org.apache.flink.table.runtime.connector.sink.SinkRuntimeProviderContext;
import org.apache.flink.table.runtime.connector.source.ScanRuntimeProviderContext;
import org.apache.flink.table.types.DataType;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link RedisTableFactory}.
 */
public class RedisTableFactoryTest {

	private static final String KEY = "key";
	private static final String VALUE = "value";

	private static final TableSchema SCHEMA =
			TableSchema.builder()
					.add(TableColumn.physical(KEY, DataTypes.STRING()))
					.add(TableColumn.physical(VALUE, DataTypes.STRING()))
					.build();
	private static final DataType SCHEMA_DATA_TYPE = SCHEMA.toPhysicalRowDataType();
	private static final Properties REDIS_SOURCE_PROPERTIES = new Properties();

	static {
		REDIS_SOURCE_PROPERTIES.setProperty("redis.deploy-mode", "single");
		REDIS_SOURCE_PROPERTIES.setProperty("redis.host", "localhost");
		REDIS_SOURCE_PROPERTIES.setProperty("redis.port", "6379");
		REDIS_SOURCE_PROPERTIES.setProperty("redis.data-type", "pubsub");
		REDIS_SOURCE_PROPERTIES.setProperty("redis.pubsub.publish-channel", "test");
		REDIS_SOURCE_PROPERTIES.setProperty("redis.pubsub.subscribe-patterns", "test");
	}

	@Test
	public void testTableSource() {
		final DynamicTableSource actualSource = createTableSource(SCHEMA, getBasicSourceOptions());
		final RedisTableSource actualRedisSource = (RedisTableSource) actualSource;

		final DecodingFormat<DeserializationSchema<RowData>> valueDecodingFormat =
				new TestFormatFactory.DecodingFormatMock(",", true);

		// Test scan source equals
		final RedisTableSource expectedRedisSource = createExpectedScanSource(
				SCHEMA_DATA_TYPE,
				valueDecodingFormat,
				new RedisOptions(REDIS_SOURCE_PROPERTIES));
		assertEquals(actualRedisSource, expectedRedisSource);

		// Test Redis consumer
		ScanTableSource.ScanRuntimeProvider provider =
				actualRedisSource.getScanRuntimeProvider(ScanRuntimeProviderContext.INSTANCE);
		assertThat(provider, instanceOf(SourceFunctionProvider.class));
		final SourceFunctionProvider sourceFunctionProvider = (SourceFunctionProvider) provider;
		final SourceFunction<RowData> sourceFunction =
				sourceFunctionProvider.createSourceFunction();
		assertThat(sourceFunction, instanceOf(RedisPubSubSourceFunction.class));
	}

	@Test
	public void testTableSink() {
		final DynamicTableSink actualSink = createTableSink(SCHEMA, getBasicSourceOptions());
		final RedisTableSink actualRedisSink = (RedisTableSink) actualSink;

		final EncodingFormat<SerializationSchema<RowData>> valueEncodingFormat =
				new TestFormatFactory.EncodingFormatMock(",");

		// Test sink equals
		final RedisTableSink expectedRedisSink = createExpectedSink(
				SCHEMA_DATA_TYPE,
				valueEncodingFormat,
				new RedisOptions(REDIS_SOURCE_PROPERTIES));
		assertEquals(actualRedisSink, expectedRedisSink);

		// Test Redis producer
		DynamicTableSink.SinkRuntimeProvider provider =
				actualRedisSink.getSinkRuntimeProvider(new SinkRuntimeProviderContext(false));
		assertThat(provider, instanceOf(SinkFunctionProvider.class));
		final SinkFunctionProvider sinkFunctionProvider = (SinkFunctionProvider) provider;
		final SinkFunction<RowData> sinkFunction = sinkFunctionProvider.createSinkFunction();
		assertThat(sinkFunction, instanceOf(RedisSinkFunction.class));
	}

	// --------------------------------------------------------------------------------------------
	// Utilities
	// --------------------------------------------------------------------------------------------

	private static Map<String, String> getBasicSourceOptions() {
		Map<String, String> tableOptions = new HashMap<>();
		// Redis specific options.
		tableOptions.put("connector", RedisTableFactory.IDENTIFIER);
		tableOptions.put("redis.deploy-mode", "single");
		tableOptions.put("redis.host", "localhost");
		tableOptions.put("redis.port", "6379");
		tableOptions.put("redis.data-type", "pubsub");
		tableOptions.put("redis.pubsub.publish-channel", "test");
		tableOptions.put("redis.pubsub.subscribe-patterns", "test");
		// Format options.
		tableOptions.put("format", TestFormatFactory.IDENTIFIER);
		final String formatDelimiterKey =
				String.format(
						"%s.%s", TestFormatFactory.IDENTIFIER, TestFormatFactory.DELIMITER.key());
		final String failOnMissingKey =
				String.format(
						"%s.%s",
						TestFormatFactory.IDENTIFIER, TestFormatFactory.FAIL_ON_MISSING.key());
		tableOptions.put(formatDelimiterKey, ",");
		tableOptions.put(failOnMissingKey, "true");
		return tableOptions;
	}

	private static RedisTableSource createExpectedScanSource(
			DataType physicalDataType,
			DecodingFormat<DeserializationSchema<RowData>> valueDecodingFormat,
			RedisOptions options) {
		return new RedisTableSource(physicalDataType, valueDecodingFormat, options);
	}

	private static RedisTableSink createExpectedSink(
			DataType physicalDataType,
			EncodingFormat<SerializationSchema<RowData>> valueEncodingFormat,
			RedisOptions options
	) {
		return new RedisTableSink(physicalDataType, valueEncodingFormat, options);
	}

	private static DynamicTableSource createTableSource(
			TableSchema schema, Map<String, String> options) {
		final ObjectIdentifier objectIdentifier =
				ObjectIdentifier.of("default", "default", "scanTable");
		final CatalogTable catalogTable = new CatalogTableImpl(schema, options, "scanTable");
		return FactoryUtil.createTableSource(
				null,
				objectIdentifier,
				catalogTable,
				new Configuration(),
				Thread.currentThread().getContextClassLoader(),
				false);
	}

	private static DynamicTableSink createTableSink(
			TableSchema schema, Map<String, String> options) {
		final ObjectIdentifier objectIdentifier =
				ObjectIdentifier.of("default", "default", "sinkTable");
		final CatalogTable catalogTable = new CatalogTableImpl(schema, options, "sinkTable");
		return FactoryUtil.createTableSink(
				null,
				objectIdentifier,
				catalogTable,
				new Configuration(),
				Thread.currentThread().getContextClassLoader(),
				false);
	}
}
