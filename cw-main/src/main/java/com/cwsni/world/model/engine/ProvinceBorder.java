package com.cwsni.world.model.engine;

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

	public int getFirst() {
		return first;
	}

	public int getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return first * 10000 + second;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ProvinceBorder)) {
			return false;
		}
		ProvinceBorder pb = (ProvinceBorder) obj;
		return pb.first == this.first && pb.second == this.second;
	}

}