package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.client.desktop.util.DataFormatter;

public class DataPopulation {

	public static Double LOYALTY_MAX = 1.0;

	private int amount;
	private DataScienceCollection science = new DataScienceCollection();
	private DataCulture culture = new DataCulture();
	private double wealth;
	/**
	 * loyalty 0..1
	 */
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

	public void addCountryLoyalty(int id, double delta) {
		addLoyalty(countriesLoyalty, id, delta);
	}

	public void addStateLoyalty(int id, double delta) {
		addLoyalty(statesLoyalty, id, delta);
	}

	private void addLoyalty(Map<Integer, Double> loyalties, int id, double delta) {
		Double currentLoyalty = loyalties.get(id);
		if (currentLoyalty == null) {
			currentLoyalty = 0.0;
		}
		currentLoyalty = Math.min(Math.max(currentLoyalty + delta, 0), LOYALTY_MAX);
		currentLoyalty = DataFormatter.doubleWith4points(currentLoyalty);
		if (currentLoyalty <= LOYALTY_MAX / 1000) {			
			loyalties.remove(id);
		} else {
			loyalties.put(id, currentLoyalty);
		}
	}

	public Double getCountryLoyalty(Integer id) {
		Double loyalty = countriesLoyalty.get(id);
		if (loyalty != null) {
			return loyalty;
		} else {
			return 0.0;
		}
	}

	public Double getStateLoyalty(Integer id) {
		Double loyalty = statesLoyalty.get(id);
		if (loyalty != null) {
			return loyalty;
		} else {
			return 0.0;
		}
	}

	public void addAllCountriesLoyalty(double delta) {
		// We need new List because DataPopulation::addCountryLoyalty can remove
		// elements from collection.
		List<Integer> ids = new ArrayList<>(countriesLoyalty.keySet());
		for (Integer id : ids) {
			addCountryLoyalty(id, delta);
		}
	}

	public void addAllStatesLoyalty(double delta) {
		// We need new List because DataPopulation::addCountryLoyalty can remove
		// elements from collection.
		List<Integer> ids = new ArrayList<>(statesLoyalty.keySet());
		for (Integer id : ids) {
			addStateLoyalty(id, delta);
		}
	}

}
