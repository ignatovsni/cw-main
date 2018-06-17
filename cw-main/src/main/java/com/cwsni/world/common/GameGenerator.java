package com.cwsni.world.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cwsni.world.model.Game;
import com.cwsni.world.model.Population;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.TerrainType;
import com.cwsni.world.model.WorldMap;

@Component
public class GameGenerator {

	public Game createTestGame() {
		return createTestGame(30, 30, 30);
	}

	public Game createTestGame(int rows, int columns, int provinceRadius) {
		Game game = new Game();
		createMap(rows, columns, provinceRadius, game);
		createTerrain(game, System.currentTimeMillis(), 10, 0.4);
		fillPopulation(game);
		game.postGenerate();
		return game;
	}

	private void createMap(int rows, int columns, int provinceRadius, Game game) {
		WorldMap map = createMap(rows, columns, provinceRadius);
		game.setMap(map);
	}

	private WorldMap createMap(int rows, int columns, double provinceRadius) {
		double xStep = 1.75;
		double yStep = 1.51;
		WorldMap map = new WorldMap(provinceRadius);
		double x = provinceRadius;
		double y = provinceRadius;
		int idx = 0;
		for (int row = 0; row < rows; row++) {
			x = provinceRadius * xStep / 2 * (row % 2);
			for (int column = 0; column < columns; column++) {
				Province province = new Province(idx++, (int) x, (int) y);
				map.addProvince(province);
				setLinks(province, map, row, column, columns);
				x += provinceRadius * xStep;
			}
			y += provinceRadius * yStep;
		}
		return map;
	}

	private static void setLinks(Province p, WorldMap map, int y, int x, int columns) {
		int id = p.getId();
		if (x > 0) {
			Province leftProv = map.findProvById(id - 1);
			p.getNeighbors().add(leftProv);
			leftProv.getNeighbors().add(p);
		}
		if (y > 0) {
			if (y % 2 == 1) {
				Province leftProv = map.findProvById((y - 1) * columns + x);
				p.getNeighbors().add(leftProv);
				leftProv.getNeighbors().add(p);
				if (x < (columns - 1)) {
					Province rightProv = map.findProvById((y - 1) * columns + x + 1);
					p.getNeighbors().add(rightProv);
					rightProv.getNeighbors().add(p);
				}
			} else {
				Province rightProv = map.findProvById((y - 1) * columns + x);
				p.getNeighbors().add(rightProv);
				rightProv.getNeighbors().add(p);
				if (x > 0) {
					Province leftProv = map.findProvById((y - 1) * columns + x - 1);
					p.getNeighbors().add(leftProv);
					leftProv.getNeighbors().add(p);
				}
			}
		}
	}

	private void createTerrain(Game game, long seed, long corePoints, double oceanProcent) {
		assert (oceanProcent < 1 && oceanProcent >= 0);
		WorldMap map = game.getMap();
		int needTerrainProvs = (int) (map.getProvinces().size() * (1 - oceanProcent));
		List<Province> terrain = new ArrayList<>(needTerrainProvs);
		Set<Integer> terrainIds = new HashSet<>(needTerrainProvs);
		Random random = new Random(seed);
		Province prov = null;
		corePoints = Math.min(corePoints, map.getProvinces().size());
		while (terrain.size() < corePoints) {
			prov = map.getProvinces().get(random.nextInt(map.getProvinces().size()));
			if (!terrainIds.contains(prov.getId())) {
				setTerrainType(terrain, terrainIds, prov, TerrainType.GRASSLAND);
			}
		}
		while (terrain.size() < needTerrainProvs) {
			if (!terrainIds.contains(prov.getId())) {
				setTerrainType(terrain, terrainIds, prov, TerrainType.GRASSLAND);
				// It is possible to remove provinces which have all non-ocean neighbors. It
				// will make generation faster.
			}
			int idx = random.nextInt(terrain.size());
			Province nProv = terrain.get(idx);
			int neighborIdx = random.nextInt(nProv.getNeighbors().size());
			prov = nProv.getNeighbors().get(neighborIdx);
		}
	}

	private void setTerrainType(List<Province> terrain, Set<Integer> terrainIds, Province prov,
			TerrainType terrainType) {
		prov.setTerrainType(terrainType);
		terrain.add(prov);
		terrainIds.add(prov.getId());
	}

	private void fillPopulation(Game game) {
		game.getMap().getProvinces().stream().filter(p -> !TerrainType.OCEAN.equals(p.getTerrainType())).forEach(p -> {
			Population pop = new Population();
			// pop.setAmount(new Random().nextInt(1000));
			pop.setAmount(p.getId() * 100);
			p.getPopulation().clear();
			p.getPopulation().add(pop);
		});
	}

	public Game createEmptyGame() {
		Game game = new Game();
		createMap(0, 0, 30, game);
		game.postGenerate();
		return game;
	}

}
