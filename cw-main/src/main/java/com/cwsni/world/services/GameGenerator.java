package com.cwsni.world.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.algorithms.PathFinder;
import com.cwsni.world.model.data.DataCulture;
import com.cwsni.world.model.data.DataFoodResource;
import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.data.DataScienceCollection;
import com.cwsni.world.model.data.DataTurn;
import com.cwsni.world.model.data.DataWorldMap;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.util.CwRandom;

@Component
public class GameGenerator {

	@Autowired
	private PlayerEventListener gameEventListener;

	private class TempData {
		Map<Integer, DataProvince> provByIds = new HashMap<>();
		Set<Integer> coreTerrainIds = new HashSet<>();
		Set<DataProvince> soilPossibleProvince = new HashSet<>();
	}

	public Game createTestGame() {
		GameParams gameParams = new GameParams();
		gameParams.setSeed(System.currentTimeMillis());
		gameParams.setRows(40);
		gameParams.setColumns(60);
		return createGame(gameParams);
	}

	public Game createGame(GameParams gameParams) {
		TempData tData = new TempData();
		DataGame dataGame = new DataGame(gameParams);
		dataGame.setTurn(new DataTurn());
		createMap(dataGame, tData);
		createTerrain(dataGame, tData);
		createMountains(dataGame, tData);
		int lastContinentId = defineContinents(dataGame, tData, tt -> !tt.isWater(), 0);
		int lastOceanId = defineContinents(dataGame, tData, tt -> tt.isWater(), lastContinentId);
		dataGame.getMap().setContinents(lastContinentId);
		dataGame.getMap().setOceans(lastOceanId - lastContinentId);
		fillSoil(dataGame, tData);
		fillPopulation(dataGame);
		fillInfrastructure(dataGame);
		Game game = new Game();
		game.buildFrom(dataGame, gameEventListener);
		gameParams.getRandom().resetWithSeed(gameParams.getSeed());
		return game;
	}

	private void createMap(DataGame game, TempData tData) {
		DataWorldMap map = createMap(game.getGameParams(), tData);
		game.setMap(map);
	}

