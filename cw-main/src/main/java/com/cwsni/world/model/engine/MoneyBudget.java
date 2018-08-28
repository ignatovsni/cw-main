package com.cwsni.world.model.engine;

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

	public double getScienceWeight() {
		return data.getSavingWeight();
	}

	public double getArmyWeight() {
		return data.getArmyWeight();
	}

	public double getProvinceTax() {
		return data.getProvinceTax();
	}

	public void setSavingWeight(double savingWeight) {
		data.setSavingWeight(savingWeight);
	}

	public void setScienceWeight(double scienceWeight) {
		data.setScienceWeight(scienceWeight);
	}

	public void setArmyWeight(double armyWeight) {
		data.setArmyWeight(armyWeight);
	}

	public void setProvinceTax(double provinceTax) {
		data.setProvinceTax(provinceTax);
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
		return data.getSavingWeight() + data.getScienceWeight() + data.getArmyWeight();
	}

	public double getAvailableMoneyForScience() {
		return income * getScienceWeight() / getTotalWeight();
	}

	public double getAvailableMoneyForArmy() {
		return availableMoneyForArmy;
	}

	public void spendMoneyForArmy(double money) {
		availableMoneyForArmy -= money;
		data.setMoney(data.getMoney() - money);
	}

	public double getArmyCost() {
		return armyCost;
	}

	public double getMoney() {
		return data.getMoney();
	}

	public double getIncome() {
		return income;
	}

	void resetBudgetForRestoredCountry() {
		data.setMoney(0.0);
	}

	public void addMoneyForNewRebelCountry(int populationLoyaltyRebelNewCountriesTakeMoneyForYears) {
		calculateBudget();
		data.setMoney(income * populationLoyaltyRebelNewCountriesTakeMoneyForYears);
	}

}
