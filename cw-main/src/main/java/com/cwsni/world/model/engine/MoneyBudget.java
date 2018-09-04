package com.cwsni.world.model.engine;

import java.util.Map;

import com.cwsni.world.model.data.DataMoneyBudget;
import com.cwsni.world.model.engine.relationships.RTribute;
import com.cwsni.world.util.ComparisonTool;

public class MoneyBudget {

	private DataMoneyBudget data;
	private Country country;

	private double incomePerYear;
	private double incomePerTurn;
	private double baseIncomePerTurn;

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
		incomePerYear = country.getProvinces().stream().mapToDouble(p -> p.getFederalIncomePerYear()).sum();
		baseIncomePerTurn = getTurn().addPerYear(incomePerYear);
		incomePerTurn = baseIncomePerTurn;
	}

	private void addMoneyFromTribute(double money) {
		incomePerTurn += money;
	}

	protected void calculateBudgetWithAgreements() {
		Map<Integer, RTribute> tributes = country.getGame().getRelationships().getCountriesWithTribute(country.getId());
		for (RTribute tribute : tributes.values()) {
			if (ComparisonTool.isEqual(tribute.getSlaveId(), country.getId())) {
				Country masterCountry = country.getGame().findCountryById(tribute.getMasterId());
				if (masterCountry != null) {
					double money = baseIncomePerTurn * tribute.getTax();
					masterCountry.getMoneyBudget().addMoneyFromTribute(money);
					addMoneyFromTribute(-money);
				}
			}
		}
	}

	public void processNewTurn() {
		double armyCostPerTurn = getTurn()
				.addPerYear(country.getArmies().stream().mapToDouble(a -> a.getCostPerYear()).sum());
		data.setMoney(data.getMoney() + incomePerTurn - armyCostPerTurn);
	}

	public double getTotalWeight() {
		return data.getSavingWeight() + data.getScienceWeight() + data.getArmyWeight();
	}

	public double getAvailableMoneyForScience() {
		return incomePerTurn * getScienceWeight() / getTotalWeight();
	}

	public void spendMoneyForArmy(double money) {
		data.setMoney(data.getMoney() - money);
	}

	public void spendMoneyForScience(double money) {
		data.setMoney(data.getMoney() - money);
	}

	public double getMoney() {
		return data.getMoney();
	}

	public double getIncomePerYear() {
		return incomePerYear;
	}

	void resetBudgetForRestoredCountry() {
		data.setMoney(0.0);
	}

	public void addMoneyForNewRebelCountry(int populationLoyaltyRebelNewCountriesTakeMoneyForYears) {
		calculateBaseBudget();
		data.setMoney(incomePerYear * populationLoyaltyRebelNewCountriesTakeMoneyForYears);
	}

	protected Turn getTurn() {
		return country.getGame().getTurn();
	}

}
