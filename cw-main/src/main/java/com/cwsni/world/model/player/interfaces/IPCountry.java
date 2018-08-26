package com.cwsni.world.model.player.interfaces;

import java.util.Collection;
import java.util.Set;

public interface IPCountry {

	int getId();

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

	IPArmy findArmyById(int armyId);

	Collection<IPArmy> findArmiesInProv(IPProvince province);

	double getArmySoldiersToPopulationForSubjugation();

	void setCapital(IPProvince candidate);

}