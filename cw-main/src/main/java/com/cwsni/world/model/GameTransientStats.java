package com.cwsni.world.model;

public class GameTransientStats {
	
	private int maxPopulationInProvince;
	private long totalPopulation;
	private int maxSoilQuality;

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

}
