package com.cwsni.world.model.engine;

import com.cwsni.world.model.data.util.DataNormalizer;

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

	public double getSoilAreaBasePlusAgriculture(Province province) {
		return getSoilAreaBasePlusAgriculture(province, province.getScienceAgriculture());
	}
	
	private double getSoilAreaBasePlusAgriculture(Province province, double agriculture) {
		return province.getSoilNaturalArea()
				* (1 + agriculture * game.getGameParams().getScienceAgricultureMultiplicatorForArea());
	}

	public double getEffectiveDistanceFromProvinceToCountryCapital(Country country, Province province,
			double distToCapital) {
		double adminScience = 10 + province.getScienceAdministration()
				+ country.getCapital().getScienceAdministration();
		double distanceToCapitalWithScience = distToCapital - Math.log10(adminScience) * 0.3;
		return distanceToCapitalWithScience;
	}

	public double getEffectiveProvinceInfluenceFromCapitalWithDistanceDecrease(Country country) {
		double base = game.getGameParams().getProvinceInfluenceFromCapitalWithDistanceDecrease();
		Province capital = country.getCapital();
		if (capital == null) {
			return base;
		}
		double scienceLevel = DataNormalizer.minMax(Math.log10(capital.getScienceAdministration() + 10), 1, 10);
		double realValue = DataNormalizer.minMax(base + (1 - base) * scienceLevel / 10, base, 0.999);
		return realValue;
	}

	public double getEffectiveDistanceFromStateCapitalToCountryCapital(Country country, Province province,
			double distToCapital) {
		double adminScience = 10 + province.getScienceAdministration()
				+ country.getCapital().getScienceAdministration();
		double effectiveDistanceFromStateCapitalToCountryCapital = distToCapital / Math.log10(adminScience) * 2;
		return effectiveDistanceFromStateCapitalToCountryCapital;
	}

	public double getOwnFractionForCulture(Province capital, Province p) {
		double perYear = Math.log10(capital.getScienceAdministration() + 1) / 10000;
		double ownFraction = 0.9999 - game.getTurn().addPerYear(Math.min(0.001, perYear)) * p.getGovernmentInfluence();
		return ownFraction;
	}

	public double getDiseaseResistanceLevel(Province p) {
		return DataNormalizer.minMax(Math.log10(p.getScienceMedicine() + 10), 1, 10);
	}

	public double getPeopleGrowthFromMedicine(Province p) {
		return DataNormalizer.minMax(getDiseaseResistanceLevel(p) / 600, 0,
				game.getGameParams().getPopulationBaseGrowthPerYear());
	}

	

}
