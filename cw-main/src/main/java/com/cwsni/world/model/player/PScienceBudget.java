package com.cwsni.world.model.player;

import com.cwsni.world.game.commands.CommandSetScienceBudget;
import com.cwsni.world.model.engine.ScienceBudget;
import com.cwsni.world.model.player.interfaces.IPScienceBudget;

public class PScienceBudget implements IPScienceBudget {

	private double agricultureWeight;
	private double administrationWeight;
	private double medicineWeight;

	private PCountry country;
	private CommandSetScienceBudget cmdScienceBudget;

	public PScienceBudget(PCountry country, ScienceBudget budget) {
		this.country = country;
		this.agricultureWeight = budget.getAgricultureWeight();
		this.administrationWeight = budget.getAdministrationWeight();
		this.medicineWeight = budget.getMedicineWeight();
	}

	@Override
	public double getAgricultureWeight() {
		return agricultureWeight;
	}

	@Override
	public void setAgricultureWeight(double agricultureWeight) {
		this.agricultureWeight = agricultureWeight;
		getCmdScienceBudget().setAgricultureWeight(agricultureWeight);
	}

	@Override
	public double getAdministrationWeight() {
		return administrationWeight;
	}

	@Override
	public void setAdministrationWeight(double administrationWeight) {
		this.administrationWeight = administrationWeight;
		getCmdScienceBudget().setAdministrationWeight(administrationWeight);
	}

	@Override
	public double getMedicineWeight() {
		return medicineWeight;
	}

	@Override
	public void setMedicineWeight(double medicineWeight) {
		this.medicineWeight = medicineWeight;
		getCmdScienceBudget().setMedicineWeight(medicineWeight);
	}

	private CommandSetScienceBudget getCmdScienceBudget() {
		if (cmdScienceBudget == null) {
			cmdScienceBudget = new CommandSetScienceBudget();
		}
		((PGame)country.getGame()).addCommand(cmdScienceBudget);
		return cmdScienceBudget;
	}

}
