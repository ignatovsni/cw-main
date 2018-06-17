package com.cwsni.world.model;

public class GameStats {
	
	private int maxPopulationInProvince;
	private int totalPopulation;
	private int maxSoilQuality;

	public int getMaxSoilQuality() {
		return maxSoilQuality;
	}

	public void setMaxSoilQuality(int maxSoilQuality) {
		this.maxSoilQuality = maxSoilQuality;
	}

	public int getTotalPopulation() {
		return totalPopulation;
	}

	public void setTotalPopulation(int totalPopulation) {
		this.totalPopulation = totalPopulation;
	}

	public int getMaxPopulationInProvince() {
		return maxPopulationInProvince;
	}

	public void setMaxPopulationInProvince(int maxPopulation) {
		this.maxPopulationInProvince = maxPopulation;
	}

}
