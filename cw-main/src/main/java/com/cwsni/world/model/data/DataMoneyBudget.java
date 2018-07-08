package com.cwsni.world.model.data;

import com.cwsni.world.client.desktop.util.DataFormatter;

public class DataMoneyBudget {

	private static double MAX_MONEY = Math.pow(1000, 2);

	private double savingsMoney;

	private double savingWeight;
	private double scienceWeight;
	private double armyWeight;

	public DataMoneyBudget() {
		savingWeight = 1;
		scienceWeight = 1;
		armyWeight = 1;
	}

	public double getMoney() {
		return savingsMoney;
	}

	public void setMoney(double money) {
		this.savingsMoney = Math.min(DataFormatter.doubleWith3points(money), MAX_MONEY);
	}

	public double getSavingWeight() {
		return savingWeight;
	}

	public void setSavingWeight(double savingWeight) {
		this.savingWeight = savingWeight;
	}

	public double getScienceWeight() {
		return scienceWeight;
	}

	public void setScienceWeight(double scienceWeight) {
		this.scienceWeight = scienceWeight;
	}

	public double getArmyWeight() {
		return armyWeight;
	}

	public void setArmyWeight(double armyWeight) {
		this.armyWeight = armyWeight;
	}

}
