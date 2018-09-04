package com.cwsni.world.model.engine;

public class ScienceModificators {

	private Game game;

	public ScienceModificators(Game game) {
		this.game = game;
	}

	public double getMaxDistance(Country country) {
		double science = getScienceAdministrator(country);
		double maxDistance = science / 1000 * country.getFocus().getValue() + 1;
		return maxDistance;
	}

	public int getMaxWaterDistance(Country country) {
		double science = getScienceAdministrator(country);
		int newWaterMaxDistance;
		int threshold = 20000;
		if (science > threshold) {
			newWaterMaxDistance = (int) (4 + science / threshold);
		} else {
			newWaterMaxDistance = (int) Math.log10(science);
		}
		return newWaterMaxDistance;

	}

	private double getScienceAdministrator(Country country) {
		double science = 0;
		if (country.getCapital() != null) {
			science = country.getCapital().getScienceAdministration();
		} else if (!country.getProvinces().isEmpty()) {
			science = country.getProvinces().iterator().next().getScienceAdministration();
		}
		science = Math.max(1, science);
		return science;
	}

	public boolean isNewCountryPossible(Province province) {
		return province.getScienceAdministration() > game.getGameParams().getNewCountryScienceAdministrationMin();
	}

	public double getSoilFertilityBasePlusAgriculture(Province province) {
		return getSoilFertilityBasePlusAgriculture(province, province.getScienceAgriculture());
	}

	public double getSoilFertilityBasePlusAgriculture(Province province, double agriculture) {
		return province.getSoilNaturalFertility()
				* (1 + agriculture * game.getGameParams().getScienceAgricultureMultiplicatorForFertility());
	}
	
	public double getEffectiveDistanceFromProvinceToCountryCapital(Country country, Province province, double distToCapital) {
		double adminScience = 10 + province.getScienceAdministration() + country.getCapital().getScienceAdministration();
		double distanceToCapitalWithScience = distToCapital - Math.log10(adminScience) * 0.3;
		return distanceToCapitalWithScience;
	}
	
	public double getEffectiveDistanceFromStateCapitalToCountryCapital(Country country, Province province, double distToCapital) {
		double adminScience = 10 + province.getScienceAdministration() + country.getCapital().getScienceAdministration();
		double effectiveDistanceFromStateCapitalToCountryCapital = distToCapital / Math.log10(adminScience) * 2;
		return effectiveDistanceFromStateCapitalToCountryCapital;
	}

}
