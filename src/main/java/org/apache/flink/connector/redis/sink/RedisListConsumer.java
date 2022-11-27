package org.apache.flink.connector.redis.sink;

import org.apache.flink.table.data.ArrayData;
import org.apache.flink.table.data.RowData;

import redis.clients.jedis.PipelineBase;

import java.util.Iterator;

/**
 * RedisListConsumer.
 */
public class RedisListConsumer implements RedisRecordConsumer<RowData> {

	private final KeySelector<RowData, byte[]> keySelector;
	private final ValueSelector<RowData, Iterator<byte[]>> valueSelector;

	public RedisListConsumer(boolean isStringType) {
		keySelector = isStringType
				? r -> r.getString(0).toBytes()
				: r -> r.getBinary(0);

		valueSelector = isStringType
				? this::convertToString
				: this::convertToBytes;
	}

	private Iterator<byte[]> convertToBytes(RowData r) {
		final ArrayData array = r.getArray(1);
		final int size = array.size();
		return new Iterator<byte[]>() {
			int prt = 0;

			@Override
			public boolean hasNext() {
				return prt < size;
			}

			@Override
			public byte[] next() {
				return array.getBinary(prt++);
			}
		};
	}

	private Iterator<byte[]> convertToString(RowData r) {
		final ArrayData array = r.getArray(1);
		final int size = array.size();

		return new Iterator<byte[]>() {
			int prt = 0;

			@Override
			public boolean hasNext() {
				return prt < size;
			}

			@Override
			public byte[] next() {
				return array.getString(prt++).toBytes();
			}
		};
	}

	@Override
	public void apply(RowData aRecord, PipelineBase client) {
		final byte[] key = keySelector.apply(aRecord);
		final Iterator<byte[]> itr = valueSelector.apply(aRecord);
		itr.forEachRemaining(e -> client.lpush(key, e));
	}
}
