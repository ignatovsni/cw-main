package com.cwsni.world.model.engine;

import com.cwsni.world.model.engine.relationships.RTribute;
import com.cwsni.world.util.ComparisonTool;

public class ProvincePassabilityCriteria {

	private Country country;
	private Game game;

	protected ProvincePassabilityCriteria() {
	}

	public ProvincePassabilityCriteria(Country country) {
		this.country = country;
		this.game = country.getGame();
	}

	public boolean isPassable(Province p) {
		// TODO probably it is worth it to cache data for country per turn
		if (p.getTerrainType().isMountain()) {
			return false;
		}
		if (country.equals(p.getCountry())) {
			return true;
		}

		// do not let to go too far too earlier
		Province capital = country.getCapital();
		if (capital == null) {
			// it is a bad country :)
			return false;
		}
		double maxDistance = game.getScienceModificators().getMaxDistance(country);
		double distance = game.getMap().findDistanceApproximateCountOfProvinces(capital, p);
		if (distance > maxDistance) {
			return false;
		}

		if (p.getCountry() == null) {
			return true;
		}
		if (p.getTerrainType().isWater()) {
			return country.getReachableWaterProvinces().contains(p);
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
