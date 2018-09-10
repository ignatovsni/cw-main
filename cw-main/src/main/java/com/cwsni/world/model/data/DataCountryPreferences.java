package com.cwsni.world.model.data;

import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataCountryPreferences {

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 3)
	private double aggressiveness;

	public double getAggressiveness() {
		return aggressiveness;
	}

	public void setAggressiveness(double aggressiveness) {
		this.aggressiveness = aggressiveness;
	}

}
