package com.cwsni.world.model.data;

import com.cwsni.world.util.CwRandom;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class GameParams {

	@JsonIgnore
	private CwRandom random;

	// ------------Map generation section--------
	private long seed = System.currentTimeMillis();
	private int rows = 10;
	private int columns = 10;
	private int provinceRadius = 30;
	private int terrainCorePoints = 10;
	private double oceanPercent = 0.4;

	private int provinceAreaAtStartMax = 100000;
	private double soilAreaMinPercentFromMaxArea = 0.1;
	private double soilAreaCorePointsPerProvinces = 0.01;

	private double soilFertilityAtStartMin = 1.2;
	private double soilFertilityAtStartMax = 1.4;
	private double soilFertilityCorePointsPerProvinces = 0.01;
	private int fractionOfMaxSoilFertility = 3;
	private double decreaseSoilFertilityAtPoles = 0.4;

	private int populationAtStart = 1000;
	private double minSoilFertilityToStartPopulation = Math.min(1.1, soilFertilityAtStartMin);
	private int scienceValueStart = 0;

	// ------------End of Map generation section--------

	// ------------Turn procession section--------
	private double populationBaseGrowth = 0.01;
	private double populationBaseMigration = 0.002;
	private double populationMaxExcess = 1.2;

	private double scienceBaseIncreasePerTurnPerPerson = 0.1;
	private double scienceNaturalGrowthLimitPerPerson = 0.1;
	private double scienceExchangeWithMaxPerTurn = 0.1;
	private double scienceExchangeFromNeighborsPercentFromMax = 0.5;

	private double scienceAgricultureMultiplicatorForFertility = (double) 1 / 4000;
	private double scienceMedicineMultiplicatorForEpidemic = (double) 1 / 10000;

	private double eventGlobalClimateChangeProbability = 0.01;
	private double eventGlobalClimateChangeContinueProbability = 0.8;
	private double eventGlobalClimateChangeBadProbability = 0.7;
	private double eventGlobalClimateChangeMultiplicator = 0.02;
	private double eventGlobalClimateMaxChange = 0.2;
	private int eventGlobalClimateChangeDuration = 10;

	private double eventEpidemicProbability = 0.02;
	private double eventEpidemicContagiousness = 0.5;
	private double eventEpidemicDeathRate = 0.4;
	private int eventEpidemicDuration = 10;
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

	public int getProvinceAreaAtStartMax() {
		return provinceAreaAtStartMax;
	}

	public void setProvinceAreaAtStartMax(int provinceAreAtStartMax) {
		this.provinceAreaAtStartMax = provinceAreAtStartMax;
	}

	public double getSoilFertilityAtStartMin() {
		return soilFertilityAtStartMin;
	}

	public void setSoilFertilityAtStartMin(double soilFertilityAtStartMin) {
		this.soilFertilityAtStartMin = soilFertilityAtStartMin;
	}

	public double getSoilFertilityAtStartMax() {
		return soilFertilityAtStartMax;
	}

	public void setSoilFertilityAtStartMax(double soilFertilityAtStartMax) {
		this.soilFertilityAtStartMax = soilFertilityAtStartMax;
	}

	public double getOceanPercent() {
		return oceanPercent;
	}

	public void setOceanPercent(double oceanPercent) {
		this.oceanPercent = oceanPercent;
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

	public int getPopulationAtStart() {
		return populationAtStart;
	}

	public void setPopulationAtStart(int populationAtStart) {
		this.populationAtStart = populationAtStart;
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

	public CwRandom getRandom() {
		if (random == null) {
			random = new CwRandom(getSeed());
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

	public double getEventGlobalClimateMaxChange() {
		return eventGlobalClimateMaxChange;
	}

	public void setEventGlobalClimateMaxChange(double eventGlobalClimateMaxChange) {
		this.eventGlobalClimateMaxChange = eventGlobalClimateMaxChange;
	}

	public double getPopulationBaseMigration() {
		return populationBaseMigration;
	}

	public void setPopulationBaseMigration(double populationBaseMigration) {
		this.populationBaseMigration = populationBaseMigration;
	}

	public int getScienceValueStart() {
		return scienceValueStart;
	}

	public void setScienceValueStart(int scienceAgricultureStart) {
		this.scienceValueStart = scienceAgricultureStart;
	}

	public double getSoilAreaMinPercentFromMaxArea() {
		return soilAreaMinPercentFromMaxArea;
	}

	public void setSoilAreaMinPercentFromMaxArea(double soilAreaMinPercentFromMaxArea) {
		this.soilAreaMinPercentFromMaxArea = soilAreaMinPercentFromMaxArea;
	}

	public double getScienceAgricultureMultiplicatorForFertility() {
		return scienceAgricultureMultiplicatorForFertility;
	}

	public void setScienceAgricultureMultiplicatorForFertility(double scienceAgricultureMultiplicatorForFertility) {
		this.scienceAgricultureMultiplicatorForFertility = scienceAgricultureMultiplicatorForFertility;
	}

	public double getScienceMedicineMultiplicatorForEpidemic() {
		return scienceMedicineMultiplicatorForEpidemic;
	}

	public void setScienceMedicineMultiplicatorForEpidemic(double scienceMedicineMultiplicatorForEpidemic) {
		this.scienceMedicineMultiplicatorForEpidemic = scienceMedicineMultiplicatorForEpidemic;
	}

	public double getScienceBaseIncreasePerTurnPerPerson() {
		return scienceBaseIncreasePerTurnPerPerson;
	}

	public void setScienceBaseIncreasePerTurnPerPerson(double scienceBaseIncreasePerTurnPerPerson) {
		this.scienceBaseIncreasePerTurnPerPerson = scienceBaseIncreasePerTurnPerPerson;
	}

	public double getScienceExchangeWithMaxPerTurn() {
		return scienceExchangeWithMaxPerTurn;
	}

	public void setScienceExchangeWithMaxPerTurn(double scienceExchangeWithMaxPerTurn) {
		this.scienceExchangeWithMaxPerTurn = scienceExchangeWithMaxPerTurn;
	}

	public double getScienceNaturalGrowthLimitPerPerson() {
		return scienceNaturalGrowthLimitPerPerson;
	}

	public void setScienceNaturalGrowthLimitPerPerson(double scienceNaturalGrowthLimitPerPerson) {
		this.scienceNaturalGrowthLimitPerPerson = scienceNaturalGrowthLimitPerPerson;
	}

	public double getScienceExchangeFromNeighborsPercentFromMax() {
		return scienceExchangeFromNeighborsPercentFromMax;
	}

	public void setScienceExchangeFromNeighborsPercentFromMax(double scienceExchangeFromNeighborsPercentFromMax) {
		this.scienceExchangeFromNeighborsPercentFromMax = scienceExchangeFromNeighborsPercentFromMax;
	}

}
