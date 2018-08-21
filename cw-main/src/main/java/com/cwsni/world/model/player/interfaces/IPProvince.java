package com.cwsni.world.model.player.interfaces;

import java.util.Collection;

import com.cwsni.world.model.data.TerrainType;

public interface IPProvince {

	int getId();

	Collection<IPProvince> getNeighbors();

	Integer getCountryId();

	TerrainType getTerrainType();

	int getPopulationAmount();

	boolean isMyProvince();
	
	IPState getState();

}