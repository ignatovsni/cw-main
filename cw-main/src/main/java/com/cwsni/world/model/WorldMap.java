package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cwsni.world.model.data.DataWorldMap;
import com.cwsni.world.model.events.Event;
import com.cwsni.world.services.algorithms.Node;

public class WorldMap {

	private List<Province> provinces;
	private Map<Integer, Province> mapProvById;
	private Game game;
	private Set<ProvinceBorder> countriesBorders;
	private int countriesBordersWasRefreshedAtTurn = -1;

	public List<Province> getProvinces() {
		return provinces;
	}

	public Province findProvById(Integer id) {
		return mapProvById.get(id);
	}

	Game getGame() {
		return game;
	}

	public void remove(Event e) {
		getProvinces().forEach(p -> p.removeEvent(e));
	}

	public void buildFrom(Game game, DataWorldMap map) {
		this.game = game;
		provinces = new ArrayList<>(map.getProvinces().size());
		mapProvById = new HashMap<>(map.getProvinces().size());
		map.getProvinces().stream().forEach(dp -> {
			Province p = new Province();
			provinces.add(p);
			mapProvById.put(dp.getId(), p);
		});
		map.getProvinces().stream().forEach(dp -> {
			mapProvById.get(dp.getId()).buildFrom(this, dp);
		});

	}

	/**
	 * Find distance quickly just for comparison
	 * 
	 */
	public double findRelativeDistanceBetweenProvs(int provId1, int provId2) {
		return findRelativeDistanceBetweenProvs(findProvById(provId1), findProvById(provId2));
	}

	private double findRelativeDistanceBetweenProvs(Province p1, Province p2) {
		return Math.pow(p1.getCenter().getX() - p2.getCenter().getX(), 2)
				+ Math.pow(p1.getCenter().getY() - p2.getCenter().getY(), 2);
	}

	private Set<ProvinceBorder> refreshCountriesBorders() {
		/*
		 * We can remember if province changes owner and check only such provinces with
		 * neighbors. It will improve performance.
		 */
		Set<ProvinceBorder> borders = new HashSet<>();
		Map<Integer, Set<Integer>> bordersNeighbors = new HashMap<>();
		game.getCountries().forEach(c -> {
			c.getProvinces().forEach(p -> {
				Set<Integer> pN = bordersNeighbors.get(p.getId());
				if (pN == null) {
					pN = new HashSet<>();
					bordersNeighbors.put(p.getId(), pN);
				}
				Set<Integer> pNeighbors = pN;
				p.getNeighbors().stream().filter(n -> !ComparisonTool.isEqual(p.getCountryId(), n.getCountryId())
						&& !pNeighbors.contains(n.getId())).forEach(n -> {
							Set<Integer> nNeighbors = bordersNeighbors.get(n.getId());
							if (nNeighbors == null) {
								nNeighbors = new HashSet<>();
								bordersNeighbors.put(n.getId(), nNeighbors);
							}
							pNeighbors.add(n.getId());
							nNeighbors.add(p.getId());
							borders.add(new ProvinceBorder(n.getId(), p.getId()));
						});
			});
		});
		return borders;
	}

	public Set<ProvinceBorder> getCountriesBorders() {
		if (countriesBordersWasRefreshedAtTurn != game.getTurn().getTurn()) {
			countriesBorders = refreshCountriesBorders();
			countriesBordersWasRefreshedAtTurn = game.getTurn().getTurn();
		}
		return countriesBorders;
	}

	public double findDistanceBetweenProvs(int fromId, int toId) {
		return findShortestPath(fromId, toId).size() - 1;
	}

	public List<Object> findShortestPath(int fromId, int toId) {
		return game.getGameAlgorithms().findShortestPath(new ProvinceNodeWrapper(findProvById(fromId)),
				new ProvinceNodeWrapper(findProvById(toId)));
	}

	private class ProvinceNodeWrapper extends Node {

		private Province p;

		ProvinceNodeWrapper(Province p) {
			this.p = p;
		}

		@Override
		public Object getKey() {
			return p.getId();
		}

		@Override
		public Collection<Node> getNeighbors() {
			return p.getNeighbors().stream().map(n -> new ProvinceNodeWrapper(n)).collect(Collectors.toList());
		}

	}

}
