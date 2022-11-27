package org.apache.flink.connector.redis.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A Thread safe data structure similar to {@code Map<P, List<List<R>>>}.
 *
 * @param <P> for Partition.
 * @param <R> for Record of Partition.
 */
public abstract class RecordAccumulator<P, R> {

	private final ConcurrentHashMap<P, Deque<RecordBatch<R>>> batches = new ConcurrentHashMap<>();
	private final Set<P> totalPartitions = new HashSet<>();

	public void append(P partiton, R aRecord) {
		final Deque<RecordBatch<R>> deque = batches.computeIfAbsent(partiton, key -> {
			totalPartitions.add(partiton);
			return new ArrayDeque<>();
		});
		synchronized (deque) {
			if (tryAppend(deque, aRecord)) {
				return;
			}
			final RecordBatch<R> batch = new RecordBatch<>(16 << 10);
			batch.tryAppend(aRecord, sizeOf(aRecord));
			deque.addLast(batch);
		}
	}

	public Map<P, List<RecordBatch<R>>> drain(long lingerMs) {
		Map<P, List<RecordBatch<R>>> finishBatches = new HashMap<>();
		long now = System.currentTimeMillis();
		for (P partition : totalPartitions) {
			final List<RecordBatch<R>> ready = finishBatches.computeIfAbsent(partition, k -> new ArrayList<>());
			final Deque<RecordBatch<R>> deque = this.batches.get(partition);
			synchronized (deque) {
				final RecordBatch<R> batch = deque.peekFirst();
				if (batch != null && (batch.isComplete() || batch.isTimeout(now, lingerMs))) {
					ready.add(deque.pollFirst());
				}
			}
		}
		return finishBatches;
	}

	protected abstract int sizeOf(R aRecord);

	private boolean tryAppend(Deque<RecordBatch<R>> deque, R value) {
		final RecordBatch<R> batch = deque.peekLast();
		if (batch == null) {
			return false;
		}
		return batch.tryAppend(value, sizeOf(value));
	}

	/**
	 * Batch for Record.
	 *
	 * @param <T> for Record.
	 */
	private static class RecordBatch<T> {

		private final List<T> records = new ArrayList<>();
		private final long createTimestamp = System.currentTimeMillis();
		private boolean writable = true;
		private long remainingCapacity;

		public RecordBatch(long remainingCapacity) {
			this.remainingCapacity = remainingCapacity;
		}

		private boolean tryAppend(T aRecord, int recordSize) {
			if (!writable) {
				return false;
			}
			records.add(aRecord);
			remainingCapacity -= recordSize;
			if (remainingCapacity <= 0) {
				writable = false;
			}
			return true;
		}

		public void forEach(Consumer<T> consumer) {
			records.forEach(consumer);
		}

		private boolean isTimeout(long now, long lingerMs) {
			return (now - createTimestamp) >= lingerMs;
		}

		private boolean isComplete() {
			return !writable;
		}
	}
}
