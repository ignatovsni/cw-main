package com.cwsni.world.game.commands;

import com.cwsni.world.CwException;
import com.cwsni.world.model.Army;
import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.player.PCountry;

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
	public void apply(Country country, CommandErrorHandler errorHandler) {
		if (armyId >= 0) {
			// We need to check in apply method because command can be created by other application
			throw new CwException("armyId for new army must be < 0");
		}
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
		Army army = country.createArmy(destination, soldiers);
		country.getGame().registerNewArmyWithIdLessThanZero(armyId, army);
	}
	
	@Override
	public void apply(PCountry country, CommandErrorHandler errorHandler) {
		// TODO
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
