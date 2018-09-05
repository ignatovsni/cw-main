package com.cwsni.world.game.commands;

import com.cwsni.world.model.engine.Army;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.util.ComparisonTool;

public class CommandArmySubjugate extends CommandArmy {

	public CommandArmySubjugate(int armyId) {
		super(armyId);
	}

	@Override
	public void apply() {
		Army army = getAndCheckArmy(armyId);
		if (army == null) {
			return;
		}
		army.subjugateProvince();
	}

	@Override
	public Object apply(PCountry country, CommandErrorHandler errorHandler) {
		PArmy army = getAndCheckArmy(country, armyId);
		if (army == null) {
			return false;
		}
		IPProvince destination = army.getLocation();
		if (destination == null) {
			addError("the army does not have a location");
			return null;
		}
		if (ComparisonTool.isEqual(destination.getCountryId(), country.getId())) {
			addError("the province " + destination.getId() + " is already belonged to the country.id = "
					+ country.getId());
			return null;
		}
		int soldiersForSubgugateAndLeave = (int) (destination.getPopulationAmount()
				* country.getArmySoldiersToPopulationForSubjugationLeaveInProvince());
		army.cmcAddSoldiers(-soldiersForSubgugateAndLeave);
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		return sb.toString();
	}

}
