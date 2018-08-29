package com.cwsni.world.model.player.interfaces;

public interface IPGameParams {

	double getBudgetBaseCostPerSoldier();

	int getArmyMinAllowedSoldiers();

	double getBudgetBaseHiringCostPerSoldier();

	IPRandom getRandom();

}