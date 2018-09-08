package com.cwsni.world.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.cwsni.world.algorithms.PathFinder;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.Province;
import com.cwsni.world.model.engine.ProvincePassabilityCriteria;
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
		assertEquals(Arrays.asList(3, 2, 11, 10).size(), path.size());

		path = findShortestPath(map, 0, 5);
		assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), path);

		clearNeighbors(map.getProvinces().get(2));
		path = findShortestPath(map, 0, 5);
		assertEquals(Arrays.asList(0, 1, 11, 12, 13, 14, 5).size(), path.size());

		clearNeighbors(map.getProvinces().get(12));
		path = findShortestPath(map, 0, 5);
		assertEquals(Arrays.asList(0, 1, 11, 22, 23, 13, 4, 5).size(), path.size());

		// System.out.println(path);
	}

	private void clearNeighbors(Province province) {
		province.getNeighbors().forEach(n -> n.getNeighbors().remove(province));
		province.getNeighbors().clear();
	}

	private List<? extends Object> findShortestPath(WorldMap map, int fromId, int toId) {
		ProvincePassabilityCriteria passability = new ProvincePassabilityCriteria() {
			@Override
			public boolean isPassable(Province p) {
				return true;
			}
		};
		return new PathFinder<Province, Integer>().findShortestPath(map.findProvinceById(fromId), map.findProvinceById(toId),
				p -> p.getId(),
				p -> p.getNeighbors().stream().filter(n -> passability.isPassable(n)).collect(Collectors.toList()));
	}

}
