package com.cwsni.world.model.player.interfaces;

import java.util.Collection;

public interface IPState {

	Collection<IPProvince> getProvinces();

	IPProvince getCapital();

	int getId();

	long getPopulationAmount();

	double getLoayltyToCountry(int countryId);

	double getLoayltyToState();

}
