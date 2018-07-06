package com.cwsni.world.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class HeapTest {

	@Test
	public void testMinSorting() {
		testSorting(true);
	}

	@Test
	public void testMaxSorting() {
		testSorting(false);
	}

	private void testSorting(boolean isMin) {
		HeapComparable<Integer> heap = new HeapComparable<>(isMin);
		Integer[] elements = { 2, 5, 1, 45, 3, 5, 5, 6, 7, 71, 72, 70, 69 };
		Arrays.asList(elements).forEach(el -> heap.put(el));
		assertEquals(heap.size(), elements.length);
		if (isMin) {
			assertEquals(heap.peek(), (Integer) Arrays.asList(elements).stream().mapToInt(e -> e).min().getAsInt());
		} else {
			assertEquals(heap.peek(), (Integer) Arrays.asList(elements).stream().mapToInt(e -> e).max().getAsInt());
		}
		List<Integer> numbers = new ArrayList<>();
		while (heap.size() > 0) {
			numbers.add(heap.poll());
		}
		if (isMin) {
			Arrays.sort(elements, (e1, e2) -> e1 - e2);
		} else {
			Arrays.sort(elements, (e1, e2) -> e2 - e1);
		}
		assertEquals(numbers, Arrays.asList(elements));
		assertEquals(heap.poll(), null);
		assertEquals(heap.peek(), null);
		assertEquals(heap.size(), 0);
	}

}
