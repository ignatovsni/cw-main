package com.cwsni.world.model;

public class ComparisonTool {

	static public boolean isEqual(Integer a, Integer b) {
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

}
