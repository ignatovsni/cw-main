package com.cwsni.world.game.commands;

import com.cwsni.world.model.MoneyBudget;

public class CommandSetMoneyBudget extends Command {

	private double savingWeight;
	private double scienceWeight;
	private double armyWeight;
	private double provinceTax;

	@Override
	public void apply() {
		MoneyBudget budget = country.getMoneyBudget();
		if (savingWeight >= 0 && savingWeight <= 100) {
			budget.setSavingWeight(savingWeight);
		}
		if (scienceWeight >= 0 && scienceWeight <= 100) {
			budget.setScienceWeight(scienceWeight);
		}
		if (armyWeight >= 0 && armyWeight <= 100) {
			budget.setArmyWeight(armyWeight);
		}
		if (provinceTax >= 0 && provinceTax <= 1) {
			budget.setProvinceTax(provinceTax);
		}
	}

	public void setSavingWeight(double savingWeight) {
		this.savingWeight = savingWeight;
	}

	public void setScienceWeight(double scienceWeight) {
		this.scienceWeight = scienceWeight;
	}

	public void setArmyWeight(double armyWeight) {
		this.armyWeight = armyWeight;
	}

	public void setProvinceTax(double provinceTax) {
		this.provinceTax = provinceTax;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" armyWeight:");
		sb.append(armyWeight);
		sb.append(" scienceWeight:");
		sb.append(scienceWeight);
		sb.append(" savingWeight:");
		sb.append(savingWeight);
		sb.append(" provinceTax:");
		sb.append(provinceTax);
		return sb.toString();
	}

}
