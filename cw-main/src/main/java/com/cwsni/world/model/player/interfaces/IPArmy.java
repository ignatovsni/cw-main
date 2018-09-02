package com.cwsni.world.model.player.interfaces;

import java.util.List;

public interface IPArmy {

	IPProvince getLocation();

	IPCountry getCountry();

	int getSoldiers();

	int getStrength();

	void moveTo(IPProvince destination);

	void moveTo(List<? extends Object> path);

	boolean isCanMove();

	double getCostForSoldierPerYear();

	double getCostPerYear();

	void dismiss();

	/**
	 * 
	 * @param howManySoldiersNeedToDismiss
	 *            if < 0 then dismiss all
	 */
	void dismiss(int howManySoldiersNeedToDismiss);

	IPArmy splitArmy(int soldiersToNewArmy);

	void merge(IPArmy fromArmy);

	void merge(IPArmy fromArmy, int soldiers);

	int getId();

}