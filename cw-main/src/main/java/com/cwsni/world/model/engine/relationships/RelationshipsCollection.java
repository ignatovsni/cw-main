package com.cwsni.world.model.engine.relationships;

import java.util.HashSet;
import java.util.Map;

import com.cwsni.world.model.data.relationships.DataRTruce;
import com.cwsni.world.model.data.relationships.DataRWar;
import com.cwsni.world.model.data.relationships.DataRelationshipsCollection;
import com.cwsni.world.model.engine.ComparisonTool;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;

public class RelationshipsCollection {

	private Game game;
	private DataRelationshipsCollection data;

	private AgreementCollection<DataRWar, RWar> wars;
	private AgreementCollection<DataRTruce, RTruce> truces;

	public void buildFrom(Game game, DataRelationshipsCollection relationships) {
		this.game = game;
		this.data = relationships;

		wars = new AgreementCollection<DataRWar, RWar>(DataRWar::new, RWar::new);
		wars.buildFrom(game, data.getWars());

		truces = new AgreementCollection<DataRTruce, RTruce>(DataRTruce::new, RTruce::new);
		truces.buildFrom(game, data.getTruces());
	}

	public void processNewTurn() {
		wars.agreements.forEach(war -> war.resetPeaceOffer());
		new HashSet<>(truces.agreements).stream().filter(truce -> truce.getEndTurn() < game.getTurn().getTurn())
				.forEach(truce -> truces.removeAgreement(truce));
	}

	public Map<Integer, RWar> getCountriesWithWar(Integer countryId) {
		return wars.getCountriesWithAgreement(countryId);
	}

	public void newWar(Integer countryId, int targetCountryId) {
		if (truces.getCountriesWithAgreement(countryId).containsKey(targetCountryId)) {
			return;
		}
		if (truces.getCountriesWithAgreement(targetCountryId).containsKey(countryId)) {
			return;
		}
		wars.newAgreement(countryId, targetCountryId);
	}

	public void unregisterCountry(Country c) {
		wars.unregisterCountry(c);
		truces.unregisterCountry(c);
	}

	public void offerPeace(int countryId, int targetCountryId) {
		Map<Integer, RWar> countries = wars.agreementsWithCountries.get(countryId);
		if (countries == null) {
			return;
		}
		RWar war = countries.get(targetCountryId);
		if (war == null) {
			return;
		}
		if (ComparisonTool.isEqual(countryId, war.getMasterId())) {
			war.attackerOfferPeace();
		} else {
			war.defenderOfferPeace();
		}
		if (war.checkPeace()) {
			wars.removeAgreement(war);
			RTruce truce = truces.newAgreement(countryId, targetCountryId);
			truce.setEndTurn(
					game.getTurn().calculateFutureTurnAfterYears(game.getGameParams().getTruceDurationInYears()));
		}
	}

	public Map<Integer, RTruce> getCountriesWithTruce(Integer countryId) {
		return truces.getCountriesWithAgreement(countryId);
	}

}
