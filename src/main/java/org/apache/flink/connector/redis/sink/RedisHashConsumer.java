package org.apache.flink.connector.redis.sink;

import org.apache.flink.table.data.ArrayData;
import org.apache.flink.table.data.MapData;
import org.apache.flink.table.data.RowData;

import redis.clients.jedis.PipelineBase;

/**
 * RedisHashConsumer.
 */
public class RedisHashConsumer implements RedisRecordConsumer<RowData> {

	private final RedisRecordConsumer<RowData> delegate;

	public RedisHashConsumer(boolean isStringType, boolean isMapStructure) {
		final KeySelector<RowData, byte[]> keySelector = (isStringType)
				? r -> r.getString(0).toBytes()
				: r -> r.getBinary(0);

		if (isMapStructure) {
			delegate = (aRecord, client) -> {
				final MapData map = aRecord.getMap(1);
				final ArrayData fieldArray = map.keyArray();
				final ArrayData valueArray = map.valueArray();
				final int size = map.size();

				final byte[] key = keySelector.apply(aRecord);
				for (int i = 0; i < size; i++) {
					client.hset(key, fieldArray.getBinary(i), valueArray.getBinary(i));
				}
			};
		} else {
			final ValueSelector<RowData, byte[]> fieldSelector = isStringType
					? r -> r.getString(1).toBytes()
					: r -> r.getBinary(1);
			final ValueSelector<RowData, byte[]> valueSelector = isStringType
					? r -> r.getString(2).toBytes()
					: r -> r.getBinary(2);
			delegate = (aRecord, client) ->
					client.hset(keySelector.apply(aRecord), fieldSelector.apply(aRecord), valueSelector.apply(aRecord));
		}
	}

	@Override
	public void apply(RowData aRecord, PipelineBase client) {
		delegate.apply(aRecord, client);
	}
}
