package com.cwsni.world.common.algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cwsni.world.util.Heap;

@Component
public class GameAlgorithms {

	private static class Pair<F, S extends Comparable<S>> implements Comparable<Pair<F, S>> {
		F first;
		S second;

		public Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public int compareTo(Pair<F, S> o) {
			return second.compareTo(o.second);
		}

		@Override
		public String toString() {
			return "first: [" + first + "]; second: [" + second + "]";
		}

	}

	public List<Object> findShortestPath(Node nodeFrom, Node nodeTo) {
		Heap<Pair<Node, Double>> fromArea = new Heap<>();
		Map<Node, Pair<Node, Double>> fromAreaDistance = new HashMap<>();
		fromArea.put(new Pair<>(nodeFrom, 0.0));
		fromAreaDistance.put(nodeFrom, new Pair<>(null, 0.0));

		while (fromArea.size() > 0 && !fromAreaDistance.containsKey(nodeTo)) {
			stepWave(fromArea, fromAreaDistance);
		}

		List<Object> path;
		Pair<Node, Double> toNode = fromAreaDistance.get(nodeTo);
		if (toNode != null) {
			// found
			path = new LinkedList<>();
			path.add(0, nodeTo.getKey());

			while (toNode != null && toNode.first != null) {
				path.add(0, toNode.first.getKey());
				toNode = fromAreaDistance.get(toNode.first);
			}

		} else {
			path = Collections.emptyList();
		}
		
		if (path.size()<2) {
			System.out.println(path);
		}

		return path;
	}

	private void stepWave(Heap<Pair<Node, Double>> heap, Map<Node, Pair<Node, Double>> visited) {
		Pair<Node, Double> p = heap.poll();
		p.first.getNeighbors().stream().filter(n -> !visited.containsKey(n)).forEach(n -> {
			// now distance between all nodes = 1
			double distance = p.second + 1;
			heap.put(new Pair<>(n, distance));
			visited.put(n, new Pair<>(p.first, distance));
		});
	}

}
