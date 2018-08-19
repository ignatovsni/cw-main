package com.cwsni.world.model.player.interfaces;

import java.util.Collection;

public interface IPCountry {

	int getId();

	Collection<IPArmy> getArmies();

	IPProvince getCapital();

	IPProvince getFirstCapital();

	Collection<IPProvince> getProvinces();

	IPMoneyBudget getMoneyBudget();

	IPScienceBudget getScienceBudget();

	Collection<IPProvince> getNeighborsProvs();

	IPArmy createArmy(int provinceId, int soldiers);

	String getAiScriptName();

	IPArmy findArmyById(int armyId);

	Collection<IPArmy> findArmiesInProv(IPProvince province);

	double getArmySoldiersToPopulationForSubjugation();

	void setCapital(IPProvince candidate);

}