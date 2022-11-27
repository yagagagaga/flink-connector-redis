package org.apache.flink.connector.redis.sink;

import org.apache.flink.table.data.ArrayData;
import org.apache.flink.table.data.RowData;

import redis.clients.jedis.PipelineBase;

/**
 * RedisSortedSetConsumer.
 */
public class RedisSortedSetConsumer implements RedisRecordConsumer<RowData> {

	private final RedisRecordConsumer<RowData> delegate;

	public RedisSortedSetConsumer(boolean isStringType, boolean isArrayStructure) {
		final KeySelector<RowData, byte[]> keySelector = (isStringType)
				? r -> r.getString(0).toBytes()
				: r -> r.getBinary(0);

		if (isArrayStructure) {
			final ValueSelector<RowData, byte[]> elemSelector = isStringType
					? r -> r.getString(1).toBytes()
					: r -> r.getBinary(1);

			delegate = (aRecord, client) -> {
				final ArrayData array = aRecord.getArray(1);
				final int size = array.size();

				final byte[] key = keySelector.apply(aRecord);
				for (int i = 0; i < size; i++) {
					final RowData row = array.getRow(i, 2);
					client.zadd(key, row.getDouble(0), elemSelector.apply(row));
				}
			};
		} else {
			final ValueSelector<RowData, Double> scoreSelector = r -> r.getDouble(1);
			final ValueSelector<RowData, byte[]> elemSelector = isStringType
					? r -> r.getString(2).toBytes()
					: r -> r.getBinary(2);
			delegate = (aRecord, client) ->
					client.zadd(keySelector.apply(aRecord), scoreSelector.apply(aRecord), elemSelector.apply(aRecord));
		}
	}

	@Override
	public void apply(RowData aRecord, PipelineBase client) {
		delegate.apply(aRecord, client);
	}
}
