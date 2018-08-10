package com.cwsni.world.model.player.interfaces;

import java.util.List;

import com.cwsni.world.model.data.TerrainType;

public interface IPProvince {

	int getId();

	List<IPProvince> getNeighbors();

	Integer getCountryId();

	TerrainType getTerrainType();

	int getPopulationAmount();

}