package com.cwsni.world.model.player;

import com.cwsni.world.model.data.GameParams;

public class PGameParams {

	private GameParams params;

	public PGameParams(GameParams gameParams) {
		this.params = gameParams;
	}

	public double getBudgetBaseCostPerSoldier() {
		return params.getBudgetBaseCostPerSoldier();
	}

	public int getArmyMinAllowedSoldiers() {
		return params.getArmyMinAllowedSoldiers();
	}

	public double getBudgetBaseHiringCostPerSoldier() {
		return params.getBudgetBaseHiringCostPerSoldier();
	}
}
