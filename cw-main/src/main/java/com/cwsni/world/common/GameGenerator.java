package com.cwsni.world.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.GameParams;
import com.cwsni.world.model.Population;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.TerrainType;
import com.cwsni.world.model.WorldMap;

@Component
public class GameGenerator {

	public Game createTestGame() {
		GameParams gameParams = new GameParams();
		gameParams.setSeed(System.currentTimeMillis());
		gameParams.setRows(30);
		gameParams.setColumns(30);
		// gameParams.setSoilAreaCorePointsPerProvinces(0.03);
		// gameParams.setSoilFertilityCorePointsPerProvinces(0.1);
		return createTestGame(gameParams);
	}

	public Game createTestGame(GameParams gameParams) {
		Game game = new Game(gameParams);
		createMap(game);
		createTerrain(game);
		fillSoil(game);
		fillPopulation(game);
		game.postGenerate();
		return game;
	}

	private void createMap(Game game) {
		WorldMap map = createMap(game.getGameParams());
		game.setMap(map);
	}

	private WorldMap createMap(GameParams gParams) {
		double xStep = 1.75;
		double yStep = 1.51;
		WorldMap map = new WorldMap();
		double x = gParams.getProvinceRadius();
		double y = gParams.getProvinceRadius();
		int idx = 0;
		for (int row = 0; row < gParams.getRows(); row++) {
			x = gParams.getProvinceRadius() * xStep / 2 * (row % 2);
			for (int column = 0; column < gParams.getColumns(); column++) {
				Province province = new Province(idx++, (int) x, (int) y);
				province.setCoordAsHex(column, row);
				map.addProvince(province);
				setLinks(province, map, row, column, gParams.getColumns());
				x += gParams.getProvinceRadius() * xStep;
			}
			y += gParams.getProvinceRadius() * yStep;
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

	private void createTerrain(Game game) {
		GameParams gParams = game.getGameParams();
		assert (gParams.getOceanProcent() < 1 && gParams.getOceanProcent() >= 0);
		WorldMap map = game.getMap();
		int needTerrainProvs = (int) (map.getProvinces().size() * (1 - gParams.getOceanProcent()));
		List<Province> terrain = new ArrayList<>(needTerrainProvs);
		Set<Integer> terrainIds = new HashSet<>(needTerrainProvs);
		Province prov = null;
		int corePoints = Math.min(gParams.getTerrainCorePoints(), map.getProvinces().size());
		while (terrain.size() < corePoints) {
			prov = map.getProvinces().get(gParams.getRandom().nextInt(map.getProvinces().size()));
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
			int idx = gParams.getRandom().nextInt(terrain.size());
			Province nProv = terrain.get(idx);
			int neighborIdx = gParams.getRandom().nextInt(nProv.getNeighbors().size());
			prov = nProv.getNeighbors().get(neighborIdx);
		}
	}

	private void setTerrainType(List<Province> terrain, Set<Integer> terrainIds, Province prov,
			TerrainType terrainType) {
		prov.setTerrainType(terrainType);
		terrain.add(prov);
		terrainIds.add(prov.getId());
	}

	private void fillSoil(Game game) {
		GameParams gParams = game.getGameParams();
		WorldMap map = game.getMap();
		List<Province> terrain = new ArrayList<>();
		// setup min soil attributes and find all terrain provinces
		map.getProvinces().stream().filter(p -> p.getTerrainType().isSoilPossible()).forEach(p -> {
			p.setSoilArea(gParams.getMinSoilArea());
			p.setSoilFertility(gParams.getMinSoilFertility());
			terrain.add(p);
		});
		fillSoilArea(game, terrain);
		fillSoilFertility(game, terrain);
	}

	private void fillSoilArea(Game game, List<Province> terrain) {
		GameParams gParams = game.getGameParams();
		// setup core provs
		int corePoints = Math.min(gParams.getSoilAreaCorePoints(), terrain.size());
		Set<Integer> increasedIds = new HashSet<>(corePoints);
		while (increasedIds.size() < corePoints) {
			Province p = terrain.get(gParams.getRandom().nextInt(terrain.size()));
			if (!increasedIds.contains(p.getId())) {
				double maxSA = gParams.getMinSoilArea()
						+ (gParams.getMaxSoilArea() - gParams.getMinSoilArea()) * (gParams.getRandom().nextDouble());
				p.setSoilArea((int) maxSA);
				increasedIds.add(p.getId());
			}
		}
		// increase soil quality for neighbors
		Queue<Province> queueProvs = new LinkedBlockingQueue<Province>();
		increasedIds.forEach(id -> queueProvs.add(game.getMap().findProvById(id)));
		while (!queueProvs.isEmpty()) {
			Province p = queueProvs.poll();
			p.getNeighbors().stream()
					.filter(n -> n.getTerrainType().isSoilPossible() && !increasedIds.contains(n.getId()))
					.forEach(n -> {
						n.setSoilArea((p.getSoilArea() * gParams.getFractionOfMaxSoilArea() + n.getSoilArea())
								/ (gParams.getFractionOfMaxSoilArea() + 1));
						queueProvs.add(n);
						increasedIds.add(n.getId());
					});
		}
	}

	private void fillSoilFertility(Game game, List<Province> terrain) {
		GameParams gParams = game.getGameParams();
		// setup core provs
		int corePoints = Math.min(gParams.getSoilFertilityCorePoints(), terrain.size());
		Set<Integer> increasedIds = new HashSet<>(corePoints);
		while (increasedIds.size() < corePoints) {
			Province p = terrain.get(gParams.getRandom().nextInt(terrain.size()));
			if (!increasedIds.contains(p.getId())) {
				double maxSF = gParams.getMinSoilFertility()
						+ (gParams.getMaxSoilFertility() - gParams.getMinSoilFertility())
								* (gParams.getRandom().nextDouble());
				p.setSoilFertility(DataFormatter.doubleWith3points(maxSF));
				increasedIds.add(p.getId());
			}
		}
		// increase soil quality for neighbors
		Queue<Province> queueProvs = new LinkedBlockingQueue<Province>();
		increasedIds.forEach(id -> queueProvs.add(game.getMap().findProvById(id)));
		while (!queueProvs.isEmpty()) {
			Province p = queueProvs.poll();
			p.getNeighbors().stream()
					.filter(n -> n.getTerrainType().isSoilPossible() && !increasedIds.contains(n.getId()))
					.forEach(n -> {
						double fertility = (p.getSoilFertility() * gParams.getFractionOfMaxSoilFertility()
								+ n.getSoilFertility()) / (gParams.getFractionOfMaxSoilFertility() + 1);
						n.setSoilFertility(DataFormatter.doubleWith3points(fertility));
						queueProvs.add(n);
						increasedIds.add(n.getId());
					});
		}
		// process poles
		terrain.forEach(p -> {
			double distanceToPole = Math.min(p.getCoordAsHex().getY() + 1, gParams.getRows() - p.getCoordAsHex().getY())
					/ (double) gParams.getRows();
			if (distanceToPole < gParams.getDecreaseSoilFertilityAtPoles()) {
				double fertilityDecrease = 1 - (gParams.getDecreaseSoilFertilityAtPoles() - distanceToPole)
						/ gParams.getDecreaseSoilFertilityAtPoles();
				p.setSoilFertility(p.getSoilFertility() * fertilityDecrease);
			}
		});
	}

	private void fillPopulation(Game game) {
		GameParams gParams = game.getGameParams();
		game.getMap().getProvinces().stream().filter(p -> (p.getTerrainType().isPopulationPossible())).forEach(p -> {
			if (p.getSoilFertility() >= 1) {
				Population pop = new Population();
				pop.setAmount((int) (p.getSoilQuality() * gParams.getMapPopulationAtStart()));
				p.getPopulation().clear();
				p.getPopulation().add(pop);
			}
		});
	}

	public Game createEmptyGame() {
		GameParams gParams = new GameParams();
		gParams.setRows(0);
		gParams.setColumns(0);
		Game game = new Game(gParams);
		createMap(game);
		game.postGenerate();
		return game;
	}

}
