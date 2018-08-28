package com.cwsni.world.game.commands;

import com.cwsni.world.model.engine.ScienceBudget;

public class CommandSetScienceBudget extends Command {

	private double agricultureWeight;
	private double administrationWeight;
	private double medicineWeight;

	@Override
	public void apply() {
		ScienceBudget budget = country.getScienceBudget();
		if (agricultureWeight >= 0 && agricultureWeight <= 100) {
			budget.setAgricultureWeight(agricultureWeight);
		}
		if (administrationWeight >= 0 && administrationWeight <= 100) {
			budget.setAdministrationWeight(administrationWeight);
		}
		if (medicineWeight >= 0 && medicineWeight <= 100) {
			budget.setMedicineWeight(medicineWeight);
		}
	}

	public void setAgricultureWeight(double agricultureWeight) {
		this.agricultureWeight = agricultureWeight;
	}

	public void setAdministrationWeight(double administrationWeight) {
		this.administrationWeight = administrationWeight;
	}

	public void setMedicineWeight(double medicineWeight) {
		this.medicineWeight = medicineWeight;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" agricultureWeight:");
		sb.append(agricultureWeight);
		sb.append(" administrationWeight:");
		sb.append(administrationWeight);
		sb.append(" medicineWeight:");
		sb.append(medicineWeight);
		return sb.toString();
	}

}
