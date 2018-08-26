package com.cwsni.world.model;

public class ProvincePassabilityCriteria {

	private Country country;

	public ProvincePassabilityCriteria(Country country) {
		this.country = country;
	}

	public boolean isPassable(Province p) {
		if (!p.getTerrainType().isWater() || country == null) {
			return true;
		}
		return country.getPassability() > p.getPassability();
	}

}
