package com.cwsni.world.game.commands;

public class CommandDiplomacyCancelTribute extends CommandDiplomacy {

	public CommandDiplomacyCancelTribute(int targetCountryId) {
		super(targetCountryId);
	}

	@Override
	public void apply() {
		if (!checkTargetCountry()) {
			return;
		}
		getGame().getRelationships().cancelTribute(getCountryId(), targetCountryId);
	}

}
