package com.cwsni.world.model;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.util.MedianFinder;

public class GameTransientStats {

	private static final Log logger = LogFactory.getLog(GameTransientStats.class);

	private Game game;
	private InternalFutureTask<IntSummaryStatistics> populationInProvince;
	private InternalFutureTask<Integer> populationMedianInProvince;
	private InternalFutureTask<Long> populationTotal;
	private InternalFutureTask<Integer> soilQualityMax;
	private InternalFutureTask<DoubleSummaryStatistics> soilFertility;
	private InternalFutureTask<Double> soilFertilityMedian;
	private InternalFutureTask<IntSummaryStatistics> scienceAgricultureInProvince;
	private InternalFutureTask<Integer> scienceAgricultureMedianInProvince;

	public GameTransientStats(Game game) {
		this.game = game;

		populationInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().filter(p -> p.getPopulationAmount() > 0)
					.mapToInt(p -> p.getPopulationAmount()).summaryStatistics();
		}, new IntSummaryStatistics());

		populationMedianInProvince = new InternalFutureTask<>(() -> {
			int v = new MedianFinder()
					.findMedianInteger(getPopulationPossibleProvinces().filter(p -> p.getPopulationAmount() > 0)
							.map(p -> p.getPopulationAmount()).collect(Collectors.toList()));
			return v;
		}, 0);

		populationTotal = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().mapToLong(p -> p.getPopulationAmount()).sum();
		}, 0L);

		soilQualityMax = new InternalFutureTask<>(() -> {
			return getSoilPossibleProvinces().mapToInt(p -> p.getSoilQuality()).max().getAsInt();
		}, 0);

		soilFertility = new InternalFutureTask<>(() -> {
			return getSoilPossibleProvinces().mapToDouble(p -> p.getSoilFertility()).summaryStatistics();
		}, new DoubleSummaryStatistics());

		soilFertilityMedian = new InternalFutureTask<>(() -> {
			double v = new MedianFinder().findMedianDouble(
					getSoilPossibleProvinces().map(p -> p.getSoilFertility()).collect(Collectors.toList()));
			return v;
		}, 0.0);

		scienceAgricultureInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().mapToInt(p -> p.getScienceAgriculture()).summaryStatistics();
		}, new IntSummaryStatistics());

		scienceAgricultureMedianInProvince = new InternalFutureTask<>(() -> {
			int v = new MedianFinder().findMedianInteger(
					getPopulationPossibleProvinces().map(p -> p.getScienceAgriculture()).collect(Collectors.toList()));
			return v;
		}, 0);
	}

	private Stream<Province> getPopulationPossibleProvinces() {
		return game.getMap().getProvinces().stream().filter(p -> p.getTerrainType().isPopulationPossible());
	}

	private Stream<Province> getSoilPossibleProvinces() {
		return game.getMap().getProvinces().stream().filter(p -> p.getTerrainType().isSoilPossible());
	}

	public int getSoilQualityMax() {
		return soilQualityMax.get();
	}

	public long getPopulationTotal() {
		return populationTotal.get();
	}

	public int getPopulationMaxInProvince() {
		return populationInProvince.get().getMax();
	}

	public double getPopulationAvgInProvince() {
		return populationInProvince.get().getAverage();
	}

	public int getPopulationMedianInProvince() {
		return populationMedianInProvince.get();
	}

	public double getSoilFertilityMax() {
		return soilFertility.get().getMax();
	}

	public double getSoilFertilityAvg() {
		return soilFertility.get().getAverage();
	}

	public double getSoilFertilityMedian() {
		return soilFertilityMedian.get();
	}

	public int getScienceAgricultureMaxInProvince() {
		return scienceAgricultureInProvince.get().getMax();
	}

	public double getScienceAgricultureAvgInProvince() {
		return scienceAgricultureInProvince.get().getAverage();
	}

	public int getScienceAgricultureMedianInProvince() {
		return scienceAgricultureMedianInProvince.get();
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
				e.printStackTrace();
				return defaultValue;
			}
		}
	}

}
