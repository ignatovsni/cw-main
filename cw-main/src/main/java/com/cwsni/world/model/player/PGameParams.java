package com.cwsni.world.model.player;

import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.player.interfaces.IPGameParams;

public class PGameParams implements IPGameParams {

	private GameParams params;

	public PGameParams(GameParams gameParams) {
		this.params = gameParams;
	}

	@Override
	public double getBudgetBaseCostPerSoldier() {
		return params.getBudgetBaseCostPerSoldier();
	}

	@Override
	public int getArmyMinAllowedSoldiers() {
		return params.getArmyMinAllowedSoldiers();
	}

	@Override
	public double getBudgetBaseHiringCostPerSoldier() {
		return params.getBudgetBaseHiringCostPerSoldier();
	}

}
