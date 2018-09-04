package com.cwsni.world.model.player.interfaces;

public interface IPMoneyBudget {

	double getMoney();

	double getIncomePerYear();
	
	double getProvinceTax();	

	double getArmyWeight();

	double getScienceWeight();

	double getSavingWeight();
	
	double getTotalWeight();

	void setProvinceTax(double provinceTax);

	void setArmyWeight(double armyWeight);

	void setScienceWeight(double scienceWeight);

	void setSavingWeight(double savingWeight);

}