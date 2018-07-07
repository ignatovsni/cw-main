package com.cwsni.world.game.commands;

import com.cwsni.world.model.Army;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;

public class CommandArmyDismiss extends CommandArmy {

	/**
	 * if < 0 then dismiss all
	 */
	private int howManySoldiers;

	public CommandArmyDismiss(int armyId, int howManySoldiersNeedToDismiss) {
		super(armyId);
		howManySoldiers = howManySoldiersNeedToDismiss;
	}

	@Override
	public void apply(Country country, CommandErrorHandler errorHandler) {
		Game game = country.getGame();
		Army army = game.findArmyById(armyId);
		if (army == null) {
			errorHandler.addError(this.toString() + ": army = null");
			return;
		}
		if (howManySoldiers < 0) {
			country.dismissArmy(army);
		} else {
			army.dismissSoldiers(howManySoldiers);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" howManySoldiers:");
		sb.append(howManySoldiers);
		return sb.toString();
	}
	
	public boolean isFullDismiss() {
		return howManySoldiers < 0;
	}

}
