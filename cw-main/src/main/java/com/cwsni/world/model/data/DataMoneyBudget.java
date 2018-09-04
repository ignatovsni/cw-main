package com.cwsni.world.model.data;

import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataMoneyBudget {

	private static double MAX_MONEY = Math.pow(1000, 6);

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double money;

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double savingWeight;

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double scienceWeight;

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double armyWeight;

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 2)
	private double provinceTax;

	public DataMoneyBudget() {
		savingWeight = 1;
		scienceWeight = 1;
		armyWeight = 1;
		provinceTax = 0.5;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = Math.min(money, MAX_MONEY);
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

	public double getProvinceTax() {
		return provinceTax;
	}

	public void setProvinceTax(double provinceTax) {
		this.provinceTax = Math.max(0, Math.min(1, provinceTax));
	}

}
