package com.cwsni.world.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.cwsni.world.model.data.HistoryData;
import com.cwsni.world.model.data.HistoryDataCountry;

public class History {

	private static final int MAX_AGE_OF_COUNTRY_RECORD = 200;

	private HistoryData data;
	private Game game;
	private Map<Integer, HistoryDataCountry> countries;
	private int internalTurn;

	public void buildFrom(Game game, HistoryData history) {
		this.game = game;
		this.data = history;
		this.countries = new HashMap<>();
		data.getCountries().forEach(dc -> countries.put(dc.getId(), dc));
		this.internalTurn = 0;
	}

	void removeCountry(Country c) {
		HistoryDataCountry hdc = new HistoryDataCountry(c.getCountryData(), game.getTurn().getTurn());
		data.getCountries().add(hdc);
		countries.put(c.getId(), hdc);
	}

	public HistoryDataCountry findCountry(int countryId) {
		return countries.get(countryId);
	}

	public void processNewTurn() {
		if (++internalTurn % 10 != 0) {
			return;
		}
		int turnToDelete = game.getTurn().getTurn() - MAX_AGE_OF_COUNTRY_RECORD;
		Iterator<Entry<Integer, HistoryDataCountry>> iter = countries.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, HistoryDataCountry> entry = iter.next();
			HistoryDataCountry hdc = entry.getValue();
			if (hdc.getTurnOfRecord() < turnToDelete) {
				iter.remove();
				data.getCountries().remove(hdc);
			}
		}

	}

}
