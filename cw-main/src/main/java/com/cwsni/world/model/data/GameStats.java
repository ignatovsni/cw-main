package com.cwsni.world.model.data;

public class GameStats {

	private long maxPopulationForAllTime;
	private long diedFromDisease;
	private long diedFromHunger;

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

	public void setMaxPopulationForAllTime(long maxPopulationForAllTime) {
		this.maxPopulationForAllTime = maxPopulationForAllTime;
	}
	
	public void addDiedFromDisease(long diedFromDisease) {
		this.diedFromDisease += diedFromDisease;
	}
	
	public void addDiedFromHunger(long diedFromHunger) {
		this.diedFromHunger += diedFromHunger;
	}

	public void addDiedFromDisease(double d) {
		addDiedFromDisease((long)d);
	}

	public void addDiedFromHunger(double d) {
		addDiedFromHunger((long)d);
	}

}
