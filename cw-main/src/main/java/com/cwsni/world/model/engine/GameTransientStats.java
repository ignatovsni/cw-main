package com.cwsni.world.model.engine;

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

	private InternalFutureTask<DoubleSummaryStatistics> govInfluenceInProvince;
	private InternalFutureTask<Double> govInfluenceMedianInProvince;

	private InternalFutureTask<DoubleSummaryStatistics> infrastructureInProvince;
	private InternalFutureTask<Double> infrastructureMedianInProvince;
	
	private InternalFutureTask<DoubleSummaryStatistics> soilNaturalFertility;
	private InternalFutureTask<Double> soilNaturalFertilityMedian;
	private InternalFutureTask<DoubleSummaryStatistics> soilFertility;
	private InternalFutureTask<Double> soilFertilityMedian;
	private InternalFutureTask<DoubleSummaryStatistics> soilArea;
	private InternalFutureTask<Double> soilAreaMedian;

	private InternalFutureTask<DoubleSummaryStatistics> scienceAgricultureInProvince;
	private InternalFutureTask<Double> scienceAgricultureMedianInProvince;
	private InternalFutureTask<DoubleSummaryStatistics> scienceMedicineInProvince;
	private InternalFutureTask<Double> scienceMedicineMedianInProvince;
	private InternalFutureTask<DoubleSummaryStatistics> scienceAdministrationInProvince;
	private InternalFutureTask<Double> scienceAdministrationMedianInProvince;

	public GameTransientStats(Game game) {
		this.game = game;

		populationInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().filter(p -> p.getPopulationAmount() > 0)
					.mapToInt(p -> p.getPopulationAmount()).summaryStatistics();
		}, new IntSummaryStatistics());

		populationMedianInProvince = new InternalFutureTask<>(() -> {
			int v = new MedianFinder()
					.findMedianIntegerAndGetZeroIfEmpty(getPopulationPossibleProvinces().filter(p -> p.getPopulationAmount() > 0)
							.map(p -> p.getPopulationAmount()).collect(Collectors.toList()));
			return v;
		}, 0);

		govInfluenceInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().filter(p -> p.getPopulationAmount() > 0)
					.mapToDouble(p -> p.getGovernmentInfluence()).summaryStatistics();
		}, new DoubleSummaryStatistics());

		govInfluenceMedianInProvince = new InternalFutureTask<>(() -> {
			double v = new MedianFinder()
					.findMedianDoubleAndGetZeroIfEmpty(getPopulationPossibleProvinces().filter(p -> p.getPopulationAmount() > 0)
							.map(p -> p.getGovernmentInfluence()).collect(Collectors.toList()));
			return v;
		}, 0.0);

		infrastructureInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces()
					.filter(p -> p.getPopulationAmount() >= getPopulationMedianInProvince())
					.mapToDouble(p -> p.getInfrastructurePercent()).summaryStatistics();
		}, new DoubleSummaryStatistics());

		infrastructureMedianInProvince = new InternalFutureTask<>(() -> {
			double v = new MedianFinder().findMedianDoubleAndGetZeroIfEmpty(getPopulationPossibleProvinces()
					.filter(p -> p.getPopulationAmount() >= getPopulationMedianInProvince())
					.map(p -> p.getInfrastructurePercent()).collect(Collectors.toList()));
			return v;
		}, 0.0);

		populationTotal = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().mapToLong(p -> p.getPopulationAmount()).sum();
		}, 0L);

		soilNaturalFertility = new InternalFutureTask<>(() -> {
			return getSoilPossibleProvinces().mapToDouble(p -> p.getSoilNaturalFertility()).summaryStatistics();
		}, new DoubleSummaryStatistics());

		soilNaturalFertilityMedian = new InternalFutureTask<>(() -> {
			double v = new MedianFinder().findMedianDoubleAndGetZeroIfEmpty(
					getSoilPossibleProvinces().map(p -> p.getSoilNaturalFertility()).collect(Collectors.toList()));
			return v;
		}, 0.0);

		soilFertility = new InternalFutureTask<>(() -> {
			return getSoilPossibleProvinces().mapToDouble(p -> p.getSoilFertility()).summaryStatistics();
		}, new DoubleSummaryStatistics());

		soilFertilityMedian = new InternalFutureTask<>(() -> {
			double v = new MedianFinder().findMedianDoubleAndGetZeroIfEmpty(
					getSoilPossibleProvinces().map(p -> p.getSoilFertility()).collect(Collectors.toList()));
			return v;
		}, 0.0);
		
		soilArea = new InternalFutureTask<>(() -> {
			return getSoilPossibleProvinces().mapToDouble(p -> p.getSoilArea()).summaryStatistics();
		}, new DoubleSummaryStatistics());

		soilAreaMedian = new InternalFutureTask<>(() -> {
			double v = new MedianFinder().findMedianDoubleAndGetZeroIfEmpty(
					getSoilPossibleProvinces().map(p -> p.getSoilArea()).collect(Collectors.toList()));
			return v;
		}, 0.0);

		scienceAgricultureInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().mapToDouble(p -> p.getScienceAgriculture()).summaryStatistics();
		}, new DoubleSummaryStatistics());

		scienceAgricultureMedianInProvince = new InternalFutureTask<>(() -> {
			double v = new MedianFinder().findMedianDoubleAndGetZeroIfEmpty(
					getPopulationPossibleProvinces().map(p -> p.getScienceAgriculture()).collect(Collectors.toList()));
			return v;
		}, 0.0);

		scienceMedicineInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().mapToDouble(p -> p.getScienceMedicine()).summaryStatistics();
		}, new DoubleSummaryStatistics());

		scienceMedicineMedianInProvince = new InternalFutureTask<>(() -> {
			double v = new MedianFinder().findMedianDoubleAndGetZeroIfEmpty(
					getPopulationPossibleProvinces().map(p -> p.getScienceMedicine()).collect(Collectors.toList()));
			return v;
		}, 0.0);

		scienceAdministrationInProvince = new InternalFutureTask<>(() -> {
			return getPopulationPossibleProvinces().mapToDouble(p -> p.getScienceAdministration()).summaryStatistics();
		}, new DoubleSummaryStatistics());

		scienceAdministrationMedianInProvince = new InternalFutureTask<>(() -> {
			double v = new MedianFinder().findMedianDoubleAndGetZeroIfEmpty(getPopulationPossibleProvinces()
					.map(p -> p.getScienceAdministration()).collect(Collectors.toList()));
			return v;
		}, 0.0);
	}

	private Stream<Province> getPopulationPossibleProvinces() {
		return game.getMap().getProvinces().stream().filter(p -> p.getTerrainType().isPopulationPossible());
	}

	private Stream<Province> getSoilPossibleProvinces() {
		return game.getMap().getProvinces().stream().filter(p -> p.getTerrainType().isSoilPossible());
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

	public double getInfrastructureMaxInProvince() {
		return infrastructureInProvince.get().getMax();
	}

	public double getInfrastructureAvgInProvince() {
		return infrastructureInProvince.get().getAverage();
	}

	public double getInfrastructureMedianInProvince() {
		return infrastructureMedianInProvince.get();
	}

	public double getSoilAreaMax() {
		return soilArea.get().getMax();
	}

	public double getSoilAreaAvg() {
		return soilArea.get().getAverage();
	}

	public double getSoilAreaMedian() {
		return soilAreaMedian.get();
	}

	public double getSoilNaturalFertilityMedian() {
		return soilNaturalFertilityMedian.get();
	}

	public double getSoilNaturalFertilityMax() {
		return soilNaturalFertility.get().getMax();
	}

	public double getSoilNaturalFertilityAvg() {
		return soilNaturalFertility.get().getAverage();
	}

	public double getSoilFertilityMedian() {
		return soilFertilityMedian.get();
	}

	public double getSoilFertilityMax() {
		return soilFertility.get().getMax();
	}

	public double getSoilFertilityAvg() {
		return soilFertility.get().getAverage();
	}
	
	public double getScienceAgricultureMaxInProvince() {
		return scienceAgricultureInProvince.get().getMax();
	}

	public double getScienceAgricultureAvgInProvince() {
		return scienceAgricultureInProvince.get().getAverage();
	}

	public double getScienceAgricultureMedianInProvince() {
		return scienceAgricultureMedianInProvince.get();
	}

	public double getScienceMedicineMaxInProvince() {
		return scienceMedicineInProvince.get().getMax();
	}

	public double getScienceMedicineAvgInProvince() {
		return scienceMedicineInProvince.get().getAverage();
	}

	public double getScienceMedicineMedianInProvince() {
		return scienceMedicineMedianInProvince.get();
	}

	public double getScienceAdministrationMaxInProvince() {
		return scienceAdministrationInProvince.get().getMax();
	}

	public double getScienceAdministrationAvgInProvince() {
		return scienceAdministrationInProvince.get().getAverage();
	}

	public double getScienceAdministrationMedianInProvince() {
		return scienceAdministrationMedianInProvince.get();
	}

	public double getGovInfluenceMaxInProvince() {
		return govInfluenceInProvince.get().getMax();
	}

	public double getGovInfluenceAvgInProvince() {
		return govInfluenceInProvince.get().getAverage();
	}

	public double getGovInfluenceMedianInProvince() {
		return govInfluenceMedianInProvince.get();
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
