package com.cwsni.world.model.engine;

import java.util.Map;

import com.cwsni.world.model.data.DataMoneyBudget;
import com.cwsni.world.model.engine.relationships.RTribute;

public class MoneyBudget {

	private DataMoneyBudget data;
	private Country country;

	private double baseIncome;
	private double totalIncome;
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
		calculateBaseBudget();
	}

	protected void calculateBaseBudget() {
		baseIncome = country.getProvinces().stream().mapToDouble(p -> p.getFederalIncomePerYear()).sum();
		armyCost = country.getArmies().stream().mapToDouble(a -> a.getCostPerYear()).sum();
		totalIncome = baseIncome;
		availableMoneyForArmy = totalIncome * data.getArmyWeight() / getTotalWeight() - armyCost;
	}

	private void addMoneyFromTribute(double money) {
		totalIncome += money;
		availableMoneyForArmy += money * data.getArmyWeight() / getTotalWeight();
	}

	protected void calculateBudgetWithAgreements() {
		Map<Integer, RTribute> tributes = country.getGame().getRelationships().getCountriesWithTribute(country.getId());
		for (RTribute tribute : tributes.values()) {
			if (ComparisonTool.isEqual(tribute.getSlaveId(), country.getId())) {
				Country masterCountry = country.getGame().findCountryById(tribute.getMasterId());
				if (masterCountry != null) {
					double money = baseIncome * tribute.getTax();
					masterCountry.getMoneyBudget().addMoneyFromTribute(money);
					addMoneyFromTribute(-money);
				}
			}
		}
	}

	public void processNewTurn() {
		data.setMoney(
				data.getMoney() + totalIncome * data.getSavingWeight() / getTotalWeight() + availableMoneyForArmy);
	}

	private double getTotalWeight() {
		return data.getSavingWeight() + data.getScienceWeight() + data.getArmyWeight();
	}

	public double getAvailableMoneyForScience() {
		return totalIncome * getScienceWeight() / getTotalWeight();
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
		return totalIncome;
	}

	void resetBudgetForRestoredCountry() {
		data.setMoney(0.0);
	}

	public void addMoneyForNewRebelCountry(int populationLoyaltyRebelNewCountriesTakeMoneyForYears) {
		calculateBaseBudget();
		data.setMoney(totalIncome * populationLoyaltyRebelNewCountriesTakeMoneyForYears);
	}

}
