package com.cwsni.world.model.engine.relationships;

import java.util.HashSet;
import java.util.Map;

import com.cwsni.world.model.data.relationships.DataRTribute;
import com.cwsni.world.model.data.relationships.DataRTruce;
import com.cwsni.world.model.data.relationships.DataRWar;
import com.cwsni.world.model.data.relationships.DataRelationshipsCollection;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.util.ComparisonTool;

public class RelationshipsCollection {

	private Game game;
	private DataRelationshipsCollection data;

	private AgreementCollection<DataRWar, RWar> wars;
	private AgreementCollection<DataRTruce, RTruce> truces;
	private AgreementCollection<DataRTribute, RTribute> tributes;

	public void buildFrom(Game game, DataRelationshipsCollection relationships) {
		this.game = game;
		this.data = relationships;

		wars = new AgreementCollection<DataRWar, RWar>(DataRWar::new, RWar::new);
		wars.buildFrom(game, data.getWars());

		truces = new AgreementCollection<DataRTruce, RTruce>(DataRTruce::new, RTruce::new);
		truces.buildFrom(game, data.getTruces());

		tributes = new AgreementCollection<DataRTribute, RTribute>(DataRTribute::new, RTribute::new);
		tributes.buildFrom(game, data.getTributes());
	}

	public void processNewTurn() {
		wars.agreements.forEach(war -> war.resetPeaceOffer());
		new HashSet<>(truces.agreements).stream().filter(truce -> truce.getEndTurn() < game.getTurn().getDateTurn())
				.forEach(truce -> truces.removeAgreement(truce));
	}

	public void unregisterCountry(Country c) {
		wars.unregisterCountry(c);
		truces.unregisterCountry(c);
		tributes.unregisterCountry(c);
	}

	public boolean hasAgreement(Integer cId1, Integer cId2) {
		return wars.hasAgreement(cId1, cId2) || truces.hasAgreement(cId1, cId2) || tributes.hasAgreement(cId1, cId2);
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
		cancelTribute(countryId, targetCountryId);
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
		if (war.checkAnyPeace()) {
			wars.removeAgreement(war);
			if (war.checkAttackerIsMasterInTribute()) {
				RTribute tribute = tributes.newAgreement(war.getMasterId(), war.getSlaveId());
				tribute.setTax(0.1);
			} else if (war.checkDefenderIsMasterInTribute()) {
				RTribute tribute = tributes.newAgreement(war.getSlaveId(), war.getMasterId());
				tribute.setTax(0.15);
			}
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

	public Map<Integer, RTribute> getCountriesWithTribute(Integer countryId) {
		return tributes.getCountriesWithAgreement(countryId);
	}

	public void cancelTribute(int countryId, int targetCountryId) {
		RTribute tribute = getCountriesWithTribute(countryId).get(targetCountryId);
		if (tribute != null) {
			tributes.removeAgreement(tribute);
		}
	}

}
