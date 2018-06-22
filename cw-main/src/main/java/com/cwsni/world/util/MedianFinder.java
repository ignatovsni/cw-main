package com.cwsni.world.util;

import java.util.Collection;

public class MedianFinder<T extends Comparable<T>> {

	private Heap<T> minHeap;
	private Heap<T> maxHeap;

	public MedianFinder() {
		this(1);
	}

	public MedianFinder(int countOfElements) {
		minHeap = new Heap<>(countOfElements / 2 + 2, true);
		maxHeap = new Heap<>(countOfElements / 2 + 2, false);
	}

	public T findMedian(Collection<T> collection) {
		collection.forEach(e -> {
			if (minHeap.size() == 0) {
				minHeap.put(e);
			} else if (e.compareTo(minHeap.peek()) > 0) {
				minHeap.put(e);
			} else {
				maxHeap.put(e);
			}
			// balance heaps
			if (minHeap.size() > maxHeap.size() + 1) {
				maxHeap.put(minHeap.poll());
			}
		});
		return minHeap.peek();
	}

}
