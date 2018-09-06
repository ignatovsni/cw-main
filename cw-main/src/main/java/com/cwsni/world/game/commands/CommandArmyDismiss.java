package com.cwsni.world.game.commands;

import com.cwsni.world.model.engine.Army;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.PCountry;

public class CommandArmyDismiss extends CommandArmy {

	/**
	 * if < 0 then dismiss all
	 */
	private int howManySoldiers;

	public CommandArmyDismiss(long armyId, int howManySoldiersNeedToDismiss) {
		super(armyId);
		this.howManySoldiers = howManySoldiersNeedToDismiss;
	}

	@Override
	public void apply() {
		if (howManySoldiers == 0) {
			return;
		}
		Army army = getAndCheckArmy(armyId);
		if (army == null) {
			return;
		}
		if (howManySoldiers < 0 || howManySoldiers >= army.getSoldiers()) {
			country.dismissArmy(army);
		} else {
			army.dismissSoldiers(howManySoldiers);
		}
	}

	@Override
	public Object apply(PCountry country, CommandErrorHandler errorHandler) {
		if (howManySoldiers == 0) {
			return false;
		}
		PArmy army = getAndCheckArmy(country, armyId);
		if (army == null) {
			return false;
		}
		if (howManySoldiers < 0 || howManySoldiers >= army.getSoldiers()) {
			country.cmcDismissArmy(army);
		} else {
			army.cmcAddSoldiers(-howManySoldiers);
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" howManySoldiers:");
		sb.append(howManySoldiers);
		return sb.toString();
	}

}
