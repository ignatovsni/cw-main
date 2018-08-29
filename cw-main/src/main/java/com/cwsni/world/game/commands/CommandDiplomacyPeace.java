package com.cwsni.world.game.commands;

import java.util.Map;

import com.cwsni.world.model.engine.relationships.RWar;
import com.cwsni.world.model.engine.relationships.RelationshipsCollection;

public class CommandDiplomacyPeace extends CommandDiplomacy {

	public CommandDiplomacyPeace(int targetCountryId) {
		super(targetCountryId);
	}

	@Override
	public void apply() {
		if (!checkTargetCountry()) {
			return;
		}
		RelationshipsCollection relationships = getGame().getRelationships();
		Map<Integer, RWar> wars = relationships.getCountriesWithWar(getCountryId());
		if (!wars.containsKey(targetCountryId)) {
			System.out.println("war doesn't exist: " + this);
			return;
		}
		relationships.offerPeace(getCountryId(), targetCountryId);
	}



}
