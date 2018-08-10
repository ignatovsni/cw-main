package com.cwsni.world.model.player;

import com.cwsni.world.model.MoneyBudget;
import com.cwsni.world.model.player.interfaces.IPBudget;

public class PBudget implements IPBudget {
	private MoneyBudget budget;

	public PBudget(MoneyBudget budget) {
		this.budget = budget;
	}

	@Override
	public double getAvailableMoneyForArmy() {
		return budget.getAvailableMoneyForArmy();
	}
	
	@Override
	public double getMoney() {
		return budget.getMoney();
	}
	

}
