package com.cwsni.world.model.player.interfaces;

import java.util.Collection;
import java.util.Set;

public interface IPCountry {

	int getId();
	
	boolean isAI();

	Collection<IPArmy> getArmies();

	IPProvince getCapital();

	IPProvince getFirstCapital();

	Collection<IPProvince> getProvinces();

	IPMoneyBudget getMoneyBudget();

	IPScienceBudget getScienceBudget();

	Set<IPProvince> getReachableLandBorderAlienProvs();

	Set<IPProvince> getReachableLandProvincesThroughWater();

	Set<IPProvince> getReachableWaterProvinces();

	Set<IPProvince> getReachableLandAlienProvincesThroughWater();

	IPArmy createArmy(int provinceId, int soldiers);

	String getAiScriptName();

	IPArmy findArmyById(long armyId);

	Collection<IPArmy> findArmiesInProv(IPProvince province);

	double getArmySoldiersToPopulationForSubjugation();

	void setCapital(IPProvince candidate);

	long getPopulationAmount();

	double getFocusLevel();
	
	public double getCasualties();
	
	public double getLoyaltyToCountryFromCountryCasualties();

	double getArmySoldiersToPopulationForSubjugationLeaveInProvince();

	IPCountryPreferences getPreferences();
	

}