	private DataWorldMap createMap(GameParams gParams, TempData tData) {
		DataWorldMap map = new DataWorldMap();
		double size = gParams.getProvinceRadius();
		double yStep = Math.sqrt(2);
		double xStep = Math.sqrt(3);
		double startCoord = size;
		double x = startCoord + size;
		double y = startCoord + size;
		int idx = 0;
		for (int row = 0; row < gParams.getRows(); row++) {
			x = startCoord + size * xStep / 2 * (row % 2);
			for (int column = 0; column < gParams.getColumns(); column++) {
				DataProvince province = new DataProvince(idx++, null, x, y);
				map.addProvince(province);
				tData.provByIds.put(province.getId(), province);
				setLinks(tData, province, map, row, column, gParams.getColumns());
				x += size * xStep;
			}
			y += size * yStep;
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

	private void createMountains(DataGame game, TempData tData) {
		GameParams gParams = game.getGameParams();
		int needMountains = (int) (gParams.getMountainsPerMapProvinces() * game.getMap().getProvinces().size());
		int maxChainLength = Math.max(gParams.getRows(), gParams.getColumns());
		createChainMountains(tData, gParams, needMountains, maxChainLength);
		createChainMountains(tData, gParams, needMountains, 1);

		int tryMakeReachable = 0;
		while (tryMakeReachable++ < game.getMap().getProvinces().size() / 100
				&& makeAllProvinceReachable(game, tData)) {
		}
	}

	/**
	 * Mountains can block some provinces, so we need to make all provinces
	 * reachable.
	 */
	private boolean makeAllProvinceReachable(DataGame game, TempData tData) {
		List<DataProvince> passableLand = game.getMap().getProvinces().stream()
				.filter(dp -> dp.getTerrainType().isPassable()).collect(Collectors.toList());
		DataProvince zeroProvince = passableLand.get(0);
		List<DataProvince> listReachableProvinces = new ArrayList<>(passableLand.size());
		Set<Integer> reachableProvincesIds = new HashSet<>();
		listReachableProvinces.add(zeroProvince);
		reachableProvincesIds.add(zeroProvince.getId());
		int idx = 0;
		while (idx < listReachableProvinces.size()) {
			for (int i = idx; i < listReachableProvinces.size(); i++, idx++) {
				DataProvince p = listReachableProvinces.get(i);
				p.getNeighbors().stream().filter(nId -> !reachableProvincesIds.contains(nId))
						.map(nId -> tData.provByIds.get(nId)).filter(n -> n.getTerrainType().isPassable())
						.forEach(n -> {
							listReachableProvinces.add(n);
							reachableProvincesIds.add(n.getId());
						});
			}
		}
		for (int i = 0; i < passableLand.size(); i++) {
			DataProvince prov = passableLand.get(i);
			if (!reachableProvincesIds.contains(prov.getId())) {
				List<DataProvince> path = new PathFinder<DataProvince, DataProvince>().findShortestPath(zeroProvince,
						prov, p -> p, p -> p.getNeighbors().stream().map(nId -> tData.provByIds.get(nId))
								.collect(Collectors.toList()));
				path.stream().filter(p -> p.getTerrainType().isMountain()).findFirst().get()
						.setTerrainType(TerrainType.GRASSLAND);
				return true;
			}
		}
		return false;
	}

	private void createChainMountains(TempData tData, GameParams gParams, int needMountains, int maxChainLength) {
		CwRandom rnd = gParams.getRandom();
		double probabilityOfPass = 0.2;
		double probabilityOfChangeDirection = 0.05;
		int alreadyCreatedMountains = 0;
		DataProvince lastMountainProv = null;
		int direction = 0;
		int chainLength = 0;
		int maxThisChainLength = 0;
		while (alreadyCreatedMountains < needMountains) {
			if (lastMountainProv == null) {
				int provId = rnd.nextInt(tData.provByIds.keySet().size());
				DataProvince prov = tData.provByIds.get(provId);
				if (prov.getTerrainType().isWater()) {
					continue;
				}
				// start new mountains chain
				lastMountainProv = prov;
				direction = rnd.nextInt(lastMountainProv.getNeighbors().size());
				chainLength = 0;
				maxThisChainLength = 1 + rnd.nextInt(
						1 + (int) (1.0 * maxChainLength * (needMountains - alreadyCreatedMountains) / needMountains));
				maxThisChainLength = Math.min(maxThisChainLength, maxChainLength);
			} else if (chainLength >= maxThisChainLength) {
				// end mountains chain
				lastMountainProv = null;
				alreadyCreatedMountains++;
			} else {
				direction = changeDirection(lastMountainProv, direction, 0);
				lastMountainProv = tData.provByIds.get(lastMountainProv.getNeighbors().get(direction));
			}
			if (lastMountainProv != null) {
				if (!lastMountainProv.getTerrainType().isWater()) {
					if (rnd.nextDouble() > probabilityOfPass) {
						configureMountainProvince(lastMountainProv);
					}
				}
				chainLength++;
				if (rnd.nextDouble() < probabilityOfChangeDirection) {
					direction = changeDirection(lastMountainProv, direction, 1);
				} else if (rnd.nextDouble() < probabilityOfChangeDirection) {
					direction = changeDirection(lastMountainProv, direction, -1);
				}

			}
		}
	}

	private void configureMountainProvince(DataProvince p) {
		p.setTerrainType(TerrainType.MOUNTAIN);
	}

	private int changeDirection(DataProvince p, int direction, int delta) {
		direction += delta;
		if (direction >= p.getNeighbors().size()) {
			direction = 0;
		} else if (direction < 0) {
			direction = p.getNeighbors().size() - 1;
		}
		return direction;
	}

	private int defineContinents(DataGame game, TempData tData, Function<TerrainType, Boolean> checkProvince,
			int currentContinentId) {
		for (DataProvince p : game.getMap().getProvinces()) {
			if (p.getContinentId() > 0 || !checkProvince.apply(p.getTerrainType())) {
				continue;
			}
			int continentId = ++currentContinentId;
			List<DataProvince> continent = new ArrayList<>();
			continent.add(p);
			p.setContinentId(continentId);
			int idx = 0;
			while (idx < continent.size()) {
				DataProvince prov = continent.get(idx);
				prov.getNeighbors().stream().map(nId -> tData.provByIds.get(nId))
						.filter(n -> n.getContinentId() == 0 && checkProvince.apply(n.getTerrainType())).forEach(n -> {
							n.setContinentId(continentId);
							continent.add(n);
						});
				idx++;
			}
		}
		return currentContinentId;
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
		// initialize area attributes and find all terrain provinces
		map.getProvinces().stream().filter(p -> p.getTerrainType().isSoilPossible()).forEach(p -> {
			p.setSize(100);
			DataFoodResource foodResource = new DataFoodResource();
			foodResource.setAmount(p.getSize() * gParams.getSoilAreaPerSize());
			foodResource.setQuality(gParams.getSoilFertilityAtStartBase());
			p.getFoodResources().add(foodResource);
			tData.soilPossibleProvince.add(p);
		});
		fillSoilFertility(game, tData);
	}

	private void fillSoilFertility(DataGame game, TempData tData) {
		GameParams gParams = game.getGameParams();
		fillSoilFertilityAtPoles(tData, gParams);
		Set<Integer> increasedIds = new HashSet<>();
		// setup core provs
		// use core terrains to support soil and population on all "continents"
		tData.coreTerrainIds.forEach(coreId -> {
			DataProvince p = tData.provByIds.get(coreId);
			if (p.getTerrainType().isSoilPossible()) {
				DataFoodResource foodResource = p.getFoodResources().get(0);
				foodResource.setAmount(foodResource.getAmount() * gParams.getSoilAreaAtStartCoeffForCoast());
				foodResource.setQuality(foodResource.getQuality() * gParams.getSoilFertilityAtStartCoeffForCoast());
				increasedIds.add(coreId);
			}
		});

		// increase soil fertility for coast
		Set<Integer> coastIds = new HashSet<>();
		tData.soilPossibleProvince.stream().filter(p -> p.getTerrainType().isSoilPossible()).forEach(p -> {
			if (p.getNeighbors().stream().map(nId -> tData.provByIds.get(nId)).filter(n -> n.getTerrainType().isWater())
					.findAny().isPresent()) {
				DataFoodResource foodResource = p.getFoodResources().get(0);
				foodResource.setAmount(foodResource.getAmount() * gParams.getSoilAreaAtStartCoeffForCoast());
				foodResource.setQuality(foodResource.getQuality() * gParams.getSoilFertilityAtStartCoeffForCoast());
				coastIds.add(p.getId());
			}
		});
		increasedIds.addAll(coastIds);

		// increase soil fertility for neighbors
		Queue<DataProvince> queueProvs = new LinkedBlockingQueue<DataProvince>();
		increasedIds.forEach(id -> queueProvs.add(tData.provByIds.get(id)));
		while (!queueProvs.isEmpty()) {
			DataProvince p = queueProvs.poll();
			p.getNeighbors().stream().map(nID -> tData.provByIds.get(nID))
					.filter(n -> n.getTerrainType().isSoilPossible() && !increasedIds.contains(n.getId()))
					.forEach(n -> {
						DataFoodResource provFoodResource = p.getFoodResources().get(0);
						DataFoodResource nFoodResource = n.getFoodResources().get(0);
						nFoodResource
								.setQuality((provFoodResource.getQuality() * gParams.getFractionOfBestFoodResource()
										+ nFoodResource.getQuality()) / (gParams.getFractionOfBestFoodResource() + 1));
						queueProvs.add(n);
						nFoodResource.setAmount((provFoodResource.getAmount() * gParams.getFractionOfBestFoodResource()
								+ nFoodResource.getAmount()) / (gParams.getFractionOfBestFoodResource() + 1));
						queueProvs.add(n);
						increasedIds.add(n.getId());
					});
		}
	}

	private void fillSoilFertilityAtPoles(TempData tData, GameParams gParams) {
		Set<DataProvince> terrain = tData.soilPossibleProvince;
		if (!terrain.isEmpty()) {
			double minY = terrain.stream().mapToDouble(p -> p.getCenter().getY()).min().getAsDouble();
			double maxY = terrain.stream().mapToDouble(p -> p.getCenter().getY()).max().getAsDouble();
			tData.soilPossibleProvince.stream().forEach(p -> {
				double distanceToPole = (1.0
						+ Math.min(p.getCenter().getY() - minY, maxY - p.getCenter().getY())) / (maxY - minY);
				if (distanceToPole < gParams.getDecreaseSoilFertilityAtPoles()) {
					DataFoodResource foodResource = p.getFoodResources().get(0);
					foodResource.setQuality(foodResource.getQuality()
							* (1 - (gParams.getDecreaseSoilFertilityAtPoles() - distanceToPole)
									/ gParams.getDecreaseSoilFertilityAtPoles()));
				}
				if (distanceToPole < gParams.getDecreaseSoilAreaAtPoles()) {
					DataFoodResource foodResource = p.getFoodResources().get(0);
					foodResource.setAmount(
							foodResource.getAmount() * (1 - (gParams.getDecreaseSoilAreaAtPoles() - distanceToPole)
									/ gParams.getDecreaseSoilAreaAtPoles()));
				}
			});
		}
	}

	private void fillPopulation(DataGame game) {
		GameParams gParams = game.getGameParams();
		game.getMap().getProvinces().stream().filter(p -> (p.getTerrainType().isPopulationPossible())).forEach(p -> {
			DataFoodResource food = p.getFoodResources().get(0);
			if (food.getQuality() >= gParams.getMinSoilFertilityToCreatePopulaton()) {
				DataPopulation pop = new DataPopulation();
				pop.setId(game.nextPopulationId());
				pop.setAmount((int) (food.getQuality() * gParams.getPopulationAtStart()));
				initCulture(p, pop, game);
				initScience(pop, gParams);
				p.getPopulation().clear();
				p.getPopulation().add(pop);
			}
		});
	}

	private void initCulture(DataProvince p, DataPopulation pop, DataGame game) {
		GameParams gParams = game.getGameParams();
		DataCulture cult = new DataCulture();
		cult.setRed(gParams.getRandom().nextInt(255));
		cult.setGreen(gParams.getRandom().nextInt(255));
		cult.setBlue(gParams.getRandom().nextInt(255));
		pop.setCulture(cult);
	}

	private void initScience(DataPopulation pop, GameParams gParams) {
		DataScienceCollection.allGetter4Science().forEach(
				scienceGetter -> scienceGetter.apply(pop.getScience()).setAmount(gParams.getScienceValueStart()));
	}

	private void fillInfrastructure(DataGame game) {
		GameParams gParams = game.getGameParams();
		game.getMap().getProvinces().stream().filter(p -> (p.getTerrainType().isPopulationPossible())).forEach(p -> {
			int popsAmpount = p.getPopulation().stream().mapToInt(pop -> pop.getAmount()).sum();
			p.setInfrastructure((int) (popsAmpount * gParams.getInfrastructureNaturalLimitFromPopulation()));
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
