package com.cwsni.world.game.commands;

import com.cwsni.world.model.engine.Army;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.Province;

public class CommandArmyMove extends CommandArmy {

	private Integer destinationProvId;

	public CommandArmyMove(long armyId, Integer destinationProvId) {
		super(armyId);
		this.destinationProvId = destinationProvId;
	}

	public Integer getDestinationProvId() {
		return destinationProvId;
	}

	@Override
	public void apply() {
		Game game = country.getGame();
		Army army = getAndCheckArmy(armyId);
		if (army == null) {
			return;
		}
		Province destination = game.getMap().findProvById(destinationProvId);
		if (destination == null) {
			addError("destination province not found. id = " + destinationProvId);
			return;
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
