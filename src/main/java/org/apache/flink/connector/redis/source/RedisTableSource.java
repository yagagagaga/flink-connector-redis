package org.apache.flink.connector.redis.source;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.connector.redis.options.RedisDataType;
import org.apache.flink.connector.redis.options.RedisOptions;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.source.AsyncTableFunctionProvider;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.LookupTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.SourceFunctionProvider;
import org.apache.flink.table.connector.source.TableFunctionProvider;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.DataType;

import java.util.Objects;

import static org.apache.flink.connector.redis.options.RedisDataType.PUBSUB;
import static org.apache.flink.connector.redis.options.RedisDeployMode.SHARDED;
import static org.apache.flink.util.Preconditions.checkArgument;

/**
 * RedisTableSource for Redis service.
 */
public class RedisTableSource implements ScanTableSource, LookupTableSource {

	private final DataType physicalDataType;
	private final DecodingFormat<DeserializationSchema<RowData>> decodingFormat;
	private final RedisOptions redisOptions;

	public RedisTableSource(
			DataType physicalDataType,
			DecodingFormat<DeserializationSchema<RowData>> decodingFormat,
			RedisOptions redisOptions) {
		this.physicalDataType = physicalDataType;
		this.decodingFormat = decodingFormat;
		this.redisOptions = redisOptions;
	}

	@Override
	public ChangelogMode getChangelogMode() {
		return ChangelogMode.insertOnly();
	}

	@Override
	public ScanRuntimeProvider getScanRuntimeProvider(ScanContext runtimeProviderContext) {
		if (redisOptions.dataType() == PUBSUB) {
			DeserializationSchema<RowData> deserializationSchema = decodingFormat.createRuntimeDecoder(
					runtimeProviderContext, physicalDataType);

			if (redisOptions.deployMode() == SHARDED) {
				final RedisPubSubParallelSourceFunction<RowData> sourceFunction = new RedisPubSubParallelSourceFunction<>(
						redisOptions, deserializationSchema);
				return SourceFunctionProvider.of(sourceFunction, false);
			} else {
				final RedisPubSubSourceFunction<RowData> sourceFunction = new RedisPubSubSourceFunction<>(
						redisOptions, deserializationSchema);
				return SourceFunctionProvider.of(sourceFunction, false);
			}
		} else {
			final RedisRecordProducer<RowData> producer = redisOptions.createRecordProducer(physicalDataType);
			if (redisOptions.deployMode() == SHARDED) {
				return SourceFunctionProvider.of(new RedisParallelSourceFunction<>(producer, redisOptions), true);
			}
			return SourceFunctionProvider.of(new RedisSourceFunction<>(producer, redisOptions), true);
		}
	}

	@Override
	public DynamicTableSource copy() {
		return new RedisTableSource(physicalDataType, decodingFormat, redisOptions);
	}

	@Override
	public String asSummaryString() {
		return RedisTableSource.class.getSimpleName();
	}

	@Override
	public LookupRuntimeProvider getLookupRuntimeProvider(LookupContext context) {
		if (redisOptions.dataType() != RedisDataType.HASH) {
			checkArgument(context.getKeys().length == 1 && context.getKeys()[0].length == 1,
					"Currently, Redis table can only be lookup by single key.");
		} else {
			checkArgument(context.getKeys().length <= 2,
					"Currently, Redis HASH table can only be lookup by most 2 key.");
		}

		if (redisOptions.async()) {
			final RedisRowDataAsyncLookupFunction tableFunction =
					new RedisRowDataAsyncLookupFunction(redisOptions, redisOptions.createRecordProducer(physicalDataType));
			return AsyncTableFunctionProvider.of(tableFunction);
		} else {
			final RedisRowDataLookupFunction tableFunction =
					new RedisRowDataLookupFunction(redisOptions, redisOptions.createRecordProducer(physicalDataType));
			return TableFunctionProvider.of(tableFunction);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RedisTableSource that = (RedisTableSource) o;
		return Objects.equals(physicalDataType, that.physicalDataType) &&
				Objects.equals(decodingFormat, that.decodingFormat) &&
				Objects.equals(redisOptions, that.redisOptions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(physicalDataType, decodingFormat, redisOptions);
	}
}
