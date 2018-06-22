package com.cwsni.world.util;

import java.util.Arrays;

public class Heap<T extends Comparable<T>> {

	private Object[] elements;
	private int size = 0;
	private boolean isMinHeap = true;

	public Heap() {
		this(1, true);
	}

	public Heap(boolean isMinHeap) {
		this(1, isMinHeap);
	}

	public Heap(int size) {
		this(size, true);
	}

	public Heap(int size, boolean isMinHeap) {
		elements = new Object[Math.max(size, 1)];
		this.isMinHeap = isMinHeap;
	}

	public void put(T o) {
		if (elements.length <= size) {
			elements = Arrays.copyOf(elements, elements.length * 2);
		}
		elements[size++] = o;
		rise(size - 1);
	}

	private void rise(int idx) {
		if (idx == 0) {
			return;
		}
		int parentIdx = (idx - 1) / 2;
		T el = (T) elements[idx];
		T parent = (T) elements[parentIdx];
		if (compare(el, parent) < 0) {
			elements[parentIdx] = el;
			elements[idx] = parent;
			rise(parentIdx);
		}
	}

	private int compare(T first, T second) {
		if (isMinHeap) {
			return first.compareTo(second);
		} else {
			return second.compareTo(first);
		}
	}

	public T poll() {
		if (size == 0) {
			return null;
		}
		T result = (T) elements[0];
		size--;
		elements[0] = elements[size];
		elements[size] = null;
		sink(0);
		return result;
	}

	public T peek() {
		if (size == 0) {
			return null;
		}
		return (T) elements[0];
	}

	private void sink(int parentIdx) {
		int rightIdx = parentIdx * 2 + 1;
		int leftIdx = rightIdx + 1;
		if (rightIdx >= size) {
			return;
		}
		int minIdx = rightIdx;
		if (leftIdx < size && compare((T) elements[leftIdx], (T) elements[rightIdx]) < 0) {
			minIdx = leftIdx;
		}
		if (compare((T) elements[minIdx], (T) elements[parentIdx]) < 0) {
			T temp = (T) elements[parentIdx];
			elements[parentIdx] = elements[minIdx];
			elements[minIdx] = temp;
			sink(minIdx);
		}

	}

	public int size() {
		return size;
	}

}
