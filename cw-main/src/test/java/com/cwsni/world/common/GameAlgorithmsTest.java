package com.cwsni.world.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.cwsni.world.common.algorithms.GameAlgorithms;
import com.cwsni.world.common.algorithms.Node;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.WorldMap;
import com.cwsni.world.model.data.GameParams;

public class GameAlgorithmsTest {

	private Game game;
	private GameAlgorithms gameAlg;

	@Before
	public void init() {
		GameGenerator gg = new GameGenerator();
		GameParams gameParams = new GameParams();
		gameParams.setSeed(System.currentTimeMillis());
		gameParams.setRows(10);
		gameParams.setColumns(10);
		gameParams.setOceanPercent(0);
		game = gg.createGame(gameParams);
		gameAlg = new GameAlgorithms();
	}

	@Test
	public void testPath() {
		WorldMap map = game.getMap();
		assertEquals(100, map.getProvinces().size());
		List<Object> path = findShortestPath(map, 0, 1);
		assertEquals(Arrays.asList(0, 1), path);

		path = findShortestPath(map, 5, 0);
		assertEquals(Arrays.asList(5, 4, 3, 2, 1, 0), path);

		path = findShortestPath(map, 3, 10);
		assertEquals(Arrays.asList(3, 2, 11, 10), path);

		path = findShortestPath(map, 0, 5);
		assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), path);

		map.getProvinces().get(2).getNeighbors().clear();
		path = findShortestPath(map, 0, 5);
		assertEquals(Arrays.asList(0, 1, 11, 12, 13, 14, 5), path);

		map.getProvinces().get(12).getNeighbors().clear();
		path = findShortestPath(map, 0, 5);
		assertEquals(Arrays.asList(0, 1, 11, 22, 23, 13, 4, 5), path);

		// System.out.println(path);
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

	private List<Object> findShortestPath(WorldMap map, int fromId, int toId) {
		return gameAlg.findShortestPath(new ProvinceNodeWrapper(map.findProvById(fromId)),
				new ProvinceNodeWrapper(map.findProvById(toId)));
	}

}
