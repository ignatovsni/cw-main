package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.model.player.interfaces.IPState;

public class PProvince implements IPProvince {

	private Province province;
	private PGame game;
	private List<IPProvince> neighbors;
	private int populationAmount;

	PProvince(PGame game, Province province) {
		this.game = game;
		this.province = province;
		this.populationAmount = province.getPopulationAmount();
	}

	@Override
	public int getId() {
		return province.getId();
	}

	@Override
	public String getName() {
		return province.getName();
	}

	@Override
	public Collection<IPProvince> getNeighbors() {
		if (neighbors == null) {
			neighbors = new ArrayList<>(province.getNeighbors().size());
			province.getNeighbors().forEach(n -> neighbors.add(game.getProvince(n)));
		}
		return neighbors;
	}

	@Override
	public Integer getCountryId() {
		return province.getCountryId();
	}

	@Override
	public TerrainType getTerrainType() {
		return province.getTerrainType();
	}

	@Override
	public int getContinentId() {
		return province.getContinentId();
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PProvince)) {
			return false;
		}
		return ((IPProvince) obj).getId() == getId();
	}

	@Override
	public int getPopulationAmount() {
		return populationAmount;
	}

	@Override
	public String toString() {
		return "PProvince: " + getId();
	}

	@Override
	public boolean isMyProvince() {
		return ComparisonTool.isEqual(getCountryId(), game.getCountryId());
	}

	@Override
	public IPState getState() {
		if (province.getStateId() != null) {
			return game.findStateById(province.getStateId());
		} else {
			return null;
		}
	}

	@Override
	public double getSoilFertility() {
		return province.getSoilFertility();
	}

	@Override
	public double getSoilFertilityWithPopFromArmy(IPArmy army) {
		return province.getSoilFertilityBasePlusAgriculture(((PArmy) army).getAgriculture());
	}

	@Override
	public boolean isPassable(IPArmy a) {
		if (a != null) {
			return province.isPassable(a.getCountry().getId());
		} else {
			return province.isPassable(game.getCountryId());
		}
	}

	// --------------------- client model changes ---------------------
	public void cmcAddPopulation(int delta) {
		populationAmount += delta;
	}

}
