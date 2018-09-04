package com.cwsni.world.model.data;

import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataScienceBudget {

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double agricultureWeight;
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double administrationWeight;
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double medicineWeight;

	public DataScienceBudget() {
		agricultureWeight = 1;
		administrationWeight = 1;
		medicineWeight = 1;
	}

	public double getAgricultureWeight() {
		return agricultureWeight;
	}

	public void setAgricultureWeight(double agricultureWeight) {
		this.agricultureWeight = agricultureWeight;
	}

	public double getAdministrationWeight() {
		return administrationWeight;
	}

	public void setAdministrationWeight(double administrationWeight) {
		this.administrationWeight = administrationWeight;
	}

	public double getMedicineWeight() {
		return medicineWeight;
	}

	public void setMedicineWeight(double medicineWeight) {
		this.medicineWeight = medicineWeight;
	}

}
