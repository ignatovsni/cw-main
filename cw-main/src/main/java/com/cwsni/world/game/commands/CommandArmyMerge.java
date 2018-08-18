package com.cwsni.world.game.commands;

import com.cwsni.world.model.Army;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.PCountry;

public class CommandArmyMerge extends CommandArmy {
	// TODO P-model does not use this command yet

	private int armyFromId;
	private int soldiers;

	public CommandArmyMerge(int armyToId, int armyFromId, int soldiers) {
		super(armyToId);
		this.armyFromId = armyFromId;
		this.soldiers = soldiers;
	}

	@Override
	public void apply() {
		Game game = country.getGame();
		Army army = game.findArmyByIdForCommand(country.getId(), armyId);
		if (army == null) {
			addError("army = null");
			return;
		}
		Army armyFrom = game.findArmyByIdForCommand(country.getId(), armyFromId);
		if (armyFrom == null) {
			addError("armyFrom = null");
			return;
		}
		country.mergeArmy(army, armyFrom, soldiers);
	}

	@Override
	public Object apply(PCountry country, CommandErrorHandler errorHandler) {
		PArmy army = (PArmy) country.findArmyById(armyId);
		if (army == null) {
			addError("army = null");
			return false;
		}
		PArmy armyFrom = (PArmy) country.findArmyById(armyFromId);
		if (armyFrom == null) {
			addError("armyFrom = null");
			return false;
		}
		int realSoldiers = Math.min(soldiers, armyFrom.getSoldiers());
		army.cmcAddSoldiers(realSoldiers);
		armyFrom.cmcAddSoldiers(-realSoldiers);
		if (armyFrom.getSoldiers() <= 0) {
			country.cmcDismissArmy(army);
		}
		return true;
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
