package com.cwsni.world.model.data;

public class DataPopulation {

	private int amount;
	private DataScienceCollection science = new DataScienceCollection();

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
	
}
