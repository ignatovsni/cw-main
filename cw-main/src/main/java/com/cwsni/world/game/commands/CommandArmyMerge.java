package com.cwsni.world.game.commands;

import com.cwsni.world.model.Army;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;

public class CommandArmyMerge extends CommandArmy {
	// TODO P-model does not use this command yet

	private int armyFromId;

	public CommandArmyMerge(int armyToId, int armyFromId) {
		super(armyToId);
		this.armyFromId = armyFromId;
	}

	@Override
	public void apply(Country country, CommandErrorHandler errorHandler) {
		Game game = country.getGame();
		Army army = game.findArmyById(armyId);
		if (army == null) {
			errorHandler.addError(this.toString() + ": army = null");
			return;
		}
		Army armyFrom = game.findArmyById(armyFromId);
		if (armyFrom == null) {
			errorHandler.addError(this.toString() + ": armyFrom = null");
			return;
		}
		country.mergeArmy(army, armyFrom);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" armyFromId:");
		sb.append(armyFromId);
		return sb.toString();
	}

}
