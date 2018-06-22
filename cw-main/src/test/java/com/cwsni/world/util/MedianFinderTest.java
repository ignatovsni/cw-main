package com.cwsni.world.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

public class MedianFinderTest {

	@Test
	public void testInteger() {
		Collection<Integer> elements = new ArrayList<>(Arrays.asList());
		MedianFinder mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), null);

		elements = new ArrayList<>(Arrays.asList(1));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 1);

		elements = new ArrayList<>(Arrays.asList(1, 2));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 1);

		elements = new ArrayList<>(Arrays.asList(10, 20));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 15);
		
		elements = new ArrayList<>(Arrays.asList(1, 2, 3));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 2);

		elements = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 3);
		
		elements = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 5));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 3);
		
		elements = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 5, 5));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 4);
		
		elements = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 5, 5, 5));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 4);
		
		elements = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 5));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 5);
		elements = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5));
		mf = new MedianFinder();
		assertEquals(mf.findMedian(elements), (Integer) 1);
	}

}
