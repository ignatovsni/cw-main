package com.cwsni.world.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cwsni.world.common.algorithms.GameAlgorithms;
import com.cwsni.world.model.Game;
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
		List<Integer> path = gameAlg.findShortestPath(map, 0, 1);
		assertEquals(Arrays.asList(0, 1), path);

		path = gameAlg.findShortestPath(map, 5, 0);
		assertEquals(Arrays.asList(5, 4, 3, 2, 1, 0), path);

		path = gameAlg.findShortestPath(map, 3, 10);
		assertEquals(Arrays.asList(3, 2, 11, 10), path);

		path = gameAlg.findShortestPath(map, 0, 5);
		assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), path);

		map.getProvinces().get(2).getNeighbors().clear();
		path = gameAlg.findShortestPath(map, 0, 5);
		assertEquals(Arrays.asList(0, 1, 11, 12, 13, 14, 5), path);

		map.getProvinces().get(12).getNeighbors().clear();
		path = gameAlg.findShortestPath(map, 0, 5);
		assertEquals(Arrays.asList(0, 1, 11, 22, 23, 13, 4, 5), path);

		// System.out.println(path);
	}

}
