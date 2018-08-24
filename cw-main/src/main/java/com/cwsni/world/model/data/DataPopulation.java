package com.cwsni.world.model.data;

import java.util.HashMap;
import java.util.Map;

public class DataPopulation {

	public static Double LOYALTY_MAX = 1.0;

	private int amount;
	private DataScienceCollection science = new DataScienceCollection();
	private DataCulture culture = new DataCulture();
	private double wealth;
	private Map<Integer, Double> loyaltyToCountries = new HashMap<>();
	private Map<Integer, Double> loyaltyToStates = new HashMap<>();
	private Map<Integer, Integer> lifeInCountries = new HashMap<>();

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
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
		this.wealth = wealth;
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

}
