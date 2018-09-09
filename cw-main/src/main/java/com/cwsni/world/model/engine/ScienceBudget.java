package com.cwsni.world.model.engine;

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
		if (tw <= 0) {
			tw = 1;
		}
		return tw;
	}

	public static double calcHowToIncreaseScience(Game game, double v) {
		if (v > 0) {
			// v - per turn here, we need to scale for equal increasing because we use
			// Math.log.
			// Math.log (50) != Math.log(1) / 50
			double coeff = 1.0 * TimeMode.YEAR.getDateTurnPerTime() / game.getTurn().getTimeMode().getDateTurnPerTime();
			return Math.max(0, Math.log(v * coeff) / coeff);
		} else {
			return 0;
		}
	}

	public double getAdministrationFraction(Game game, double money) {
		return calcHowToIncreaseScience(game, money * getAdministrationWeight() / getTotalWeight());
	}

	public double getAgricultureFraction(Game game, double money) {
		return calcHowToIncreaseScience(game, money * getAgricultureWeight() / getTotalWeight());
	}

	public double getMedicineFraction(Game game, double money) {
		return calcHowToIncreaseScience(game, money * getMedicineWeight() / getTotalWeight());
	}

}
