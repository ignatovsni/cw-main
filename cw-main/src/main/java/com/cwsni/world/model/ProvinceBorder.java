package com.cwsni.world.model;

public class ProvinceBorder {
	private int first;
	private int second;

	public ProvinceBorder(int first, int second) {
		if (first <= second) {
			this.first = first;
			this.second = second;
		} else {
			this.first = second;
			this.second = first;
		}
	}

	@Override
	public int hashCode() {
		return first * 10000 + second;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProvinceBorder)) {
			return false;
		}
		ProvinceBorder pb = (ProvinceBorder) obj;
		return pb.first == this.first && pb.second == this.second;
	}

}