package org.apache.flink.connector.redis.sink;

import org.apache.flink.table.data.RowData;

import redis.clients.jedis.PipelineBase;

/**
 * RedisStringConsumer.
 */
public class RedisStringConsumer implements RedisRecordConsumer<RowData> {

	private final KeySelector<RowData, byte[]> keySelector;
	private final ValueSelector<RowData, byte[]> valueSelector;

	public RedisStringConsumer(boolean isStringType) {
		keySelector = isStringType
				? r -> r.getString(0).toBytes()
				: r -> r.getBinary(0);

		valueSelector = isStringType
				? r -> r.getString(1).toBytes()
				: r -> r.getBinary(1);
	}

	@Override
	public void apply(RowData aRecord, PipelineBase client) {
		final byte[] key = keySelector.apply(aRecord);
		final byte[] value = valueSelector.apply(aRecord);
		client.set(key, value);
	}
}
