package com.cwsni.world.game.commands;

import com.cwsni.world.model.Army;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPCountry;

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
	public void apply(Country country, CommandErrorHandler errorHandler) {
		if (howManySoldiers == 0) {
			return;
		}
		Game game = country.getGame();
		Army army = game.findArmyById(armyId);
		if (army == null) {
			errorHandler.addError(this.toString() + ": army = null");
			return;
		}
		if (howManySoldiers < 0 || howManySoldiers >= army.getSoldiers()) {
			country.dismissArmy(army);
		} else {
			army.dismissSoldiers(howManySoldiers);
		}
	}

	@Override
	public void apply(PCountry country, CommandErrorHandler errorHandler) {
		if (howManySoldiers == 0) {
			return;
		}
		PArmy army = (PArmy) country.findArmyById(armyId);
		if (army == null) {
			errorHandler.addError(this.toString() + ": army = null");
			return;
		}
		if (howManySoldiers < 0 || howManySoldiers >= army.getSoldiers()) {
			country.cpDismissArmy(army);
		} else {
			army.cpDismissSoldiers(howManySoldiers);
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

}
