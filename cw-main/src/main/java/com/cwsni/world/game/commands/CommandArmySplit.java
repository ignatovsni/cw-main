package com.cwsni.world.game.commands;

import com.cwsni.world.model.Army;
import com.cwsni.world.model.Game;

public class CommandArmySplit extends CommandArmy {
	// TODO P-model does not use this command yet

	private int soldiersToNewArmy;

	public CommandArmySplit(int armyId, int soldiersToNewArmy) {
		super(armyId);
		this.soldiersToNewArmy = soldiersToNewArmy;
	}

	@Override
	public void apply() {
		if (soldiersToNewArmy <= 0) {
			return;
		}
		Game game = country.getGame();
		Army army = game.findArmyByIdForCommand(country.getId(), armyId);
		if (army == null) {
			addError("army = null");
			return;
		}
		if (soldiersToNewArmy >= army.getSoldiers()) {
			return;
		}
		country.splitArmy(army, soldiersToNewArmy);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" soldiersToNewArmy:");
		sb.append(soldiersToNewArmy);
		return sb.toString();
	}

}
