package com.cwsni.world.model.player;

import com.cwsni.world.model.MoneyBudget;

public class PBudget {
	private MoneyBudget budget;

	public PBudget(MoneyBudget budget) {
		this.budget = budget;
	}

	public double getAvailableMoneyForArmy() {
		return budget.getAvailableMoneyForArmy();
	}
	
	public double getMoney() {
		return budget.getMoney();
	}
	

}
