package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.model.Province;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.player.interfaces.IPProvince;

public class PProvince implements IPProvince {

	private Province province;
	private PGame game;
	private List<IPProvince> neighbors;

	PProvince(PGame game, Province province) {
		this.game = game;
		this.province = province;
	}

	@Override
	public int getId() {
		return province.getId();
	}

	@Override
	public List<IPProvince> getNeighbors() {
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
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PProvince)) {
			return false;
		}
		return ((IPProvince) obj).getId() == getId();
	}

	@Override
	public int getPopulationAmount() {
		return province.getPopulationAmount();
	}

	@Override
	public String toString() {
		return "PProvince: " + getId();
	}

}
