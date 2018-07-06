package com.cwsni.world.model;

import com.cwsni.world.model.data.DataMoneyBudget;

public class MoneyBudget {

	private DataMoneyBudget data;
	private Country country;

	private double income;
	private double armyCost;
	private double availableMoneyForArmy;

	public double getSavingWeight() {
		return data.getSavingWeight();
	}

	public double getArmyWeight() {
		return data.getArmyWeight();
	}

	public void setArmyWeight(double armyWeight) {
		this.setArmyWeight(armyWeight);
	}

	public void buildFrom(Country country, DataMoneyBudget budget) {
		this.country = country;
		this.data = budget;
		calculateBudget();
	}

	private void calculateBudget() {
		income = country.getProvinces().stream().mapToDouble(p -> p.getFederalIncomePerYear()).sum();
		armyCost = country.getArmies().stream().mapToDouble(a -> a.getCostPerYear()).sum();
		availableMoneyForArmy = income * data.getArmyWeight() / getTotalWeight() - armyCost;
	}

	public void processNewTurn() {
		calculateBudget();
		data.setMoney(data.getMoney() + income * data.getSavingWeight() / getTotalWeight() + availableMoneyForArmy);
	}

	private double getTotalWeight() {
		return data.getSavingWeight() + data.getArmyWeight();
	}

	public double getAvailableMoneyForArmy() {
		return availableMoneyForArmy;
	}

	public double getArmyCost() {
		return armyCost;
	}

	public double getMoney() {
		return data.getMoney();
	}

}
