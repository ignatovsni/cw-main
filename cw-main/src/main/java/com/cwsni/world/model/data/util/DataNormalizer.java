package com.cwsni.world.model.data.util;

public class DataNormalizer {

	public static double minMax(double value, double min, double max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

}
