package com.cwsni.world.model.player.interfaces;

import java.util.List;

public interface IPArmy {

	IPProvince getLocation();

	IPCountry getCountry();

	int getSoldiers();

	void moveTo(IPProvince destination);

	void moveTo(List<Object> path);

	double getCostForSoldierPerYear();

	double getCostPerYear();

	void dismiss();

	/**
	 * 
	 * @param howManySoldiersNeedToDismiss
	 *            if < 0 then dismiss all
	 */
	void dismissSoldiers(int howManySoldiersNeedToDismiss);

	void splitArmy(int soldiersToNewArmy);

	void merge(IPArmy fromArmy);
	
	void merge(IPArmy fromArmy, int soldiers);

	int getId();

}