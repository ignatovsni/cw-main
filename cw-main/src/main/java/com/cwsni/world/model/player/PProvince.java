package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.common.algorithms.Node;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.data.TerrainType;

public class PProvince implements Node<PProvince> {

	private Province province;
	private PGame game;
	private List<PProvince> neighbors;

	PProvince(PGame game, Province province) {
		this.game = game;
		this.province = province;
	}

	public int getId() {
		return province.getId();
	}

	public List<PProvince> getNeighbors() {
		if (neighbors == null) {
			neighbors = new ArrayList<>(province.getNeighbors().size());
			province.getNeighbors().forEach(n -> neighbors.add(game.getProvince(n)));
		}
		return neighbors;
	}

	public Integer getCountryId() {
		return province.getCountryId();
	}

	public TerrainType getTerrainType() {
		return province.getTerrainType();
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		return ((PProvince) obj).getId() == getId();
	}

	public int getPopulationAmount() {
		return province.getPopulationAmount();
	}

}
