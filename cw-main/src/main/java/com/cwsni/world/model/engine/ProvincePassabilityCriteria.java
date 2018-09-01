package com.cwsni.world.model.engine;

import com.cwsni.world.model.engine.relationships.RTribute;

public class ProvincePassabilityCriteria {

	private Country country;
	private Game game;

	public ProvincePassabilityCriteria(Country country) {
		this.country = country;
		this.game = country.getGame();
	}

	public boolean isPassable(Province p) {
		if (p.getTerrainType().isMountain()) {
			return false;
		}
		if (p.getTerrainType().isWater()) {
			return country.getReachableWaterProvinces().contains(p);
		}
		if (p.getCountry() == null) {
			return true;
		}
		if (p.getCountry().equals(country)) {
			return true;
		}
		if (game.getRelationships().getCountriesWithWar(country.getId()).keySet().contains(p.getCountryId())) {
			return true;
		}
		RTribute tribute = game.getRelationships().getCountriesWithTribute(country.getId()).get(p.getCountryId());
		if (tribute != null && ComparisonTool.isEqual(tribute.getMasterId(), country.getId())) {
			return true;
		}
		return false;
	}

}
