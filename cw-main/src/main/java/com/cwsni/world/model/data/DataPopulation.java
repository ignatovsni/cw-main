package com.cwsni.world.model.data;

import java.util.HashMap;
import java.util.Map;

public class DataPopulation {

	public static Double LOYALTY_MAX = 1.0;

	private int amount;
	private DataScienceCollection science = new DataScienceCollection();
	private DataCulture culture = new DataCulture();
	private double wealth;
	private Map<Integer, Double> countriesLoyalty = new HashMap<>();
	private Map<Integer, Double> statesLoyalty = new HashMap<>();

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

	public Map<Integer, Double> getCountriesLoyalty() {
		return countriesLoyalty;
	}

	public void setCountriesLoyalty(Map<Integer, Double> countriesLoyalty) {
		this.countriesLoyalty = countriesLoyalty;
	}

	public Map<Integer, Double> getStatesLoyalty() {
		return statesLoyalty;
	}

	public void setStatesLoyalty(Map<Integer, Double> statesLoyalty) {
		this.statesLoyalty = statesLoyalty;
	}

}
