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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.model.data.DataCulture;
import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.data.DataScienceCollection;
import com.cwsni.world.model.data.DataWorldMap;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.data.Turn;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.services.algorithms.GameAlgorithms;
import com.cwsni.world.util.CwRandom;

@Component
public class GameGenerator {

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private GameAlgorithms gameAlgorithms;

	@Autowired
	private GameEventListener gameEventListener;

	private class TempData {
		Map<Integer, DataProvince> provByIds = new HashMap<>();
		Set<Integer> coreTerrainIds = new HashSet<>();
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
		dataGame.setTurn(new Turn(0));
		createMap(dataGame, tData);
		createTerrain(dataGame, tData);
		createMountaints(dataGame, tData);
		int lastContinentId = defineContinents(dataGame, tData, tt -> !tt.isWater(), 0);
		int lastOceanId = defineContinents(dataGame, tData, tt -> tt.isWater(), lastContinentId);
		dataGame.getMap().setContinents(lastContinentId);
		dataGame.getMap().setOceans(lastOceanId - lastContinentId);
		fillSoil(dataGame, tData);
		fillPopulation(dataGame);
		fillInfrastructure(dataGame);
		Game game = new Game();
		game.buildFrom(dataGame, messageSource, getGameAlgorithms(), gameEventListener);
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

	private void createMountaints(DataGame game, TempData tData) {
		if (game.getMap().getProvinces().size() < 200) {
			return;
		}
		GameParams gParams = game.getGameParams();
		int needMountains = (int) (gParams.getMountainsPerMapProvinces() * game.getMap().getProvinces().size());
		int maxChainLength = Math.max(gParams.getRows(), gParams.getColumns());
		createChainMountains(tData, gParams, needMountains, maxChainLength);
		createChainMountains(tData, gParams, needMountains, 1);

		// Check isolated provinces. It is not the best solution, but still...
		tData.provByIds.values().forEach(p -> {
			long notMontainsProvs = p.getNeighbors().stream().map(nId -> tData.provByIds.get(nId))
					.filter(n -> !n.getTerrainType().isMountain()).count();
			if (notMontainsProvs == 0) {
				configureMountainProvince(p);
			}
		});
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
		List<DataProvince> terrain = new ArrayList<>();
		// initialize area attributes and find all terrain provinces
		map.getProvinces().stream().filter(p -> p.getTerrainType().isSoilPossible()).forEach(p -> {
			p.setSize(100);
			// p.setSize((int) (50 + 100 * gParams.getRandom().nextNormalDouble()));
			p.setSoilArea(p.getSize() * gParams.getSoilAreaPerSize() / 10);
			p.setSoilFertility(gParams.getSoilFertilityAtStartBase());
			terrain.add(p);
		});
		fillSoilFertility(game, tData, terrain);
	}

	private void fillSoilFertility(DataGame game, TempData tData, List<DataProvince> terrain) {
		GameParams gParams = game.getGameParams();
		fillSoilFertilityAtPoles(tData, terrain, gParams);
		Set<Integer> increasedIds = new HashSet<>();
		// setup core provs
		// use core terrains to support soil and population on all "continents"
		tData.coreTerrainIds.forEach(coreId -> {
			DataProvince p = tData.provByIds.get(coreId);
			p.setSoilFertility(p.getSoilFertility() * gParams.getSoilFertilityAtStartCoeffForCoast());
			if (p.getSoilFertility() < gParams.getMinSoilFertilityToStartPopulation()) {
				p.setSoilFertility(gParams.getMinSoilFertilityToStartPopulation() + 0.01);
			}
			increasedIds.add(coreId);
		});

		// increase soil fertility for coast
		Set<Integer> coastIds = new HashSet<>();
		terrain.stream().filter(p -> p.getTerrainType().isSoilPossible()).forEach(p -> {
			if (p.getNeighbors().stream().map(nId -> tData.provByIds.get(nId)).filter(n -> n.getTerrainType().isWater())
					.findAny().isPresent()) {
				p.setSoilFertility(p.getSoilFertility() * gParams.getSoilFertilityAtStartCoeffForCoast());
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
						double fertility = (p.getSoilFertility() * gParams.getFractionOfMaxSoilFertility()
								+ n.getSoilFertility()) / (gParams.getFractionOfMaxSoilFertility() + 1);
						n.setSoilFertility(DataFormatter.doubleWith2points(fertility));
						queueProvs.add(n);
						increasedIds.add(n.getId());
					});
		}
	}

	private void fillSoilFertilityAtPoles(TempData tData, List<DataProvince> terrain, GameParams gParams) {
		if (!terrain.isEmpty()) {
			double minY = terrain.stream().mapToDouble(p -> p.getCenter().getY()).min().getAsDouble();
			double maxY = terrain.stream().mapToDouble(p -> p.getCenter().getY()).max().getAsDouble();
			terrain.stream().forEach(p -> {
				double distanceToPole = (double) (1
						+ Math.min(p.getCenter().getY() - minY, maxY - p.getCenter().getY())) / (maxY - minY);
				if (distanceToPole < gParams.getDecreaseSoilFertilityAtPoles()) {
					double fertilityDecrease = 1 - (gParams.getDecreaseSoilFertilityAtPoles() - distanceToPole)
							/ gParams.getDecreaseSoilFertilityAtPoles();
					p.setSoilFertility(DataFormatter.doubleWith2points(p.getSoilFertility() * fertilityDecrease));
				}
			});
		}
	}

	private void fillPopulation(DataGame game) {
		GameParams gParams = game.getGameParams();
		game.getMap().getProvinces().stream().filter(p -> (p.getTerrainType().isPopulationPossible())).forEach(p -> {
			if (p.getSoilFertility() >= gParams.getMinSoilFertilityToStartPopulation()) {
				DataPopulation pop = new DataPopulation();
				pop.setAmount((int) (p.getSoilFertility() * gParams.getPopulationAtStart()));
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

	public GameAlgorithms getGameAlgorithms() {
		return gameAlgorithms;
	}

	public void setGameAlgorithms(GameAlgorithms gameAlgorithms) {
		this.gameAlgorithms = gameAlgorithms;
	}

}
