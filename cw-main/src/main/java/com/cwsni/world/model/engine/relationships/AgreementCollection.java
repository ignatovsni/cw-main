package com.cwsni.world.model.engine.relationships;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.cwsni.world.model.data.relationships.DataRBaseAgreement;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;

public class AgreementCollection<DT extends DataRBaseAgreement, ET extends RBaseAgreement> {

	private Supplier<DT> dtSupplier;
	private Supplier<ET> etSupplier;

	protected Set<DT> dataModelSet;
	protected Set<ET> agreements;
	protected Map<Integer, Map<Integer, ET>> agreementsWithCountries;
	protected Game game;

	public AgreementCollection(Supplier<DT> dtSupplier, Supplier<ET> etSupplier) {
		this.dtSupplier = dtSupplier;
		this.etSupplier = etSupplier;
	}

	public void buildFrom(Game game, Set<DT> dataModelSet) {
		this.game = game;
		this.dataModelSet = dataModelSet;
		agreements = new HashSet<>();
		agreementsWithCountries = new HashMap<>();
		dataModelSet.forEach(dt -> registerAgreement(game, dt));
	}

	protected ET registerAgreement(Game game, DT dt) {
		ET agreement = etSupplier.get();
		agreement.buildFrom(game, dt);
		addAgreementWithCountries(dt.getMasterId(), dt.getSlaveId(), agreement);
		addAgreementWithCountries(dt.getSlaveId(), dt.getMasterId(), agreement);
		agreements.add(agreement);
		return agreement;
	}

	protected void addAgreementWithCountries(int cId1, int cId2, ET agreement) {
		Map<Integer, ET> countries = agreementsWithCountries.get(cId1);
		if (countries == null) {
			countries = new HashMap<>();
			agreementsWithCountries.put(cId1, countries);
		}
		countries.put(cId2, agreement);
	}

	public Map<Integer, ET> getCountriesWithAgreement(Integer countryId) {
		Map<Integer, ET> countries = agreementsWithCountries.get(countryId);
		if (countries != null) {
			return Collections.unmodifiableMap(countries);
		} else {
			return Collections.emptyMap();
		}
	}

	public ET newAgreement(Integer countryId, int targetCountryId) {
		Map<Integer, ET> agrs = getCountriesWithAgreement(countryId);
		ET et = agrs.get(targetCountryId);
		if (et != null) {
			return et;
		}
		agrs = getCountriesWithAgreement(targetCountryId);
		et = agrs.get(countryId);
		if (et != null) {
			return et;
		}
		DT dt = dtSupplier.get();
		dt.setMasterId(countryId);
		dt.setSlaveId(targetCountryId);
		dt.setStartTurn(game.getTurn().getDateTurn());
		dataModelSet.add(dt);
		return registerAgreement(game, dt);
	}

	public void unregisterCountry(Country c) {
		Map<Integer, ET> countryWars = getCountriesWithAgreement(c.getId());
		if (countryWars != null) {
			countryWars = new HashMap<>(countryWars);
			countryWars.values().forEach(agreement -> removeAgreement(agreement));
			agreementsWithCountries.remove(c.getId());
		}
	}

	protected void removeAgreement(ET agreement) {
		Map<Integer, ET> countries = agreementsWithCountries.get(agreement.getMasterId());
		if (countries != null) {
			countries.remove(agreement.getSlaveId());
			if (countries.isEmpty()) {
				agreementsWithCountries.remove(agreement.getMasterId());
			}
		}
		countries = agreementsWithCountries.get(agreement.getSlaveId());
		if (countries != null) {
			countries.remove(agreement.getMasterId());
			if (countries.isEmpty()) {
				agreementsWithCountries.remove(agreement.getSlaveId());
			}
		}
		agreements.remove(agreement);
		dataModelSet.remove(agreement.getData());
	}

	public boolean hasAgreement(Integer cId1, Integer cId2) {
		return getCountriesWithAgreement(cId1).containsKey(cId2);
	}

}
