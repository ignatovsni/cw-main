package com.cwsni.world.model.engine.relationships;

import java.util.HashSet;
import java.util.Map;

import com.cwsni.world.model.data.relationships.DataRTruce;
import com.cwsni.world.model.data.relationships.DataRVassal;
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
	private AgreementCollection<DataRVassal, RVassal> vassals;

	public void buildFrom(Game game, DataRelationshipsCollection relationships) {
		this.game = game;
		this.data = relationships;

		wars = new AgreementCollection<DataRWar, RWar>(DataRWar::new, RWar::new);
		wars.buildFrom(game, data.getWars());

		truces = new AgreementCollection<DataRTruce, RTruce>(DataRTruce::new, RTruce::new);
		truces.buildFrom(game, data.getTruces());

		vassals = new AgreementCollection<DataRVassal, RVassal>(DataRVassal::new, RVassal::new);
		vassals.buildFrom(game, data.getVassals());
	}

	public void processNewTurn() {
		wars.agreements.forEach(war -> war.resetPeaceOffer());
		new HashSet<>(truces.agreements).stream().filter(truce -> truce.getEndTurn() < game.getTurn().getTurn())
				.forEach(truce -> truces.removeAgreement(truce));
	}

	public void unregisterCountry(Country c) {
		wars.unregisterCountry(c);
		truces.unregisterCountry(c);
		vassals.unregisterCountry(c);
	}

	// -------------------- wars ---------------------------------------

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

	public void offerPeace(int countryId, int targetCountryId, boolean isRegularPeace, boolean isWantToBeMaster,
			boolean isWantToBeVassal) {
		Map<Integer, RWar> countries = wars.agreementsWithCountries.get(countryId);
		if (countries == null) {
			return;
		}
		RWar war = countries.get(targetCountryId);
		if (war == null) {
			return;
		}
		if (ComparisonTool.isEqual(countryId, war.getMasterId())) {
			war.attackerOfferPeace(isRegularPeace, isWantToBeMaster, isWantToBeVassal);
		} else {
			war.defenderOfferPeace(isRegularPeace, isWantToBeMaster, isWantToBeVassal);
		}
		if (war.checkAttackerIsMasterInVassal()) {
			wars.removeAgreement(war);
			vassals.newAgreement(countryId, targetCountryId);
			System.out.println("attacker is master " + countryId);
		} else if (war.checkDefenderIsMasterInVassal()) {
			wars.removeAgreement(war);
			vassals.newAgreement(targetCountryId, countryId);
			System.out.println("defender is master " + targetCountryId);
		} else if (war.checkRegularTruce()) {
			wars.removeAgreement(war);
			RTruce truce = truces.newAgreement(countryId, targetCountryId);
			truce.setEndTurn(
					game.getTurn().calculateFutureTurnAfterYears(game.getGameParams().getTruceDurationInYears()));
		}

	}

	// -------------------- truces ---------------------------------------

	public Map<Integer, RTruce> getCountriesWithTruce(Integer countryId) {
		return truces.getCountriesWithAgreement(countryId);
	}

	// -------------------- vassals ---------------------------------------

	public Map<Integer, RVassal> getCountriesWithVassal(Integer countryId) {
		return vassals.getCountriesWithAgreement(countryId);
	}

}
