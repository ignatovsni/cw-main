package com.cwsni.world.game.commands;

public class CommandDiplomacyWar extends CommandDiplomacy {

	public CommandDiplomacyWar(int targetCountryId) {
		super(targetCountryId);
	}

	@Override
	public void apply() {
		if (!checkTargetCountry()) {
			return;
		}
		getGame().getRelationships().newWar(getCountryId(), targetCountryId);
	}

}
