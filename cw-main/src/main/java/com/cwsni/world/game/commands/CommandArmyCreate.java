package com.cwsni.world.game.commands;

import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;

public class CommandArmyCreate extends CommandArmy {

	private Integer destinationProvId;
	private int soldiers;

	public CommandArmyCreate(int provinceId, int soldiers) {
		super(-1);
		this.destinationProvId = provinceId;
		this.soldiers = soldiers;
	}

	public Integer getDestinationProvId() {
		return destinationProvId;
	}

	@Override
	public void apply(Country country, CommandErrorHandler errorHandler) {
		Game game = country.getGame();
		Province destination = game.getMap().findProvById(destinationProvId);
		if (destination == null) {
			errorHandler.addError(this.toString() + ": destination province not found. id = " + destinationProvId);
			return;
		}
		if (!ComparisonTool.isEqual(destination.getCountryId(), country.getId())) {
			errorHandler.addError(this.toString() + ": destination country id = " + destination.getCountryId()
					+ " but country.id = " + country.getId());
			return;
		}
		country.createArmy(destination, soldiers);
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
