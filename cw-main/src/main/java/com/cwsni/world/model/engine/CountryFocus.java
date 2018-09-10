package com.cwsni.world.model.engine;

import com.cwsni.world.model.data.DataCountryFocus;
import com.cwsni.world.model.engine.modifiers.CountryModifier;

public class CountryFocus {

	private Country country;
	private DataCountryFocus data;

	public void buildFrom(Country country, DataCountryFocus focus) {
		this.country = country;
		this.data = focus;
	}

	public double getValue() {
		return country.getModifiers().getModifiedValue(CountryModifier.FOCUS, data.getFocus());
	}

	// -------------------------- static section ----------------------

	public static DataCountryFocus createFocusForNewCountry(Game game) {
		DataCountryFocus dcf = new DataCountryFocus();
		dcf.setFocus(1);
		return dcf;
	}

}
