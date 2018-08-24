package com.cwsni.world.model;

import com.cwsni.world.model.data.DataCountry;

public class CountryFocus {

	private DataCountry dc;

	public CountryFocus(DataCountry dc) {
		this.dc = dc;
	}

	public double getValue() {
		return dc.getFocus();
	}

}
