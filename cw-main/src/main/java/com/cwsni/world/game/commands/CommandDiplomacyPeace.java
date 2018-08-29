package com.cwsni.world.game.commands;

public class CommandDiplomacyPeace extends CommandDiplomacy {

	public CommandDiplomacyPeace(int targetCountryId) {
		super(targetCountryId);
	}

	@Override
	public void apply() {
		if (!checkTargetCountry()) {
			return;
		}
		getGame().getRelationships().offerPeace(getCountryId(), targetCountryId);
	}

}
