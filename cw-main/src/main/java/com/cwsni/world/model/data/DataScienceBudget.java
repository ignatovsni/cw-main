package com.cwsni.world.model.data;

import com.cwsni.world.client.desktop.util.DataFormatter;

public class DataScienceBudget {

	private double agricultureWeight;
	private double administrationWeight;
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
		this.agricultureWeight = DataFormatter.doubleWith3points(agricultureWeight);
	}

	public double getAdministrationWeight() {
		return administrationWeight;
	}

	public void setAdministrationWeight(double administrationWeight) {
		this.administrationWeight = DataFormatter.doubleWith3points(administrationWeight);
	}

	public double getMedicineWeight() {
		return medicineWeight;
	}

	public void setMedicineWeight(double medicineWeight) {
		this.medicineWeight = DataFormatter.doubleWith3points(medicineWeight);
	}

}
