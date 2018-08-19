package com.cwsni.world.game.commands;

import com.cwsni.world.CwException;
import com.cwsni.world.model.Army;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.interfaces.IPGameParams;

public class CommandArmySplit extends CommandArmy {

	private int newArmyId;
	private int soldiers;

	public CommandArmySplit(int armyId, int newArmyId, int soldiers) {
		super(armyId);
		this.newArmyId = newArmyId;
		this.soldiers = soldiers;
		if (newArmyId >= 0) {
			throw new CwException("armyId for new army must be < 0");
		}
	}

	@Override
	public void apply() {
		if (newArmyId >= 0) {
			addError("armyId for new army must be < 0");
			return;
		}
		if (soldiers <= 0) {
			return;
		}
		Game game = country.getGame();
		Army createdArmy = game.findArmyByIdForCommand(country.getId(), newArmyId);
		if (createdArmy != null) {
			addError("army with id = " + newArmyId + " is already created");
			return;
		}
		Army army = getAndCheckArmy(armyId);
		if (army == null) {
			return;
		}
		if (army.getLocationId() == null) {
			addError("army can be splitted only if it has a location");
			return;
		}
		if (soldiers >= army.getSoldiers()) {
			addError("new army must have less soldiers than original (" + army.getSoldiers() + ")");
			return;
		}
		Army newArmy = country.splitArmy(army, soldiers);
		country.getGame().registerNewArmyWithIdLessThanZero(country.getId(), newArmyId, newArmy);
	}

	@Override
	public Object apply(PCountry country, CommandErrorHandler errorHandler) {
		if (newArmyId >= 0) {
			addError("armyId for new army must be < 0");
			return null;
		}
		if (country.findArmyById(newArmyId) != null) {
			addError("army with id = " + newArmyId + " is already created");
			return null;
		}
		PArmy army = getAndCheckArmy(country, armyId);
		if (army == null) {
			return null;
		}
		if (army.getLocation() == null) {
			addError("army can be splitted only if it has a location");
			return null;
		}
		if (soldiers >= army.getSoldiers()) {
			addError("new army must have less soldiers than original (" + army.getSoldiers() + ")");
			return null;
		}
		IPGameParams gParams = country.getGame().getGameParams();
		if (soldiers < gParams.getArmyMinAllowedSoldiers()) {
			addError("soldiers <= gParams.getArmyMinAllowedSoldiers() ; " + soldiers + " < "
					+ gParams.getArmyMinAllowedSoldiers());
			return null;
		}
		if ((army.getSoldiers() - soldiers) < gParams.getArmyMinAllowedSoldiers()) {
			addError("(army.getSoldiers()-soldiers) <= gParams.getArmyMinAllowedSoldiers() ; "
					+ (army.getSoldiers() - soldiers) + " < " + gParams.getArmyMinAllowedSoldiers());
			return null;
		}
		return country.cmcSplitArmy(army, newArmyId, soldiers);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" newArmyId:");
		sb.append(newArmyId);
		sb.append(" soldiersToNewArmy:");
		sb.append(soldiers);
		return sb.toString();
	}

}
