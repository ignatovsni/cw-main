package com.cwsni.world.game.commands;

import com.cwsni.world.model.engine.Army;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.util.ComparisonTool;

abstract public class CommandArmy extends Command {

	protected long armyId;

	public CommandArmy(long armyId) {
		this.armyId = armyId;
	}

	public long getArmyId() {
		return armyId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" armyId:");
		sb.append(armyId);
		return sb.toString();
	}

	@Override
	abstract public void apply();

	protected Army getAndCheckArmy(long idOfArmy) {
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

	protected PArmy getAndCheckArmy(IPCountry country, long idOfArmy) {
		PArmy army = (PArmy) country.findArmyById(idOfArmy);
		if (army == null) {
			addError("army = null");
			return null;
		}
		return army;
	}

}
