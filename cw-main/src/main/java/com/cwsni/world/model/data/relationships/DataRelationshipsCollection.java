package com.cwsni.world.model.data.relationships;

import java.util.HashSet;
import java.util.Set;

public class DataRelationshipsCollection {

	private Set<DataRWar> wars = new HashSet<>();

	public Set<DataRWar> getWars() {
		return wars;
	}

	public void setWars(Set<DataRWar> wars) {
		this.wars = wars;
	}

}
