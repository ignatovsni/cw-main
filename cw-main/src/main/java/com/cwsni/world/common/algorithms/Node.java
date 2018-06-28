package com.cwsni.world.common.algorithms;

import java.util.List;

public interface Node<T> {

	public List<T> getNeighbors();

	public int getId();

}
