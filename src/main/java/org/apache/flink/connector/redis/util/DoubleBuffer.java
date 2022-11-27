package org.apache.flink.connector.redis.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * DoubleBuffer.
 */
public class DoubleBuffer<E> {
	private Queue<E> backupBuffer = new ConcurrentLinkedQueue<>();
	private Queue<E> currentBuffer = new ConcurrentLinkedQueue<>();
	private volatile boolean flushing = false;
	private volatile boolean hasSomeoneWaiting = false;

	public void add(E elem) {
		currentBuffer.add(elem);
	}

	private synchronized boolean sway() {
		if (flushing) {
			// make sure only one thread to flush
			if (hasSomeoneWaiting) {
				return false;
			}
			hasSomeoneWaiting = true;
			// wait until finish writing
			while (flushing) {
				try {
					wait(2000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			hasSomeoneWaiting = false;
		}

		Queue<E> tmp = currentBuffer;
		currentBuffer = backupBuffer;
		backupBuffer = tmp;

		flushing = true;
		return true;
	}

	public List<E> flush() {
		if (!sway()) {
			return Collections.emptyList();
		}

		LinkedList<E> tmp = new LinkedList<>(backupBuffer);
		backupBuffer.clear();

		synchronized (this) {
			flushing = false;
			notifyAll();
		}

		return tmp;
	}
}
