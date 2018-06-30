package com.cwsni.world.model;

public class ComparisonTool {

	static public boolean isEqual(Integer a, Integer b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}

}
