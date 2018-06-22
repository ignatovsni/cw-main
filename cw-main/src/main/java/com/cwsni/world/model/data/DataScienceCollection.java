package com.cwsni.world.model.data;

public class DataScienceCollection {
	
	private DataScience agriculture = new DataScience();
	private DataScience medicine = new DataScience();

	public void cloneFrom(DataScienceCollection from) {
		agriculture = from.getAgriculture().createClone();
		medicine = from.getMedicine().createClone();
	}
	
	public DataScience getAgriculture() {
		return agriculture;
	}

	public void setAgriculture(DataScience agriculture) {
		this.agriculture = agriculture;
	}	

	public DataScience getMedicine() {
		return medicine;
	}

	public void setMedicine(DataScience medicine) {
		this.medicine = medicine;
	}

}
