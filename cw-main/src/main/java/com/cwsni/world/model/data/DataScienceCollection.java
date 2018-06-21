package com.cwsni.world.model.data;

public class DataScienceCollection {
	
	private DataScience agriculture = new DataScience();

	public DataScience getAgriculture() {
		return agriculture;
	}

	public void setAgriculture(DataScience agriculture) {
		this.agriculture = agriculture;
	}

	public void cloneFrom(DataScienceCollection from) {
		agriculture = from.getAgriculture().createClone();
	}

}
