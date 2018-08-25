package com.cwsni.world.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.cwsni.world.model.data.HistoryData;
import com.cwsni.world.model.data.HistoryDataCountry;

public class History {

	private HistoryData data;
	private Game game;
	private Map<Integer, HistoryDataCountry> countries;
	private int lastCleaningTurn;

	public void buildFrom(Game game, HistoryData history) {
		this.game = game;
		this.data = history;
		this.countries = new HashMap<>();
		this.data.getCountries().forEach(dc -> countries.put(dc.getId(), dc));
		this.lastCleaningTurn = 0;
	}

	void removeCountry(Country c) {
		HistoryDataCountry hdc = findCountry(c.getId());
		if (hdc == null) {
			hdc = new HistoryDataCountry();
			hdc.update(c.getCountryData(), game.getTurn().getTurn());
			data.getCountries().add(hdc);
			countries.put(c.getId(), hdc);
		} else {
			hdc.update(c.getCountryData(), game.getTurn().getTurn());
		}
	}

	public HistoryDataCountry findCountry(int countryId) {
		return countries.get(countryId);
	}

	public boolean containsCountry(int countryId) {
		return countries.containsKey(countryId);
	}

	public void processNewTurn() {
		if (lastCleaningTurn > game.getTurn().getTurn() - 10) {
			return;
		}
		lastCleaningTurn = game.getTurn().getTurn();
		Iterator<Entry<Integer, HistoryDataCountry>> iter = countries.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, HistoryDataCountry> entry = iter.next();
			HistoryDataCountry hdc = entry.getValue();
			if (hdc.isCanBeRemoved(game.getTurn())) {
				iter.remove();
				data.getCountries().remove(hdc);
			}
		}

	}

}
