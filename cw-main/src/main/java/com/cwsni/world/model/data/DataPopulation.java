package com.cwsni.world.model.data;

public class DataPopulation {

	private int amount;
	private DataScienceCollection science = new DataScienceCollection();
	private DataCulture culture = new DataCulture();
	private double wealth;

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
	
}
