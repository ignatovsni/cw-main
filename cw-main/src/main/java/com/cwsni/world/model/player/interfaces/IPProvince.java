package com.cwsni.world.model.player.interfaces;

import java.util.Collection;

import com.cwsni.world.model.data.TerrainType;

public interface IPProvince {

	int getId();

	String getName();

	int getContinentId();

	Collection<IPProvince> getNeighbors();

	Integer getCountryId();

	TerrainType getTerrainType();

	int getPopulationAmount();
	
	int getAvailablePeopleForRecruiting();

	double getSoilFertility();

	boolean canBeSubjugatedByMe();

	IPState getState();

	double getSoilFertilityWithPopFromArmy(IPArmy army);

	public boolean isPassable(IPArmy a);

	double getLoyaltyToState();

	double getLoyaltyToCountry();

}