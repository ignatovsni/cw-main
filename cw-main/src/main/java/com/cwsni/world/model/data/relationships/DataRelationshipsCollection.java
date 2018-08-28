package com.cwsni.world.model.data.relationships;

import java.util.ArrayList;
import java.util.List;

public class DataRelationshipsCollection {

	private List<DataRWar> wars = new ArrayList<>();

	public List<DataRWar> getWars() {
		return wars;
	}

	public void setWars(List<DataRWar> wars) {
		this.wars = wars;
	}

}
