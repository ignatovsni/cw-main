package com.cwsni.world.model.data;

import java.util.HashMap;
import java.util.Map;

import com.cwsni.world.client.desktop.util.DataFormatter;

public class DataPopulation {

	public static Double LOYALTY_MAX = 1.0;

	private int amount;
	private double recruitedPercent;
	private DataScienceCollection science = new DataScienceCollection();
	private DataCulture culture = new DataCulture();
	private double wealth;
	private Map<Integer, Double> loyaltyToCountries = new HashMap<>();
	private Map<Integer, Double> loyaltyToStates = new HashMap<>();
	private Map<Integer, Integer> lifeInCountries = new HashMap<>();
	/**
	 * died people by different reasons (mostly fights and diseases)
	 */
	private long casualties;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		if (amount < 0) {
			System.out.println("amount < 0");
			amount = 0;
		}
		this.amount = amount;
	}

	public DataScienceCollection getScience() {
		return science;
	}

	public void setScience(DataScienceCollection science) {
		this.science = science;
	}

	public DataCulture getCulture() {
		return culture;
	}

	public void setCulture(DataCulture culture) {
		this.culture = culture;
	}

	public double getWealth() {
		return wealth;
	}

	public void setWealth(double wealth) {
		this.wealth = DataFormatter.doubleWith2points(wealth);
	}

	public Map<Integer, Double> getLoyaltyToCountries() {
		return loyaltyToCountries;
	}

	public void setLoyaltyToCountries(Map<Integer, Double> loyaltyToCountries) {
		this.loyaltyToCountries = loyaltyToCountries;
	}

	public Map<Integer, Double> getLoyaltyToStates() {
		return loyaltyToStates;
	}

	public void setLoyaltyToStates(Map<Integer, Double> loyaltyToStates) {
		this.loyaltyToStates = loyaltyToStates;
	}

	public Map<Integer, Integer> getLifeInCountries() {
		return lifeInCountries;
	}

	public void setLifeInCountries(Map<Integer, Integer> lifeInCountries) {
		this.lifeInCountries = lifeInCountries;
	}

	public double getRecruitedPercent() {
		return recruitedPercent;
	}

	public void setRecruitedPercent(double recruitedPercent) {
		recruitedPercent = Math.min(1, Math.max(0, recruitedPercent));
		this.recruitedPercent = DataFormatter.doubleWith4points(recruitedPercent);
	}

	public long getCasualties() {
		return casualties;
	}

	public void setCasualties(long casualties) {
		this.casualties = casualties;
	}

}
