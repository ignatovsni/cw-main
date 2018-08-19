package com.cwsni.world.model;

import com.cwsni.world.model.data.DataScienceBudget;

public class ScienceBudget {

	private DataScienceBudget data;

	public void buildFrom(Country country, DataScienceBudget budget) {
		this.data = budget;
	}

	public double getAgricultureWeight() {
		return data.getAgricultureWeight();
	}

	public void setAgricultureWeight(double agricultureWeight) {
		data.setAgricultureWeight(agricultureWeight);
	}

	public double getAdministrationWeight() {
		return data.getAdministrationWeight();
	}

	public void setAdministrationWeight(double administrationWeight) {
		data.setAdministrationWeight(administrationWeight);
	}

	public double getMedicineWeight() {
		return data.getMedicineWeight();
	}

	public void setMedicineWeight(double medicineWeight) {
		data.setMedicineWeight(medicineWeight);
	}

	public double getTotalWeight() {
		double tw = data.getAgricultureWeight() + data.getAdministrationWeight() + data.getMedicineWeight();
		if (tw<=0) {
			tw = 1;
		}
		return tw;
	}

	private double calcHowToIncreaseScience(double v) {
		if (v > 0) {
			return Math.log(v);
		} else {
			return 0;
		}
	}

	public double getAdministrationFraction(double money) {
		return calcHowToIncreaseScience(money * getAdministrationWeight() / getTotalWeight());
	}

	public double getAgricultureFraction(double money) {
		return calcHowToIncreaseScience(money * getAgricultureWeight() / getTotalWeight());
	}

	public double getMedicineFraction(double money) {
		return calcHowToIncreaseScience(money * getMedicineWeight() / getTotalWeight());
	}

}
