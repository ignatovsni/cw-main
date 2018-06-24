package com.cwsni.world.model.data;

public class GameStats {

	private long maxPopulationForAllTime;
	private long diedFromDisease;
	private long diedFromHunger;
	private long diedFromOverpopulation;

	public long getDiedFromDisease() {
		return diedFromDisease;
	}

	public void setDiedFromDisease(long diedFromDisease) {
		this.diedFromDisease = diedFromDisease;
	}

	public long getDiedFromHunger() {
		return diedFromHunger;
	}

	public void setDiedFromHunger(long diedFromHunger) {
		this.diedFromHunger = diedFromHunger;
	}

	public long getMaxPopulationForAllTime() {
		return maxPopulationForAllTime;
	}

	public long getDiedFromOverpopulation() {
		return diedFromOverpopulation;
	}

	public void setDiedFromOverpopulation(long diedFromOverpopulation) {
		this.diedFromOverpopulation = diedFromOverpopulation;
	}

	public void setMaxPopulationForAllTime(long maxPopulationForAllTime) {
		this.maxPopulationForAllTime = maxPopulationForAllTime;
	}

	public void addDiedFromDisease(long diedFromDisease) {
		this.diedFromDisease += diedFromDisease;
	}

	public void addDiedFromHunger(long diedFromHunger) {
		this.diedFromHunger += diedFromHunger;
	}

	public void addDiedFromOverpopulation(long diedFromOverpopulation) {
		this.diedFromOverpopulation += diedFromOverpopulation;
	}

	public void addDiedFromDisease(double d) {
		addDiedFromDisease((long) d);
	}

	public void addDiedFromHunger(double d) {
		addDiedFromHunger((long) d);
	}

	public void addDiedFromOverpopulation(double d) {
		addDiedFromOverpopulation((long) d);
	}

}
