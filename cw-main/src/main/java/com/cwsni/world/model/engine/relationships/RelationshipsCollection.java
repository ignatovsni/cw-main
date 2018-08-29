package com.cwsni.world.model.engine.relationships;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cwsni.world.model.data.relationships.DataRWar;
import com.cwsni.world.model.data.relationships.DataRelationshipsCollection;
import com.cwsni.world.model.engine.ComparisonTool;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;

public class RelationshipsCollection {

	private Game game;
	private DataRelationshipsCollection data;
	private Set<RWar> wars;
	private Map<Integer, Map<Integer, RWar>> warsWithCountries;

	public void buildFrom(Game game, DataRelationshipsCollection relationships) {
		this.game = game;
		this.data = relationships;
		wars = new HashSet<>();
		warsWithCountries = new HashMap<>();
		data.getWars().forEach(dw -> registerWar(game, dw));
	}

	private void registerWar(Game game, DataRWar dw) {
		RWar war = new RWar();
		war.buildFrom(game, dw);
		addWarsWithCountries(dw.getAttackerId(), dw.getDefenderId(), war);
		addWarsWithCountries(dw.getDefenderId(), dw.getAttackerId(), war);
		wars.add(war);
	}

	private void addWarsWithCountries(int cId1, int cId2, RWar war) {
		Map<Integer, RWar> countries = warsWithCountries.get(cId1);
		if (countries == null) {
			countries = new HashMap<>();
			warsWithCountries.put(cId1, countries);
		}
		countries.put(cId2, war);
	}

	public Map<Integer, RWar> getCountriesWithWar(Integer countryId) {
		Map<Integer, RWar> countries = warsWithCountries.get(countryId);
		if (countries != null) {
			return Collections.unmodifiableMap(countries);
		} else {
			return Collections.emptyMap();
		}
	}

	public void newWar(Integer countryId, int targetCountryId) {
		Map<Integer, RWar> wars = getCountriesWithWar(countryId);
		if (wars.containsKey(targetCountryId)) {
			return;
		}
		wars = getCountriesWithWar(targetCountryId);
		if (wars.containsKey(countryId)) {
			return;
		}
		DataRWar dw = new DataRWar();
		dw.setAttackerId(countryId);
		dw.setDefenderId(targetCountryId);
		dw.setStartTurn(game.getTurn().getTurn());
		data.getWars().add(dw);
		registerWar(game, dw);
	}

	public void unregisterCountry(Country c) {
		Map<Integer, RWar> countryWars = getCountriesWithWar(c.getId());
		if (countryWars == null) {
			return;
		}
		countryWars = new HashMap<>(countryWars);
		countryWars.values().forEach(war -> removeWar(war));
		warsWithCountries.remove(c.getId());
	}

	private void removeWar(RWar war) {
		Map<Integer, RWar> countries = warsWithCountries.get(war.getAttackerId());
		if (countries != null) {
			countries.remove(war.getDefenderId());
			if (countries.isEmpty()) {
				warsWithCountries.remove(war.getAttackerId());
			}
		}
		countries = warsWithCountries.get(war.getDefenderId());
		if (countries != null) {
			countries.remove(war.getAttackerId());
			if (countries.isEmpty()) {
				warsWithCountries.remove(war.getDefenderId());
			}
		}
		wars.remove(war);
		data.getWars().remove(war.getData());
	}

	public void offerPeace(int countryId, int targetCountryId) {
		Map<Integer, RWar> countries = warsWithCountries.get(countryId);
		if (countries == null) {
			return;
		}
		RWar war = countries.get(targetCountryId);
		if (war == null) {
			return;
		}
		if (ComparisonTool.isEqual(countryId, war.getAttackerId())) {
			war.attackerOfferPeace();
		} else {
			war.defenderOfferPeace();
		}
		if (war.checkPeace()) {
			removeWar(war);
		}
	}

	public void processNewTurn() {
		wars.forEach(war -> war.resetPeaceOffer());
	}

}
