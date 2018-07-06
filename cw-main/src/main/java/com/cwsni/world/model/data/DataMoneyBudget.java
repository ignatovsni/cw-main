package com.cwsni.world.model.data;

import com.cwsni.world.client.desktop.util.DataFormatter;

public class DataMoneyBudget {

	private double money;
	private double savingWeight;
	private double armyWeight;

	public DataMoneyBudget() {
		savingWeight = 1;
		armyWeight = 1;
	}

	public double getSavingWeight() {
		return savingWeight;
	}

	public void setSavingWeight(double savingWeight) {
		this.savingWeight = savingWeight;
	}

	public double getArmyWeight() {
		return armyWeight;
	}

	public void setArmyWeight(double armyWeight) {
		this.armyWeight = armyWeight;
	}

	public double getMoney() {
		return money;
	}

	private static double MAX_MONEY = Math.pow(1000, 2);

	public void setMoney(double money) {
		this.money = Math.min(DataFormatter.doubleWith3points(money), MAX_MONEY);
	}

}
