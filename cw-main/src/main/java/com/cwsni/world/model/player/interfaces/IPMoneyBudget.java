package com.cwsni.world.model.player.interfaces;

public interface IPMoneyBudget {

	double getAvailableMoneyForArmy();

	double getMoney();

	double getProvinceTax();

	double getArmyWeight();

	double getScienceWeight();

	double getSavingWeight();

	void setProvinceTax(double provinceTax);

	void setArmyWeight(double armyWeight);

	void setScienceWeight(double scienceWeight);

	void setSavingWeight(double savingWeight);

}