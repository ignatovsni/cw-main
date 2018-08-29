package com.cwsni.world.game.commands;

import java.util.Map;

import com.cwsni.world.model.engine.relationships.RWar;
import com.cwsni.world.model.engine.relationships.RelationshipsCollection;

public class CommandDiplomacyWar extends CommandDiplomacy {

	public CommandDiplomacyWar(int targetCountryId) {
		super(targetCountryId);
	}

	@Override
	public void apply() {
		if (!checkTargetCountry()) {
			return;
		}
		RelationshipsCollection relationships = getGame().getRelationships();
		Map<Integer, RWar> wars = relationships.getCountriesWithWar(getCountryId());
		if (wars.containsKey(targetCountryId)) {
			System.out.println("war is already active: " + this);
			return;
		}
		relationships.newWar(getCountryId(), targetCountryId);
	}

}
