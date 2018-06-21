package com.cwsni.world.model;

public class GameTransientStats {
	
	private int maxPopulationInProvince;
	private long totalPopulation;
	private int maxSoilQuality;
	private double maxSoilFertility;
	private double minSoilFertility;
	private int maxScienceAgricultureInProvince;

	public int getMaxSoilQuality() {
		return maxSoilQuality;
	}

	public void setMaxSoilQuality(int maxSoilQuality) {
		this.maxSoilQuality = maxSoilQuality;
	}

	public long getTotalPopulation() {
		return totalPopulation;
	}

	public void setTotalPopulation(long totalPopulation) {
		this.totalPopulation = totalPopulation;
	}

	public int getMaxPopulationInProvince() {
		return maxPopulationInProvince;
	}

	public void setMaxPopulationInProvince(int maxPopulation) {
		this.maxPopulationInProvince = maxPopulation;
	}

	public double getMaxSoilFertility() {
		return maxSoilFertility;
	}

	public void setMaxSoilFertility(double maxSoilFertility) {
		this.maxSoilFertility = maxSoilFertility;
	}

	public double getMinSoilFertility() {
		return minSoilFertility;
	}

	public void setMinSoilFertility(double minSoilFertility) {
		this.minSoilFertility = minSoilFertility;
	}

	public int getMaxScienceAgricultureInProvince() {
		return maxScienceAgricultureInProvince;
	}

	public void setMaxScienceAgricultureInProvince(int maxScienceAgricultureInProvince) {
		this.maxScienceAgricultureInProvince = maxScienceAgricultureInProvince;
	}
	
}
