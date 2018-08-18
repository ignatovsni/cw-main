package com.cwsni.world.game.commands;

import com.cwsni.world.CwException;
import com.cwsni.world.model.Army;
import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.PGame;
import com.cwsni.world.model.player.interfaces.IPProvince;

public class CommandArmyCreate extends CommandArmy {

	private Integer destinationProvId;
	private int soldiers;

	public CommandArmyCreate(int armyId, int provinceId, int soldiers) {
		super(armyId);
		if (armyId >= 0) {
			throw new CwException("armyId for new army must be < 0");
		}
		this.destinationProvId = provinceId;
		this.soldiers = soldiers;
	}

	public Integer getDestinationProvId() {
		return destinationProvId;
	}

	@Override
	public void apply() {
		if (armyId >= 0) {
			addError("armyId for new army must be < 0");
			return;
		}
		Game game = country.getGame();
		Army createdArmy = game.findArmyByIdForCommand(country.getId(), armyId);
		if (createdArmy != null) {
			addError("army with id = " + armyId + " is already created");
			return;
		}
		Province destination = game.getMap().findProvById(destinationProvId);
		if (destination == null) {
			addError("destination province not found. id = " + destinationProvId);
			return;
		}
		if (!ComparisonTool.isEqual(destination.getCountryId(), country.getId())) {
			addError("destination country id = " + destination.getCountryId() + " but country.id = " + country.getId());
			return;
		}
		Army army = country.createArmy(destination, soldiers);
		country.getGame().registerNewArmyWithIdLessThanZero(country.getId(), armyId, army);
	}

	@Override
	public Object apply(PCountry country, CommandErrorHandler errorHandler) {
		if (armyId >= 0) {
			addError("armyId for new army must be < 0");
			return null;
		}
		PGame game = country.getGame();
		IPProvince destination = game.findProvById(destinationProvId);
		if (destination == null) {
			addError("destination province not found. id = " + destinationProvId);
			return null;
		}
		if (!ComparisonTool.isEqual(destination.getCountryId(), country.getId())) {
			addError("destination country id = " + destination.getCountryId() + " but country.id = " + country.getId());
			return null;
		}
		return country.cmcCreateArmy(armyId, destinationProvId, soldiers);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" provinceId:");
		sb.append(destinationProvId);
		sb.append(" soldiers:");
		sb.append(soldiers);
		return sb.toString();
	}

}
