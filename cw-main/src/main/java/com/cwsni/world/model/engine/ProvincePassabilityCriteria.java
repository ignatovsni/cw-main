package com.cwsni.world.model.engine;

public class ProvincePassabilityCriteria {

	private Country country;

	public ProvincePassabilityCriteria(Country country) {
		this.country = country;
	}

	public boolean isPassable(Province p) {
		if (p.getTerrainType().isWater()) {
			return country.getReachableWaterProvinces().contains(p);
		}
		/*
		if (p.getCountry() == null || country.equals(p.getCountry())) {
			return true;
		}
		*/
		return true;
	}

}
