package com.cwsni.world.model.data;

public class GameStats {

	private double maxPopulationForAllTime;
	private double diedFromHunger;
	private double diedFromOverpopulation;
	private double diedInBattles;
	private double diedFromInvasion;

	public double getDiedFromHunger() {
		return diedFromHunger;
	}

	public void setDiedFromHunger(double diedFromHunger) {
		this.diedFromHunger = diedFromHunger;
	}

	public double getMaxPopulationForAllTime() {
		return maxPopulationForAllTime;
	}

	public double getDiedFromOverpopulation() {
		return diedFromOverpopulation;
	}

	public void setDiedFromOverpopulation(double diedFromOverpopulation) {
		this.diedFromOverpopulation = diedFromOverpopulation;
	}

	public void setMaxPopulationForAllTime(double maxPopulationForAllTime) {
		this.maxPopulationForAllTime = maxPopulationForAllTime;
	}

	public double getDiedInBattles() {
		return diedInBattles;
	}

	public void setDiedInBattles(double diedInBattles) {
		this.diedInBattles = diedInBattles;
	}

	public double getDiedFromInvasion() {
		return diedFromInvasion;
	}

	public void setDiedFromInvasion(double diedFromInvasion) {
		this.diedFromInvasion = diedFromInvasion;
	}

	public void addDiedFromHunger(double diedFromHunger) {
		this.diedFromHunger += diedFromHunger;
	}

	public void addDiedFromOverpopulation(double diedFromOverpopulation) {
		this.diedFromOverpopulation += diedFromOverpopulation;
	}

	public void addDiedInBattles(double diedInBattles) {
		this.diedInBattles += diedInBattles;
	}

	public void addDiedFromInvasion(double diedFromInvasion) {
		this.diedFromInvasion += diedFromInvasion;
	}



}
