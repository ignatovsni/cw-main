package com.cwsni.world.game.commands;

import com.cwsni.world.model.Army;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.PCountry;

public class CommandArmyDismiss extends CommandArmy {

	/**
	 * if < 0 then dismiss all
	 */
	private int howManySoldiers;

	public CommandArmyDismiss(int armyId, int howManySoldiersNeedToDismiss) {
		super(armyId);
		this.howManySoldiers = howManySoldiersNeedToDismiss;
	}

	@Override
	public void apply() {
		if (howManySoldiers == 0) {
			return;
		}
		Game game = country.getGame();
		Army army = game.findArmyByIdForCommand(country.getId(), armyId);
		if (army == null) {
			addError("army = null");
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
		PArmy army = (PArmy) country.findArmyById(armyId);
		if (army == null) {
			errorHandler.addError(this, "army = null");
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
