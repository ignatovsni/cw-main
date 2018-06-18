package com.cwsni.world.model;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GameParams {

	@JsonIgnore
	private Random random;

	// ------------Map generation section--------
	private long seed = System.currentTimeMillis();
	private int rows = 10;
	private int columns = 10;
	private int provinceRadius = 30;
	private int terrainCorePoints = 10;
	private double oceanProcent = 0.4;

	private int minSoilArea = 10000;
	private int maxSoilArea = 20000;
	private double soilAreaCorePointsPerProvinces = 0.01;
	private int fractionOfMaxSoilArea = 2;

	private double minSoilFertility = 1.2;
	private double maxSoilFertility = 1.4;
	private double soilFertilityCorePointsPerProvinces = 0.01;
	private int fractionOfMaxSoilFertility = 5;
	private double decreaseSoilFertilityAtPoles = 0.1;

	private double populationAtStartFromMax = 0.1;
	private double minSoilFertilityToStartPopulation = 1.1;
	// ------------End of Map generation section--------

	// ------------Turn procession section--------
	private double populationBaseGrowth = 1.01;
	private double populationMaxExcess = 1.2;

	private double eventGlobalClimateChangeProbability = 0.01;
	private double eventGlobalClimateChangeContinueProbability = 0.8;
	private double eventGlobalClimateChangeBadProbability = 0.7;
	private double eventGlobalClimateChangeMultiplicator = 0.01;
	private int eventGlobalClimateChangeDuration = 10;

	private double eventEpidemicProbability = 0.02;
	private double eventEpidemicContagiousness = 0.5;
	private double eventEpidemicDeathRate = 0.2;
	private int eventEpidemicDuration = 5;
	private int eventEpidemicProtectionDuration = 20;
	// ------------End of turn procession section--------

	public double getEventGlobalClimateChangeProbability() {
		return eventGlobalClimateChangeProbability;
	}

	public void setEventGlobalClimateChangeProbability(double eventGlobalClimateChangeProbability) {
		this.eventGlobalClimateChangeProbability = eventGlobalClimateChangeProbability;
	}

	public double getPopulationBaseGrowth() {
		return populationBaseGrowth;
	}

	public void setPopulationBaseGrowth(double populationBaseGrowthPercent) {
		this.populationBaseGrowth = populationBaseGrowthPercent;
	}

	public double getSoilAreaCorePointsPerProvinces() {
		return soilAreaCorePointsPerProvinces;
	}

	public void setSoilAreaCorePointsPerProvinces(double soilAreaCorePointsPerProvinces) {
		this.soilAreaCorePointsPerProvinces = soilAreaCorePointsPerProvinces;
	}

	public double getSoilFertilityCorePointsPerProvinces() {
		return soilFertilityCorePointsPerProvinces;
	}

	public void setSoilFertilityCorePointsPerProvinces(double soilFertilityCorePointsPerProvinces) {
		this.soilFertilityCorePointsPerProvinces = soilFertilityCorePointsPerProvinces;
	}

	public int getFractionOfMaxSoilFertility() {
		return fractionOfMaxSoilFertility;
	}

	public void setFractionOfMaxSoilFertility(int fractionOfMaxSoilFertility) {
		this.fractionOfMaxSoilFertility = fractionOfMaxSoilFertility;
	}

	public int getFractionOfMaxSoilArea() {
		return fractionOfMaxSoilArea;
	}

	public void setFractionOfMaxSoilArea(int fractionOfMaxSoilArea) {
		this.fractionOfMaxSoilArea = fractionOfMaxSoilArea;
	}

	public int getMinSoilArea() {
		return minSoilArea;
	}

	public void setMinSoilArea(int minSoilArea) {
		this.minSoilArea = minSoilArea;
	}

	public int getMaxSoilArea() {
		return maxSoilArea;
	}

	public void setMaxSoilArea(int maxSoilArea) {
		this.maxSoilArea = maxSoilArea;
	}

	public double getMinSoilFertility() {
		return minSoilFertility;
	}

	public void setMinSoilFertility(double minSoilFertility) {
		this.minSoilFertility = minSoilFertility;
	}

	public double getMaxSoilFertility() {
		return maxSoilFertility;
	}

	public void setMaxSoilFertility(double maxSoilFertility) {
		this.maxSoilFertility = maxSoilFertility;
	}

	public double getOceanProcent() {
		return oceanProcent;
	}

	public void setOceanProcent(double oceanProcent) {
		this.oceanProcent = oceanProcent;
	}

	public int getTerrainCorePoints() {
		return terrainCorePoints;
	}

	public void setTerrainCorePoints(int terrainCorePoints) {
		this.terrainCorePoints = terrainCorePoints;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public int getProvinceRadius() {
		return provinceRadius;
	}

	public void setProvinceRadius(int provinceRadius) {
		this.provinceRadius = provinceRadius;
	}

	public double getPopulationAtStartFromMax() {
		return populationAtStartFromMax;
	}

	public void setPopulationAtStartFromMax(double populationAtStartFromMax) {
		this.populationAtStartFromMax = populationAtStartFromMax;
	}

	public double getDecreaseSoilFertilityAtPoles() {
		return decreaseSoilFertilityAtPoles;
	}

	public void setDecreaseSoilFertilityAtPoles(double decreaseSoilFertilityAtPoluses) {
		this.decreaseSoilFertilityAtPoles = decreaseSoilFertilityAtPoluses;
	}

	@JsonIgnore
	public int getSoilAreaCorePoints() {
		return (int) (getRows() * getColumns() * getSoilAreaCorePointsPerProvinces());
	}

	@JsonIgnore
	public int getSoilFertilityCorePoints() {
		return (int) (getRows() * getColumns() * getSoilFertilityCorePointsPerProvinces());
	}

	public Random getRandom() {
		if (random == null) {
			random = new Random(getSeed());
		}
		return random;
	}

	public double getPopulationMaxExcess() {
		return populationMaxExcess;
	}

	public void setPopulationMaxExcess(double populationMaxExcess) {
		this.populationMaxExcess = populationMaxExcess;
	}

	public double getMinSoilFertilityToStartPopulation() {
		return minSoilFertilityToStartPopulation;
	}

	public void setMinSoilFertilityToStartPopulation(double minSoilFertilityToStartPopulation) {
		this.minSoilFertilityToStartPopulation = minSoilFertilityToStartPopulation;
	}

	public double getEventGlobalClimateChangeContinueProbability() {
		return eventGlobalClimateChangeContinueProbability;
	}

	public void setEventGlobalClimateChangeContinueProbability(double eventGlobalClimateChangeContinueProbability) {
		this.eventGlobalClimateChangeContinueProbability = eventGlobalClimateChangeContinueProbability;
	}

	public int getEventGlobalClimateChangeDuration() {
		return eventGlobalClimateChangeDuration;
	}

	public void setEventGlobalClimateChangeDuration(int eventGlobalClimateChangeDuration) {
		this.eventGlobalClimateChangeDuration = eventGlobalClimateChangeDuration;
	}

	public double getEventGlobalClimateChangeMultiplicator() {
		return eventGlobalClimateChangeMultiplicator;
	}

	public void setEventGlobalClimateChangeMultiplicator(double eventGlobalClimateChangeMultiplicator) {
		this.eventGlobalClimateChangeMultiplicator = eventGlobalClimateChangeMultiplicator;
	}

	public double getEventGlobalClimateChangeBadProbability() {
		return eventGlobalClimateChangeBadProbability;
	}

	public void setEventGlobalClimateChangeBadProbability(double eventGlobalClimateChangeBadProbability) {
		this.eventGlobalClimateChangeBadProbability = eventGlobalClimateChangeBadProbability;
	}

	public double getEventEpidemicProbability() {
		return eventEpidemicProbability;
	}

	public void setEventEpidemicProbability(double eventEpidemicProbability) {
		this.eventEpidemicProbability = eventEpidemicProbability;
	}

	public double getEventEpidemicContagiousness() {
		return eventEpidemicContagiousness;
	}

	public void setEventEpidemicContagiousness(double eventEpidemicContagiousness) {
		this.eventEpidemicContagiousness = eventEpidemicContagiousness;
	}

	public double getEventEpidemicDeathRate() {
		return eventEpidemicDeathRate;
	}

	public void setEventEpidemicDeathRate(double eventEpidemicDeathRate) {
		this.eventEpidemicDeathRate = eventEpidemicDeathRate;
	}

	public int getEventEpidemicDuration() {
		return eventEpidemicDuration;
	}

	public void setEventEpidemicDuration(int eventEpidemicDuration) {
		this.eventEpidemicDuration = eventEpidemicDuration;
	}

	public int getEventEpidemicProtectionDuration() {
		return eventEpidemicProtectionDuration;
	}

	public void setEventEpidemicProtectionDuration(int eventEpidemicProtectionDuration) {
		this.eventEpidemicProtectionDuration = eventEpidemicProtectionDuration;
	}

}
