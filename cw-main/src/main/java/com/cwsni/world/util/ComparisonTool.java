package com.cwsni.world.util;

public class ComparisonTool {

	static public boolean isEqual(Integer a, Integer b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}
	
	static public boolean isEqual(Double a, Double b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}

}
