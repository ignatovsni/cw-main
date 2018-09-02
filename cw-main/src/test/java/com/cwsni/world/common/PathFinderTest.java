package com.cwsni.world.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.WorldMap;
import com.cwsni.world.services.GameGenerator;

public class PathFinderTest {

	private Game game;

	@Before
	public void init() {
		GameGenerator gg = new GameGenerator();
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
		List<? extends Object> path = findShortestPath(map, 0, 1);
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

	private List<? extends Object> findShortestPath(WorldMap map, int fromId, int toId) {
		return map.findShortestPath(fromId, toId, null);
	}

}