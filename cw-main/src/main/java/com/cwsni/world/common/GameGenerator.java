package com.cwsni.world.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.data.DataWorldMap;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.data.Turn;

@Component
public class GameGenerator {

	private class TempData {
		Map<Integer, DataProvince> provByIds = new HashMap<>();
		Set<Integer> coreTerrainIds = new HashSet<>();
	}

	public Game createTestGame() {
		GameParams gameParams = new GameParams();
		gameParams.setSeed(System.currentTimeMillis());
		gameParams.setRows(30);
		gameParams.setColumns(30);
		return createGame(gameParams);
	}

	public Game createGame(GameParams gameParams) {
		TempData tData = new TempData();
		DataGame dataGame = new DataGame(gameParams);
		dataGame.setTurn(new Turn(0));
		createMap(dataGame, tData);
		createTerrain(dataGame, tData);
		fillSoil(dataGame, tData);
		fillPopulation(dataGame);
		Game game = new Game();
		game.buildFrom(dataGame);
		return game;
	}

	private void createMap(DataGame game, TempData tData) {
		DataWorldMap map = createMap(game.getGameParams(), tData);
		game.setMap(map);
	}

	private DataWorldMap createMap(GameParams gParams, TempData tData) {
		double xStep = 1.75;
		double yStep = 1.51;
		DataWorldMap map = new DataWorldMap();
		double x = gParams.getProvinceRadius();
		double y = gParams.getProvinceRadius();
		int idx = 0;
		for (int row = 0; row < gParams.getRows(); row++) {
			x = gParams.getProvinceRadius() * xStep / 2 * (row % 2);
			for (int column = 0; column < gParams.getColumns(); column++) {
				DataProvince province = new DataProvince(idx++, (int) x, (int) y);
				map.addProvince(province);
				tData.provByIds.put(province.getId(), province);
				setLinks(tData, province, map, row, column, gParams.getColumns());
				x += gParams.getProvinceRadius() * xStep;
			}
			y += gParams.getProvinceRadius() * yStep;
		}
		return map;
	}

	private void setLinks(TempData tData, DataProvince p, DataWorldMap map, int y, int x, int columns) {
		int id = p.getId();
		if (x > 0) {
			DataProvince leftProv = tData.provByIds.get(id - 1);
			p.getNeighbors().add(leftProv.getId());
			leftProv.getNeighbors().add(p.getId());
		}
		if (y > 0) {
			if (y % 2 == 1) {
				DataProvince leftProv = tData.provByIds.get((y - 1) * columns + x);
				p.getNeighbors().add(leftProv.getId());
				leftProv.getNeighbors().add(p.getId());
				if (x < (columns - 1)) {
					DataProvince rightProv = tData.provByIds.get((y - 1) * columns + x + 1);
					p.getNeighbors().add(rightProv.getId());
					rightProv.getNeighbors().add(p.getId());
				}
			} else {
				DataProvince rightProv = tData.provByIds.get((y - 1) * columns + x);
				p.getNeighbors().add(rightProv.getId());
				rightProv.getNeighbors().add(p.getId());
				if (x > 0) {
					DataProvince leftProv = tData.provByIds.get((y - 1) * columns + x - 1);
					p.getNeighbors().add(leftProv.getId());
					leftProv.getNeighbors().add(p.getId());
				}
			}
		}
	}

	private void createTerrain(DataGame game, TempData tData) {
		GameParams gParams = game.getGameParams();
		assert (gParams.getOceanPercent() < 1 && gParams.getOceanPercent() >= 0);
		DataWorldMap map = game.getMap();
		int needTerrainProvs = (int) (map.getProvinces().size() * (1 - gParams.getOceanPercent()));
		List<DataProvince> terrain = new ArrayList<>(needTerrainProvs);
		Set<Integer> terrainIds = new HashSet<>(needTerrainProvs);
		DataProvince prov = null;
		int corePoints = Math.min(gParams.getTerrainCorePoints(), map.getProvinces().size());
		while (terrain.size() < corePoints) {
			prov = map.getProvinces().get(gParams.getRandom().nextInt(map.getProvinces().size()));
			if (!terrainIds.contains(prov.getId())) {
				setTerrainType(terrain, terrainIds, prov, TerrainType.GRASSLAND);
			}
		}
		tData.coreTerrainIds = new HashSet<>(terrainIds);
		while (terrain.size() < needTerrainProvs) {
			if (!terrainIds.contains(prov.getId())) {
				setTerrainType(terrain, terrainIds, prov, TerrainType.GRASSLAND);
				// It is possible to remove provinces which have all non-ocean neighbors. It
				// will make generation faster.
			}
			int idx = gParams.getRandom().nextInt(terrain.size());
			DataProvince nProv = terrain.get(idx);
			int neighborIdx = gParams.getRandom().nextInt(nProv.getNeighbors().size());
			prov = tData.provByIds.get(nProv.getNeighbors().get(neighborIdx));
		}
	}

	private void setTerrainType(List<DataProvince> terrain, Set<Integer> terrainIds, DataProvince prov,
			TerrainType terrainType) {
		prov.setTerrainType(terrainType);
		terrain.add(prov);
		terrainIds.add(prov.getId());
	}

	private void fillSoil(DataGame game, TempData tData) {
		GameParams gParams = game.getGameParams();
		DataWorldMap map = game.getMap();
		List<DataProvince> terrain = new ArrayList<>();
		// setup min soil attributes and find all terrain provinces
		map.getProvinces().stream().filter(p -> p.getTerrainType().isSoilPossible()).forEach(p -> {
			p.setSoilArea(gParams.getMinSoilArea());
			p.setSoilFertility(gParams.getMinSoilFertility());
			terrain.add(p);
		});
		fillSoilArea(game, tData, terrain);
		fillSoilFertility(game, tData, terrain);
	}

