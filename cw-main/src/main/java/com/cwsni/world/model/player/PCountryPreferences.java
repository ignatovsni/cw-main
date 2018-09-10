package com.cwsni.world.model.player;

import com.cwsni.world.model.engine.CountryPreferences;
import com.cwsni.world.model.player.interfaces.IPCountryPreferences;

public class PCountryPreferences implements IPCountryPreferences {

	private CountryPreferences preferences;

	public PCountryPreferences(CountryPreferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public double getAggressiveness() {
		return preferences.getAggressiveness();
	}

}
