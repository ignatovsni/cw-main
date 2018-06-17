package com.cwsni.world.model;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GameParams {

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

	private double minSoilFertility = 1;
	private double maxSoilFertility = 1.3;
	private double soilFertilityCorePointsPerProvinces = 0.01;
	private int fractionOfMaxSoilFertility = 5;
	private double decreaseSoilFertilityAtPoles = 0.15;

	private double mapPopulationAtStart = 0.1;

	@JsonIgnore
	private Random random;

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

	public double getMapPopulationAtStart() {
		return mapPopulationAtStart;
	}

	public void setMapPopulationAtStart(double mapPopulationAtStart) {
		this.mapPopulationAtStart = mapPopulationAtStart;
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

}
