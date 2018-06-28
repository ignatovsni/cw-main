package com.cwsni.world.common.algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.cwsni.world.model.Province;
import com.cwsni.world.model.WorldMap;
import com.cwsni.world.model.player.PGame;
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

	public List<Integer> findShortestPath(WorldMap map, int fromId, int toId) {
		Province fromProv = map.findProvById(fromId);
		Province toProv = map.findProvById(toId);
		return findShortestPath(fromProv, toProv);
	}

	public List<Integer> findShortestPath(Node fromProv, Node toProv) {
		Heap<Pair<Node, Double>> fromArea = new Heap<>();
		Map<Node, Pair<Node, Double>> fromAreaDistance = new HashMap<>();
		fromArea.put(new Pair<>(fromProv, 0.0));
		fromAreaDistance.put(fromProv, new Pair<>(null, 0.0));

		while (fromArea.size() > 0 && !fromAreaDistance.containsKey(toProv)) {
			stepWave(fromArea, fromAreaDistance);
		}

		List<Integer> path;
		Pair<Node, Double> toNode = fromAreaDistance.get(toProv);
		if (toNode != null) {
			// found
			path = new LinkedList<>();
			path.add(0, toProv.getId());

			while (toNode != null && toNode.first != null) {
				path.add(0, toNode.first.getId());
				toNode = fromAreaDistance.get(toNode.first);
			}

		} else {
			path = Collections.EMPTY_LIST;
		}

		return path;
	}

	static void stepWave(Heap<Pair<Node, Double>> heap, Map<Node, Pair<Node, Double>> areaDistance) {
		Pair<Node, Double> p = heap.poll();
		((Stream<Node>) (p.first.getNeighbors().stream())).filter(n -> !areaDistance.containsKey(n)).forEach(n -> {
			// now distance between all nodes = 1
			double distance = p.second + 1;
			heap.put(new Pair<>(n, distance));
			areaDistance.put(n, new Pair<>(p.first, distance));
		});
	}

}
