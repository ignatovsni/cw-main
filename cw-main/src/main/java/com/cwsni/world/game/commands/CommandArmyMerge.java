package com.cwsni.world.game.commands;

import com.cwsni.world.model.engine.Army;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.PCountry;

public class CommandArmyMerge extends CommandArmy {

	private int armyFromId;
	private int soldiers;

	public CommandArmyMerge(int armyToId, int armyFromId, int soldiers) {
		super(armyToId);
		this.armyFromId = armyFromId;
		this.soldiers = soldiers;
	}

	@Override
	public void apply() {
		Army army = getAndCheckArmy(armyId);
		Army armyFrom = getAndCheckArmy(armyFromId);
		if (army == null || armyFrom == null) {
			return;
		}
		country.mergeArmy(army, armyFrom, soldiers);
	}

	@Override
	public Object apply(PCountry country, CommandErrorHandler errorHandler) {
		PArmy army = getAndCheckArmy(country, armyId);
		PArmy armyFrom = getAndCheckArmy(country, armyFromId);
		if (army == null || armyFrom == null) {
			return false;
		}
		int realSoldiers = Math.min(soldiers, armyFrom.getSoldiers());
		army.cmcAddSoldiers(realSoldiers);
		armyFrom.cmcAddSoldiers(-realSoldiers);
		if (armyFrom.getSoldiers() <= 0) {
			country.cmcDismissArmy(armyFrom);
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" armyFromId:");
		sb.append(armyFromId);
		sb.append(" soldiers:");
		sb.append(soldiers);
		return sb.toString();
	}

}
