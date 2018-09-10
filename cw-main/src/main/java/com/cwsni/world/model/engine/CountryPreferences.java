package com.cwsni.world.model.engine;

import com.cwsni.world.model.data.DataCountryPreferences;

public class CountryPreferences {
	
	private DataCountryPreferences data;

	public void buildFrom(Country country, DataCountryPreferences preferences) {
		this.data = preferences;
	}
	
	public double getAggressiveness() {
		return data.getAggressiveness();
	}
	
	// ---------------------- static section --------------------
	
	public static DataCountryPreferences createPreferencesForNewCountry(Game game) {
		DataCountryPreferences preferences = new DataCountryPreferences();
		preferences.setAggressiveness(game.getRandom().nextDouble(0.01, 0.5));
		return preferences;
	}

}
