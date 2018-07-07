package com.cwsni.world.game.commands;

import com.cwsni.world.model.Country;

abstract public class CommandArmy extends Command {

	protected int armyId;

	public CommandArmy(int armyId) {
		this.armyId = armyId;
	}

	public int getArmyId() {
		return armyId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(".");
		sb.append(" armyId:");
		sb.append(armyId);
		return sb.toString();
	}

	@Override
	abstract public void apply(Country country, CommandErrorHandler errorHandler);

}
