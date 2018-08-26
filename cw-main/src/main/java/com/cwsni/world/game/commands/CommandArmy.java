package com.cwsni.world.game.commands;

import com.cwsni.world.model.Army;
import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.interfaces.IPCountry;

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
	abstract public void apply();

	protected Army getAndCheckArmy(int idOfArmy) {
		Army army = country.getGame().findArmyByIdForCommand(country.getId(), idOfArmy);
		if (army == null) {
			addError("army = null");
			return null;
		}
		if (!ComparisonTool.isEqual(army.getCountry().getId(), country.getId())) {
			addError("army.countryId = " + army.getCountry().getId() + " but countryId = " + country.getId());
			return null;
		}
		return army;
	}

	protected PArmy getAndCheckArmy(IPCountry country, int idOfArmy) {
		PArmy army = (PArmy) country.findArmyById(idOfArmy);
		if (army == null) {
			addError("army = null");
			return null;
		}
		return army;
	}

}
