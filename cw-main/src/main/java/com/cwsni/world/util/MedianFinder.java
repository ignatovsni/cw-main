package com.cwsni.world.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MedianFinder {

	public int findMedianIntegerAndGetZeroIfEmpty(Collection<Integer> collection) {
		Integer result = findMedianInteger(collection);
		if (result != null) {
			return result;
		} else {
			return 0;
		}
	}

	public Integer findMedianInteger(Collection<Integer> collection) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		ArrayList<Integer> arrayList;
		if (collection instanceof ArrayList) {
			arrayList = (ArrayList<Integer>) collection;
		} else {
			arrayList = new ArrayList<>(collection);
		}
		Collections.sort(arrayList);
		if (arrayList.size() % 2 == 1) {
			return arrayList.get(arrayList.size() / 2);
		} else {
			int idx = arrayList.size() / 2;
			return (arrayList.get(idx) + arrayList.get(idx - 1)) / 2;
		}
	}

	public double findMedianDoubleAndGetZeroIfEmpty(Collection<Double> collection) {
		Double result = findMedianDouble(collection);
		if (result != null) {
			return result;
		} else {
			return 0.0;
		}
	}

	public Double findMedianDouble(Collection<Double> collection) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		ArrayList<Double> arrayList;
		if (collection instanceof ArrayList) {
			arrayList = (ArrayList<Double>) collection;
		} else {
			arrayList = new ArrayList<>(collection);
		}
		Collections.sort(arrayList);
		if (arrayList.size() % 2 == 1) {
			return arrayList.get(arrayList.size() / 2);
		} else {
			int idx = arrayList.size() / 2;
			return (arrayList.get(idx) + arrayList.get(idx - 1)) / 2;
		}
	}

}
