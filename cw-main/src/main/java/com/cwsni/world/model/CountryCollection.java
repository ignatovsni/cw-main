package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.model.data.DataCountry;

public class CountryCollection {

	private List<Country> countries;
	private Map<Integer, Country> countriesById;

	public void buildFrom(Game game, List<DataCountry> dataCountries) {
		countries = new ArrayList<>(dataCountries.size());
		countriesById = new HashMap<>(dataCountries.size());
		dataCountries.forEach(dc -> {
			Country c = new Country();
			countries.add(c);
			countriesById.put(dc.getId(), c);
			c.buildFrom(game, dc);
		});
	}

	public List<Country> getCountries() {
		return Collections.unmodifiableList(countries);
	}

	public void addCountry(Country c) {
		countries.add(c);
		countriesById.put(c.getId(), c);
	}

	public void removeCountry(Country c) {
		countries.remove(c);
		countriesById.remove(c.getId());
	}

	public Country findCountryById(Integer countryId) {
		return countriesById.get(countryId);
	}

}
