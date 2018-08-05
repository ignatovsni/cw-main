package com.cwsni.world.services.algorithms;

import java.util.Collection;

public abstract class Node {

	public abstract Object getKey();

	public abstract Collection<Node> getNeighbors();

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || !(obj instanceof Node)) {
			return false;
		}
		return getKey().equals(((Node) obj).getKey());
	}
	
	@Override
	public String toString() {
		return "Node: " + getKey();
	}

}
