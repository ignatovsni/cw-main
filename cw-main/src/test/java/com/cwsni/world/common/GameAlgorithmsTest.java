package com.cwsni.world.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cwsni.world.model.Game;
import com.cwsni.world.model.WorldMap;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.services.GameGenerator;
import com.cwsni.world.services.algorithms.GameAlgorithms;

public class GameAlgorithmsTest {

	private Game game;
	private GameAlgorithms gameAlgorithms;

	@Before
	public void init() {
		gameAlgorithms = new GameAlgorithms();
		GameGenerator gg = new GameGenerator();
		gg.setGameAlgorithms(gameAlgorithms);
		GameParams gameParams = new GameParams();
		gameParams.setSeed(System.currentTimeMillis());
		gameParams.setRows(10);
		gameParams.setColumns(10);
		gameParams.setOceanPercent(0);
		game = gg.createGame(gameParams);
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

	private List<Object> findShortestPath(WorldMap map, int fromId, int toId) {
		return map.findShortestPath(fromId, toId);
	}

}
