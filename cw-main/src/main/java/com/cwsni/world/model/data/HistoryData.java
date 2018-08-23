package com.cwsni.world.model.data;

import java.util.HashSet;
import java.util.Set;

public class HistoryData {

	private Set<HistoryDataCountry> countries = new HashSet<>();

	public Set<HistoryDataCountry> getCountries() {
		return countries;
	}

	public void setCountries(Set<HistoryDataCountry> countries) {
		this.countries = countries;
	}

}