	private void fillSoilArea(DataGame game, TempData tData, List<DataProvince> terrain) {
		GameParams gParams = game.getGameParams();
		// setup core provs
		int corePoints = Math.min(gParams.getSoilAreaCorePoints(), terrain.size());
		Set<Integer> increasedIds = new HashSet<>(corePoints);
		while (increasedIds.size() < corePoints) {
			DataProvince p = terrain.get(gParams.getRandom().nextInt(terrain.size()));
			if (!increasedIds.contains(p.getId())) {
				double maxSA = gParams.getMinSoilArea()
						+ (gParams.getMaxSoilArea() - gParams.getMinSoilArea()) * (gParams.getRandom().nextDouble());
				p.setSoilArea((int) maxSA);
				increasedIds.add(p.getId());
			}
		}
		// increase soil quality for neighbors
		Queue<DataProvince> queueProvs = new LinkedBlockingQueue<DataProvince>();
		increasedIds.forEach(id -> queueProvs.add(tData.provByIds.get(id)));
		while (!queueProvs.isEmpty()) {
			DataProvince p = queueProvs.poll();
			p.getNeighbors().stream().map(nID -> tData.provByIds.get(nID))
					.filter(n -> n.getTerrainType().isSoilPossible() && !increasedIds.contains(n.getId()))
					.forEach(n -> {
						n.setSoilArea((p.getSoilArea() * gParams.getFractionOfMaxSoilArea() + n.getSoilArea())
								/ (gParams.getFractionOfMaxSoilArea() + 1));
						queueProvs.add(n);
						increasedIds.add(n.getId());
					});
		}
	}

	private void fillSoilFertility(DataGame game, TempData tData, List<DataProvince> terrain) {
		GameParams gParams = game.getGameParams();
		// setup core provs
		// use core terrains to support soil and population on all "continents"
		int corePoints = Math.min(gParams.getSoilFertilityCorePoints(), terrain.size());
		Set<Integer> increasedIds = new HashSet<>(corePoints);
		tData.coreTerrainIds.forEach(coreId -> {
			DataProvince p = tData.provByIds.get(coreId);
			addSoilFertility(gParams, increasedIds, p);
			if (p.getSoilFertility() < gParams.getMinSoilFertilityToStartPopulation()) {
				p.setSoilFertility(gParams.getMinSoilFertilityToStartPopulation() + 0.01);
			}
		});
		while (increasedIds.size() < corePoints) {
			DataProvince p = terrain.get(gParams.getRandom().nextInt(terrain.size()));
			addSoilFertility(gParams, increasedIds, p);
		}
		// increase soil quality for neighbors
		Queue<DataProvince> queueProvs = new LinkedBlockingQueue<DataProvince>();
		increasedIds.forEach(id -> queueProvs.add(tData.provByIds.get(id)));
		while (!queueProvs.isEmpty()) {
			DataProvince p = queueProvs.poll();
			p.getNeighbors().stream().map(nID -> tData.provByIds.get(nID))
					.filter(n -> n.getTerrainType().isSoilPossible() && !increasedIds.contains(n.getId()))
					.forEach(n -> {
						double fertility = (p.getSoilFertility() * gParams.getFractionOfMaxSoilFertility()
								+ n.getSoilFertility()) / (gParams.getFractionOfMaxSoilFertility() + 1);
						n.setSoilFertility(DataFormatter.doubleWith2points(fertility));
						queueProvs.add(n);
						increasedIds.add(n.getId());
					});
		}
		// process poles
		if (!terrain.isEmpty()) {
			int minY = terrain.stream().mapToInt(p -> p.getCenter().getY()).min().getAsInt();
			int maxY = terrain.stream().mapToInt(p -> p.getCenter().getY()).max().getAsInt();
			terrain.stream().filter(p -> !tData.coreTerrainIds.contains(p.getId())).forEach(p -> {
				double distanceToPole = (double) (1
						+ Math.min(p.getCenter().getY() - minY, maxY - p.getCenter().getY())) / (maxY - minY);
				if (distanceToPole < gParams.getDecreaseSoilFertilityAtPoles()) {
					double fertilityDecrease = 1 - (gParams.getDecreaseSoilFertilityAtPoles() - distanceToPole)
							/ gParams.getDecreaseSoilFertilityAtPoles() / 2;
					p.setSoilFertility(DataFormatter.doubleWith2points(p.getSoilFertility() * fertilityDecrease));
				}
			});
		}
	}

	private void addSoilFertility(GameParams gParams, Set<Integer> increasedIds, DataProvince p) {
		if (!increasedIds.contains(p.getId())) {
			double maxSF = gParams.getMinSoilFertility()
					+ (gParams.getMaxSoilFertility() - gParams.getMinSoilFertility())
							* (gParams.getRandom().nextDouble());
			p.setSoilFertility(DataFormatter.doubleWith2points(maxSF));
			increasedIds.add(p.getId());
		}
	}

	private void fillPopulation(DataGame game) {
		GameParams gParams = game.getGameParams();
		game.getMap().getProvinces().stream().filter(p -> (p.getTerrainType().isPopulationPossible())).forEach(p -> {
			if (p.getSoilFertility() >= gParams.getMinSoilFertilityToStartPopulation()) {
				DataPopulation pop = new DataPopulation();
				pop.setAmount((int) (p.getSoilFertility() * gParams.getPopulationAtStart()));
				p.getPopulation().clear();
				p.getPopulation().add(pop);
			}
		});
	}

	public Game createEmptyGame() {
		GameParams gameParams = new GameParams();
		gameParams.setSeed(System.currentTimeMillis());
		gameParams.setRows(0);
		gameParams.setColumns(0);
		return createGame(gameParams);
	}

}
