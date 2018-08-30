package com.cwsni.world.model.data.relationships;

import java.util.HashSet;
import java.util.Set;

public class DataRelationshipsCollection {

	private Set<DataRWar> wars = new HashSet<>();
	private Set<DataRTruce> truces = new HashSet<>();
	private Set<DataRVassal> vassals = new HashSet<>();

	public Set<DataRWar> getWars() {
		return wars;
	}

	public void setWars(Set<DataRWar> wars) {
		this.wars = wars;
	}

	public Set<DataRTruce> getTruces() {
		return truces;
	}

	public void setTruces(Set<DataRTruce> truces) {
		this.truces = truces;
	}

	public Set<DataRVassal> getVassals() {
		return vassals;
	}

	public void setVassals(Set<DataRVassal> vassals) {
		this.vassals = vassals;
	}

}
