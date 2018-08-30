package com.cwsni.world.game.commands;

public class CommandDiplomacyPeace extends CommandDiplomacy {

	private boolean isRegularPeace;
	private boolean isWantToBeMaster;
	private boolean isWantToBeVassal;

	public CommandDiplomacyPeace(int targetCountryId, boolean isRegularPeace, boolean isWantToBeMaster,
			boolean isWantToBeVassal) {
		super(targetCountryId);
		this.isRegularPeace = isRegularPeace;
		this.isWantToBeMaster = isWantToBeMaster;
		this.isWantToBeVassal = isWantToBeVassal;
	}

	@Override
	public void apply() {
		if (!checkTargetCountry()) {
			return;
		}
		getGame().getRelationships().offerPeace(getCountryId(), targetCountryId, isRegularPeace, isWantToBeMaster,
				isWantToBeVassal);
	}

}
