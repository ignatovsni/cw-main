package com.cwsni.world.game.commands;

import com.cwsni.world.model.Army;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;

public class CommandArmyMove extends CommandArmy {

	private Integer destinationProvId;

	public CommandArmyMove(int armyId, Integer destinationProvId) {
		super(armyId);
		this.destinationProvId = destinationProvId;
	}

	public Integer getDestinationProvId() {
		return destinationProvId;
	}

	@Override
	public void apply() {
		Game game = country.getGame();
		Army army = game.findArmyByIdForCommand(country.getId(), armyId);
		if (army == null) {
			addError("army = null");
			return;
		}
		if (army.getCountry().getId() != country.getId()) {
			addError("army.countryId = " + army.getCountry().getId() + " but countryId = " + country.getId());
			return;
		}
		Province destination = game.getMap().findProvById(destinationProvId);
		if (destination == null) {
			addError("destination province not found. id = " + destinationProvId);
			return;
		}
		if (!army.getLocation().getNeighbors().contains(destination)) {
			// destination is not neighbor
			// TODO find nearest province to move
			// right now command has only neighbors target
		}
		army.moveTo(destination);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" destinationProvId:");
		sb.append(destinationProvId);
		return sb.toString();
	}

}
