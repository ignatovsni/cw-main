package com.cwsni.world.model;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GameTransientStats {

	private static final Log logger = LogFactory.getLog(GameTransientStats.class);

	private Game game;
	private InternalFutureTask<Integer> maxPopulationInProvince;
	private InternalFutureTask<Long> totalPopulation;
	private InternalFutureTask<Integer> maxSoilQuality;
	private InternalFutureTask<Double> maxSoilFertility;
	private InternalFutureTask<Double> minSoilFertility;
	private InternalFutureTask<Integer> maxScienceAgricultureInProvince;

	public GameTransientStats(Game game) {
		this.game = game;
		maxPopulationInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().mapToInt(p -> p.getPopulationAmount()).max().getAsInt();
		}, 0);
		totalPopulation = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().mapToLong(p -> p.getPopulationAmount()).sum();
		}, 0L);
		maxSoilQuality = new InternalFutureTask<>(() -> {
			return getSoilPossibleProvinces().mapToInt(p -> p.getSoilQuality()).max().getAsInt();
		}, 0);
		maxSoilFertility = new InternalFutureTask<>(() -> {
			return getSoilPossibleProvinces().mapToDouble(p -> p.getSoilFertility()).max().getAsDouble();
		}, 0.0);
		minSoilFertility = new InternalFutureTask<>(() -> {
			return getSoilPossibleProvinces().mapToDouble(p -> p.getSoilFertility()).min().getAsDouble();
		}, 0.0);
		maxScienceAgricultureInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().mapToInt(p -> p.getScienceAgriculture()).max().getAsInt();
		}, 0);
	}

	private Stream<Province> getPopulationPossibleProvinces() {
		return game.getMap().getProvinces().stream().filter(p -> p.getTerrainType().isPopulationPossible());
	}

	private Stream<Province> getSoilPossibleProvinces() {
		return game.getMap().getProvinces().stream().filter(p -> p.getTerrainType().isSoilPossible());
	}

	public int getMaxSoilQuality() {
		return maxSoilQuality.get();
	}

	public long getTotalPopulation() {
		return totalPopulation.get();
	}

	public int getMaxPopulationInProvince() {
		return maxPopulationInProvince.get();
	}

	public double getMaxSoilFertility() {
		return maxSoilFertility.get();
	}

	public double getMinSoilFertility() {
		return minSoilFertility.get();
	}

	public int getMaxScienceAgricultureInProvince() {
		return maxScienceAgricultureInProvince.get();
	}

	private class InternalFutureTask<V> extends FutureTask<V> {
		private V defaultValue;

		public InternalFutureTask(Callable<V> callable, V defaultValue) {
			super(callable);
			this.defaultValue = defaultValue;
		}

		public V get() {
			try {
				run();
				return super.get();
			} catch (InterruptedException | ExecutionException e) {
				logger.trace(e.getMessage(), e);
				return defaultValue;
			}
		}
	}

}
