package com.cwsni.world.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cwsni.world.util.HeapComparable;

public class PathFinder<ObjectType, KeyType> {

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

	private static class Node<ObjectType, KeyType> {

		private ObjectType object;
		private Function<ObjectType, KeyType> keySupplier;
		private Function<ObjectType, Collection<ObjectType>> neigborsSupplier;

		public Node(ObjectType object, Function<ObjectType, KeyType> keySupplier,
				Function<ObjectType, Collection<ObjectType>> neigborsSupplier) {
			this.object = object;
			this.keySupplier = keySupplier;
			this.neigborsSupplier = neigborsSupplier;
		}

		public KeyType getKey() {
			return keySupplier.apply(object);
		}

		public Collection<Node<ObjectType, KeyType>> getNeighbors() {
			return neigborsSupplier.apply(object).stream()
					.map(o -> new Node<ObjectType, KeyType>(o, keySupplier, neigborsSupplier))
					.collect(Collectors.toList());
		}

		@Override
		public int hashCode() {
			return getKey().hashCode();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || !(obj instanceof Node<?, ?>)) {
				return false;
			}
			return getKey().equals(((Node<ObjectType, KeyType>) obj).getKey());
		}

		@Override
		public String toString() {
			return "Node: " + getKey();
		}

	}

	public List<KeyType> findShortestPath(ObjectType nodeFrom, ObjectType nodeTo,
			Function<ObjectType, KeyType> keySupplier, Function<ObjectType, Collection<ObjectType>> neigborsSupplier) {
		return findShortestPathInternalTwoDirection(createNode(nodeFrom, keySupplier, neigborsSupplier),
				createNode(nodeTo, keySupplier, neigborsSupplier));
	}

	private Node<ObjectType, KeyType> createNode(ObjectType node, Function<ObjectType, KeyType> keySupplier,
			Function<ObjectType, Collection<ObjectType>> neigborsSupplier) {
		return new Node<ObjectType, KeyType>(node, keySupplier, neigborsSupplier);
	}

	@SuppressWarnings("unused")
	private List<KeyType> findShortestPathInternalOneDirection(Node<ObjectType, KeyType> nodeFrom,
			Node<ObjectType, KeyType> nodeTo) {
		HeapComparable<Pair<Node<ObjectType, KeyType>, Double>> fromArea = new HeapComparable<>();
		Map<Node<ObjectType, KeyType>, Pair<Node<ObjectType, KeyType>, Double>> fromAreaDistance = new HashMap<>();
		fromArea.put(new Pair<>(nodeFrom, 0.0));
		fromAreaDistance.put(nodeFrom, new Pair<>(null, 0.0));
		while (fromArea.size() > 0 && !fromAreaDistance.containsKey(nodeTo)) {
			stepWaveOneDirection(fromArea, fromAreaDistance);
		}
		return extractPath(nodeTo, fromAreaDistance);
	}

	private void stepWaveOneDirection(HeapComparable<Pair<Node<ObjectType, KeyType>, Double>> heap,
			Map<Node<ObjectType, KeyType>, Pair<Node<ObjectType, KeyType>, Double>> visited) {
		Pair<Node<ObjectType, KeyType>, Double> p = heap.poll();
		for (Node<ObjectType, KeyType> n : p.first.getNeighbors()) {
			if (!visited.containsKey(n)) {
				// now distance between all nodes = 1
				double distance = p.second + 1;
				heap.put(new Pair<>(n, distance));
				visited.put(n, new Pair<>(p.first, distance));
			}
		}
	}

	private List<KeyType> extractPath(Node<ObjectType, KeyType> nodeTo,
			Map<Node<ObjectType, KeyType>, Pair<Node<ObjectType, KeyType>, Double>> fromAreaDistance) {
		List<KeyType> path;
		Pair<Node<ObjectType, KeyType>, Double> destNode = fromAreaDistance.get(nodeTo);
		if (destNode != null) {
			// found
			path = new LinkedList<>();
			path.add(0, nodeTo.getKey());
			while (destNode != null && destNode.first != null) {
				path.add(0, destNode.first.getKey());
				destNode = fromAreaDistance.get(destNode.first);
			}
		} else {
			path = Collections.emptyList();
		}
		return path;
	}

	private List<KeyType> findShortestPathInternalTwoDirection(Node<ObjectType, KeyType> nodeFrom,
			Node<ObjectType, KeyType> nodeTo) {
		if (nodeFrom.equals(nodeTo)) {
			List<KeyType> list = new ArrayList<>();
			list.add(nodeFrom.getKey());
			return list;
		}

		HeapComparable<Pair<Node<ObjectType, KeyType>, Double>> fromArea = new HeapComparable<>();
		Map<Node<ObjectType, KeyType>, Pair<Node<ObjectType, KeyType>, Double>> fromAreaDistance = new HashMap<>();
		fromArea.put(new Pair<>(nodeFrom, 0.0));
		fromAreaDistance.put(nodeFrom, new Pair<>(null, 0.0));

		HeapComparable<Pair<Node<ObjectType, KeyType>, Double>> toArea = new HeapComparable<>();
		Map<Node<ObjectType, KeyType>, Pair<Node<ObjectType, KeyType>, Double>> toAreaDistance = new HashMap<>();
		toArea.put(new Pair<>(nodeTo, 0.0));
		toAreaDistance.put(nodeTo, new Pair<>(null, 0.0));

		Node<ObjectType, KeyType> intersectioNode = null;
		while (intersectioNode == null && fromArea.size() > 0 && toArea.size() > 0) {
			intersectioNode = stepWaveTwoDirection(fromArea, fromAreaDistance, toAreaDistance);
			if (intersectioNode == null) {
				intersectioNode = stepWaveTwoDirection(toArea, toAreaDistance, fromAreaDistance);
			}
		}
		if (fromAreaDistance.containsKey(nodeTo)) {
			return extractPath(nodeTo, fromAreaDistance);
		} else {
			List<KeyType> pathFromStart = extractPath(intersectioNode, fromAreaDistance);
			List<KeyType> pathFromEndToIntersectNode = extractPath(intersectioNode, toAreaDistance);
			for (int i = pathFromEndToIntersectNode.size() - 2; i >= 0; i--) {
				pathFromStart.add(pathFromEndToIntersectNode.get(i));
			}
			return pathFromStart;
		}
	}

	private Node<ObjectType, KeyType> stepWaveTwoDirection(HeapComparable<Pair<Node<ObjectType, KeyType>, Double>> heap,
			Map<Node<ObjectType, KeyType>, Pair<Node<ObjectType, KeyType>, Double>> visited,
			Map<Node<ObjectType, KeyType>, Pair<Node<ObjectType, KeyType>, Double>> anotherVisited) {
		Pair<Node<ObjectType, KeyType>, Double> p = heap.poll();
		for (Node<ObjectType, KeyType> n : p.first.getNeighbors()) {
			if (!visited.containsKey(n)) {
				// now distance between all nodes = 1
				double distance = p.second + 1;
				heap.put(new Pair<>(n, distance));
				visited.put(n, new Pair<>(p.first, distance));
				if (anotherVisited.containsKey(n)) {
					return n; // found intersection, end of search
				}
			}
		}
		return null;
	}

}
