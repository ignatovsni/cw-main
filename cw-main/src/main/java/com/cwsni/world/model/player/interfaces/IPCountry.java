package com.cwsni.world.model.player.interfaces;

import java.util.List;

public interface IPCountry {

	int getId();

	List<IPArmy> getArmies();

	IPProvince getCapital();

	IPProvince getFirstCapital();

	List<IPProvince> getProvinces();

	IPBudget getBudget();

	List<IPProvince> getNeighborsProvs();

	IPArmy createArmy(int provinceId, int soldiers);

	String getAiScriptName();

	IPArmy findArmyById(int armyId);

	void setCapital(IPProvince candidate);

}