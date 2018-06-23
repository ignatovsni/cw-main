package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DataScienceCollection {

	private DataScience agriculture;
	private DataScience medicine;

	public DataScienceCollection() {
		agriculture = new DataScience();
		medicine = new DataScience();
	}

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

	// ---------------- static section ------------------
	private static List<Function<DataScienceCollection, DataScience>> getter4Science;

	public static List<Function<DataScienceCollection, DataScience>> allGetter4Science() {
		return getter4Science;
	}

	static {
		List<Function<DataScienceCollection, DataScience>> listOfFunctions = new ArrayList<>(2);
		listOfFunctions.add(dsc -> dsc.getAgriculture());
		listOfFunctions.add(dsc -> dsc.getMedicine());
		getter4Science = Collections.unmodifiableList(listOfFunctions);
	}

}
