package com.cwsni.world.util;

public class HeapComparable<T extends Comparable<T>> extends Heap<T> {

	public HeapComparable() {
		this(1, true);
	}

	public HeapComparable(boolean isMinHeap) {
		this(1, isMinHeap);
	}

	public HeapComparable(int size) {
		this(size, true);
	}

	public HeapComparable(int size, boolean isMinHeap) {
		super(size, isMinHeap, null);
	}

	protected int compare(T first, T second) {
		if (isMinHeap) {
			return first.compareTo(second);
		} else {
			return second.compareTo(first);
		}
	}

}
