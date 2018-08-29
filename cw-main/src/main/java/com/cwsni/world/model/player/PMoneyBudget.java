package com.cwsni.world.model.player;

import com.cwsni.world.game.commands.CommandSetMoneyBudget;
import com.cwsni.world.model.engine.MoneyBudget;
import com.cwsni.world.model.player.interfaces.IPMoneyBudget;

public class PMoneyBudget implements IPMoneyBudget {

	private MoneyBudget budget;
	private double savingWeight;
	private double scienceWeight;
	private double armyWeight;
	private double provinceTax;

	private CommandSetMoneyBudget cmdMoneyBudget;
	private PCountry country;

	public PMoneyBudget(PCountry country, MoneyBudget budget) {
		this.country = country;
		this.budget = budget;
		this.savingWeight = budget.getSavingWeight();
		this.scienceWeight = budget.getScienceWeight();
		this.armyWeight = budget.getArmyWeight();
		this.provinceTax = budget.getProvinceTax();
	}

	@Override
	public double getAvailableMoneyForArmy() {
		return budget.getAvailableMoneyForArmy();
	}

	@Override
	public double getMoney() {
		return budget.getMoney();
	}

	@Override
	public double getSavingWeight() {
		return savingWeight;
	}

	@Override
	public void setSavingWeight(double savingWeight) {
		this.savingWeight = savingWeight;
		getCmdMoneyBudget().setSavingWeight(savingWeight);
	}

	@Override
	public double getScienceWeight() {
		return scienceWeight;
	}

	@Override
	public void setScienceWeight(double scienceWeight) {
		this.scienceWeight = scienceWeight;
		getCmdMoneyBudget().setScienceWeight(scienceWeight);
	}

	@Override
	public double getArmyWeight() {
		return armyWeight;
	}

	@Override
	public void setArmyWeight(double armyWeight) {
		this.armyWeight = armyWeight;
		getCmdMoneyBudget().setArmyWeight(armyWeight);
	}

	@Override
	public double getProvinceTax() {
		return provinceTax;
	}

	@Override
	public void setProvinceTax(double provinceTax) {
		this.provinceTax = provinceTax;
		getCmdMoneyBudget().setProvinceTax(provinceTax);
	}

	private CommandSetMoneyBudget getCmdMoneyBudget() {
		if (cmdMoneyBudget == null) {
			cmdMoneyBudget = new CommandSetMoneyBudget();
		}
		((PGame) country.getGame()).addCommand(cmdMoneyBudget);
		return cmdMoneyBudget;
	}

}
