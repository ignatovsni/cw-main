package com.cwsni.world.model.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.data.DataScience;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.Point;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.engine.modifiers.CountryModifier;
import com.cwsni.world.model.engine.modifiers.ModifierCollection;
import com.cwsni.world.model.engine.modifiers.ProvinceModifier;
import com.cwsni.world.util.ComparisonTool;
import com.cwsni.world.util.CwException;

public class Province {

	private DataProvince data;
	private List<Province> neighbors;
	private List<Population> population;
	private List<FoodResource> foodResources;
	private WorldMap map;
	private List<Population> immigrants;
	private Country country;
	private State state;
	private List<Army> armies;
	private Integer oldCapitalId;
	private double distanceToCapital;
	private Integer oldStateCapitalId;
	private double distanceToStateCapital;
	private Boolean hasWaterNeighbor;
	private ModifierCollection<ProvinceModifier> modifiers;

	@Override
	public int hashCode() {
		return data.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Province)) {
			return false;
		}
		return ((Province) obj).getId() == getId();
	}

	@Override
	public String toString() {
		return "province with id = " + getId() + ";";
	}

	public List<Province> getNeighbors() {
		return neighbors;
	}

	public Culture getCulture() {
		if (getPopulation().isEmpty()) {
			return null;
		} else {
			return getPopulation().get(0).getCulture();
		}
	}

	public Point getCenter() {
		return data.getCenter();
	}

	public int getId() {
		return data.getId();
	}

	public List<Population> getPopulation() {
		// unmodifiableList to prevent adding elements to list
		return Collections.unmodifiableList(population);
	}

	public List<Army> getArmies() {
		return Collections.unmodifiableList(armies);
	}

	public String getName() {
		return data.getName();
	}

	public Country getCountry() {
		return country;
	}

	public Integer getCountryId() {
		return country != null ? country.getId() : null;
	}

	public void setCountry(Country c) {
		this.country = c;
	}

	public void setState(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

	public Integer getStateId() {
		return state != null ? state.getId() : null;
	}

	public int getContinentId() {
		return data.getContinentId();
	}

	public List<FoodResource> getFoodResources() {
		return foodResources;
	}

	public double getSoilFertility() {
		// science
		double v = map.getGame().getScienceModificators().getSoilFertilityBasePlusAgriculture(this);
		// infrastructure
		if (getPopulationAmount() > 0) {
			v = v * 0.5 * (1 + getInfrastructurePercent());
		}
		return v;
	}

	public double getSoilFertilityBasePlusAgriculture(double agriculture) {
		return map.getGame().getScienceModificators().getSoilFertilityBasePlusAgriculture(this, agriculture);
	}

	public double getSoilNaturalFertility() {
		return modifiers.getModifiedValue(ProvinceModifier.SOIL_FERTILITY, foodResources.get(0).getQuality());
	}

	public double getSoilArea() {
		// science
		double v = map.getGame().getScienceModificators().getSoilAreaBasePlusAgriculture(this);
		// infrastructure
		if (getPopulationAmount() > 0) {
			v = v * 0.5 * (1 + getInfrastructurePercent());
		}
		return v;
	}

	public double getSoilNaturalArea() {
		return modifiers.getModifiedValue(ProvinceModifier.SOIL_AREA, foodResources.get(0).getAmount());
	}

	public double getScienceAgriculture() {
		return getScienceTypeValue(ds -> ds.getAgriculture());
	}

	public double getScienceMedicine() {
		return getScienceTypeValue(ds -> ds.getMedicine());
	}

	public double getScienceAdministration() {
		return getScienceTypeValue(ds -> ds.getAdministration());
	}

	private double getScienceTypeValue(Function<ScienceCollection, DataScience> getter4Science) {
		int populationAmount = getPopulationAmount();
		if (populationAmount == 0) {
			return 0;
		}
		double scienceValue = (double) (getPopulation().stream()
				.mapToDouble(pop -> pop.getAmount() * getter4Science.apply(pop.getScience()).getAmount()).sum()
				/ populationAmount);
		return scienceValue;
	}

	public int getInfrastructure() {
		return data.getInfrastructure();
	}

	/**
	 * Return percent value, how much population have needed infrastructure
	 */
	public double getInfrastructurePercent() {
		int populationAmount = getPopulationAmount();
		if (populationAmount == 0) {
			return 0;
		} else {
			return Math.min((double) data.getInfrastructure() / populationAmount,
					getMap().getGame().getGameParams().getInfrastructureMaxValue());
		}
	}

	public TerrainType getTerrainType() {
		return data.getTerrainType();
	}

	public double getSize() {
		return data.getSize();
	}

	public int getPopulationAmount() {
		return getPopulation().stream().mapToInt(p -> p.getAmount()).sum();
	}

	public int getAvailablePeopleForRecruiting() {
		return getPopulation().stream().mapToInt(p -> p.getAvailablePeopleForRecruiting()).sum();
	}

	public int getMaxPopulation() {
		double defaultMax = getSoilArea() * getSoilFertility();
		if (getCountry() != null && this.equals(getCountry().getCapital())) {
			// capital can support more population
			defaultMax *= getMap().getGame().getGameParams().getPopulationMaxInCapital();
		} else {
			if (getState() != null && this.equals(getState().getCapital())) {
				// capital can support more population
				defaultMax *= getMap().getGame().getGameParams().getPopulationMaxInStateCapital();
			}
		}
		return (int) defaultMax;
	}

	public double getPopulationExcess() {
		return (double) getPopulationAmount() / Math.max(getMaxPopulation(), 1);
	}

	public List<Population> getImmigrants() {
		return immigrants;
	}

	public WorldMap getMap() {
		return map;
	}

	public void buildFrom(WorldMap worldMap, DataProvince dp) {
		this.map = worldMap;
		this.data = dp;
		modifiers = new ModifierCollection<>();
		neighbors = new ArrayList<>();
		population = new ArrayList<>();
		immigrants = new ArrayList<>();
		foodResources = new ArrayList<>();
		data.getPopulation().forEach(dpop -> {
			Population pop = new Population(worldMap.getGame());
			pop.buildFrom(this, dpop);
			population.add(pop);
		});
		data.getNeighbors().forEach(id -> neighbors.add(map.findProvinceById(id)));
		data.getFoodResources().forEach(dr -> {
			FoodResource fr = new FoodResource();
			fr.buildFrom(this, dr);
			foodResources.add(fr);
		});

		armies = new ArrayList<>();
	}

	public ModifierCollection<ProvinceModifier> getModifiers() {
		return modifiers;
	}

	/**
	 * infrastructure without government influence
	 */
	private void processNaturalInfrastructure() {
		// natural growth can not excess infrastructureNaturalLimitFromPopulation
		int populationAmount = getPopulationAmount();
		int neededInfrastructure = (int) (populationAmount
				* map.getGame().getGameParams().getInfrastructureNaturalLimitFromPopulation());
		int currentInfrastructure = Math.max(getInfrastructure(), 1);
		if (neededInfrastructure < currentInfrastructure) {
			// degradation
			double decreasing = Math.max((double) neededInfrastructure / currentInfrastructure, 0.99);
			setInfrastructure((int) (decreasing * getInfrastructure()));
		} else {
			// improving
			if (populationAmount > 0) {
				double improving = Math.min((double) neededInfrastructure / currentInfrastructure, 1.01);
				int addInfrastructure = (int) (currentInfrastructure * improving - currentInfrastructure);
				addInfrastructure = Math.max(addInfrastructure, 100);
				setInfrastructure(currentInfrastructure + addInfrastructure);
			}
		}
	}

	private void setInfrastructure(int newInfrastructure) {
		data.setInfrastructure(Math.max(0, newInfrastructure));
	}

	public void processImmigrantsAndMergePops() {
		if (!getTerrainType().isPopulationPossible()) {
			if (!getImmigrants().isEmpty()) {
				throw new CwException("Immigrants can not be on " + getTerrainType());
			}
			return;
		}
		Population.processImmigrantsAndMergePops(this, map.getGame());
		data.getPopulation().clear();
		getPopulation().forEach(pop -> data.getPopulation().add(pop.getPopulationData()));
	}

	public void addPopulation(Population pop) {
		if (getTerrainType().isPopulationPossible()) {
			population.add(pop);
			pop.setProvince(this);
		}
	}

	public void removePopulation(Population pop) {
		population.remove(pop);
	}

	public void processNewTurn() {
		if (!getTerrainType().isPopulationPossible()) {
			return;
		}
		if (getPopulationAmount() != 0) {
			ScienceCollection.growScienceNewTurn(this, map.getGame());
			Population.migrateNewTurn(this, map.getGame());
			Population.growPopulationNewTurn(this, map.getGame());
			Population.processLoyaltyNewTurn(this, map.getGame());
			Culture.influenceOfCountryFor(this, map.getGame());
		}
		processLocalModifiersToArmies();
		processNaturalInfrastructure();
		removeDiedPops();
		processLifeInTheCountryNewTurn();
		if (getPopulationAmount() != 0) {
			processTaxesAndOthersInNewTurn();
		}
	}

	private void processLocalModifiersToArmies() {
		// Modifiers on armies. Probably it is better to place modifiers on armies.
		// But right now it is good enough.
		getArmies().forEach(a -> a.setSoldiers(
				(int) getModifiers().getModifiedValue(ProvinceModifier.POPULATION_AMOUNT, a.getSoldiers())));
	}

	private void processLifeInTheCountryNewTurn() {
		// TODO ? the same for province ?
		getPopulation().forEach(pop -> pop.processLifeInTheCountryNewTurn());
	}

	private void removeDiedPops() {
		Iterator<Population> iter = population.iterator();
		while (iter.hasNext()) {
			Population pop = iter.next();
			if (pop.getAmount() < 10) {
				iter.remove();
				data.getPopulation().remove(pop.getPopulationData());
			}
		}
	}

	public void addArmy(Army a) {
		armies.add(a);
	}

	public void removeArmy(Army a) {
		armies.remove(a);
	}

	public double getEffectiveWealth() {
		return getWealthOfPopulation() + getWealthOfProvince();
	}

	public double getWealthLevel() {
		int populationAmount = getPopulationAmount();
		if (populationAmount == 0) {
			return 0;
		}
		return getEffectiveWealth() / populationAmount / map.getGame().getGameParams().getBudgetMaxWealthPerPerson()
				/ 2;
	}

	public double getWealthOfPopulation() {
		return getPopulation().stream().mapToDouble(p -> p.getWealth()).sum();
	}

	private double getWealthOfProvince() {
		// if the province lost population, then wealth can be much more than current
		// pops, so we should ignore too much wealth
		return Math.min(data.getWealth(),
				getPopulationAmount() * map.getGame().getGameParams().getBudgetMaxWealthPerPerson());
	}

	public double getWealthLevelOfProvince() {
		return getWealthOfProvince() / getPopulationAmount()
				/ map.getGame().getGameParams().getBudgetMaxWealthPerPerson();
	}

	public double getRawWealthOfProvince() {
		return data.getWealth();
	}

	/**
	 * The government effectiveness. The difference with
	 * {@link #getCapitalInfluence()}: it can take in account others things, not
	 * only capital.
	 */
	public double getGovernmentInfluence() {
		return getCapitalInfluence();
	}

	/**
	 * Influence of the capital and only the capital.
	 */
	public double getCapitalInfluence() {
		Country country = getCountry();
		if (country == null) {
			return 0;
		}
		double influence;
		GameParams gParams = map.getGame().getGameParams();
		if (country.getCapitalId() == null) {
			influence = gParams.getProvinceInfluenceFromCapitalWithoutCapital();
		} else {
			double distToCapital = getDistanceToCapital();
			double maxDistance = country.getMaxReachableDistance();
			if (distToCapital <= maxDistance) {
				influence = Math.max((maxDistance - distToCapital) / maxDistance,
						gParams.getProvinceInfluenceFromCapitalWithoutCapital());
			} else {
				influence = gParams.getProvinceInfluenceFromCapitalWithoutCapital();
			}
		}
		influence = country.getModifiers().getModifiedValue(CountryModifier.PROVINCE_GOVERNMENT_INFLUENCE, influence);
		return Math.max(influence, gParams.getProvinceInfluenceFromCapitalWithoutCapital());
	}

	public double getDistanceToCapital() {
		if (getCountry() == null) {
			return -1;
		}
		Integer capitalId = getCountry().getCapitalId();
		if (capitalId == null) {
			return -1;
		}
		if (!ComparisonTool.isEqual(oldCapitalId, capitalId)) {
			if (ComparisonTool.isEqual(capitalId, getId())) {
				distanceToCapital = 0;
			} else {
				distanceToCapital = map.findDistanceApproximateCountOfProvinces(country.getCapital(), this);
			}
			oldCapitalId = capitalId;
		}
		return distanceToCapital;
	}

	public double getDistanceToStateCapital() {
		if (getState() == null) {
			return -1;
		}
		Integer capitalId = getState().getCapitalId();
		if (capitalId == null) {
			return -1;
		}
		if (!ComparisonTool.isEqual(oldStateCapitalId, capitalId)) {
			if (ComparisonTool.isEqual(capitalId, getId())) {
				distanceToStateCapital = 0;
			} else {
				distanceToStateCapital = map.findDistanceApproximateCountOfProvinces(getState().getCapital(), this);
			}
			oldStateCapitalId = capitalId;
		}
		return distanceToStateCapital;
	}

	public double getFederalIncomePerYear() {
		return getSumTaxForYear() * getCountry().getMoneyBudget().getProvinceTax();
	}

	private double getLocalIncomePerYear() {
		if (getCountry() == null) {
			return getSumTaxForYear();
		} else {
			return getSumTaxForYear() * (1 - getCountry().getMoneyBudget().getProvinceTax());
		}
	}

	private double getSumTaxForYear() {
		if (getCountry() == null) {
			return getRawSumTaxForYear() * map.getGame().getGameParams().getProvinceEffectivenessWithoutGoverment();
		} else {
			return getRawSumTaxForYear() * getGovernmentInfluence();
		}
	}

	private double getRawSumTaxForYear() {
		int populationAmount = getPopulationAmount();
		if (populationAmount <= 0) {
			return 0;
		}
		GameParams gParams = map.getGame().getGameParams();
		// absolutely poor people
		double income = populationAmount * gParams.getBudgetBaseTaxPerPerson();
		// wealth people
		income += getEffectiveWealth() / gParams.getBudgetMaxWealthPerPerson() / 2
				* (gParams.getBudgetBaseTaxPerWealthPerson() - gParams.getBudgetBaseTaxPerPerson());
		Country country = getCountry();
		if (country != null) {
			income = country.getModifiers().getModifiedValue(CountryModifier.PROVINCE_TAX_EFFECTIVENESS, income);
		}
		return income;
	}

	protected Turn getTurn() {
		return map.getGame().getTurn();
	}

	private void processTaxesAndOthersInNewTurn() {
		GameParams gParams = map.getGame().getGameParams();
		int populationAmount = getPopulationAmount();
		double income = getTurn().addPerYear(getLocalIncomePerYear());
		double wealthForProvince = income / 3;
		double wealthForPops = income / 3;
		double wealthForInfrastructure = income / 3;

		// province wealth
		double maxWealthForProv = populationAmount * gParams.getBudgetMaxWealthPerPerson();
		double addWealthForProv = Math.min(Math.max(maxWealthForProv - data.getWealth(), 0), wealthForProvince);
		addWealth(addWealthForProv);
		income -= addWealthForProv;

		// infrastructure
		if (getCountry() != null) {
			double newInfrastructure = Math.min(getInfrastructure() + wealthForInfrastructure,
					getPopulationAmount() * gParams.getInfrastructureNaturalLimitWithLocalGovernment());
			income -= Math.max(newInfrastructure - getInfrastructure(), 0);
			setInfrastructure((int) newInfrastructure);
		}

		// population wealth
		// we use income if it is not empty after others things
		wealthForPops = wealthForPops + Math.max(income, 0) / 2;
		for (Population p : getPopulation()) {
			if (p.getAmount() > 0) {
				double maxWealth = p.getAmount() * gParams.getBudgetMaxWealthPerPerson();
				double addWealth = Math.min(Math.max(maxWealth - p.getWealth(), 0),
						wealthForPops / populationAmount * p.getAmount());
				p.addWealth(addWealth);
				// people need money to support their life style
				double spendMoneyPerPerson = getTurn().addPerYear(gParams.getBudgetSpendMoneyPerPerson())
						* p.getWealth() / p.getAmount() / gParams.getBudgetMaxWealthPerPerson();
				p.addWealth(-p.getAmount() * spendMoneyPerPerson);
				income -= addWealth;
			}
		}

		if (income > 10) {
			spendMoneyForScience(income);
		}
	}

	private void setWealth(double wealth) {
		data.setWealth(wealth);
	}

	private void addWealth(double delta) {
		setWealth(Math.max(0, data.getWealth() + delta));
	}

	protected void sufferFromFight(List<Army> attackerArmies, List<Army> defenderArmies) {
		double loss = map.getGame().getGameParams().getProvinceLossFromFight();
		setWealth(data.getWealth() * (1 - loss));
		setInfrastructure((int) (getInfrastructure() * (1 - loss)));
		int populationAmount = getPopulationAmount();
		if (populationAmount > 0) {
			int attackerSoldiers = attackerArmies.stream().mapToInt(a -> a.getSoldiers()).sum();
			int defenderSoldiers = defenderArmies.stream().mapToInt(a -> a.getSoldiers()).sum();
			loss = Math.min(0.5, loss * (attackerSoldiers + defenderSoldiers / 10) / populationAmount);
			for (Population p : getPopulation()) {
				p.sufferFromWar(loss);
			}
		}
	}

	protected void sufferFromInvasion(int soldiers, double successfulInvasion) {
		if (getPopulation().isEmpty()) {
			return;
		}
		double loss = map.getGame().getGameParams().getProvinceLossFromFight();
		loss = Math.min(0.1, loss * soldiers / getPopulationAmount() * successfulInvasion);
		setWealth(data.getWealth() * (1 - loss));
		setInfrastructure((int) (getInfrastructure() * (1 - loss)));
		for (Population p : getPopulation()) {
			p.sufferFromWar(loss);
		}
	}

	protected void spendMoneyForScience(double money) {
		ScienceCollection.spendMoneyForScience(map.getGame(), this, money);
	}

	public void recruitPeopleForArmy(Army a, int soldiers) {
		int populationAmount = getPopulationAmount();
		if (populationAmount == 0) {
			return;
		}
		double fraction = Math.min(1, (double) soldiers / populationAmount);
		List<Population> pops = new ArrayList<>(getPopulation());
		for (Population pop : pops) {
			int soldiersFromPop = (int) (pop.getAmount() * fraction);
			Population recruit = pop.recruitPopForArmy(soldiersFromPop);
			if (recruit != null) {
				a.addPopulation(recruit);
			}
		}
	}

	public void setName(String name) {
		data.setName(name);
	}

	public void addLoyaltyToCountry(int id, Double delta) {
		getPopulation().forEach(p -> p.addLoyaltyToCountry(id, delta));
	}

	public void addLoyaltyToState(int id, Double delta) {
		addLoyaltyToState(id, delta, null);
	}

	public void addLoyaltyToState(int id, Double delta, Double maxStateLoyalty) {
		getPopulation().forEach(p -> p.addLoyaltyToState(id, delta, maxStateLoyalty));
	}

	public void decreaseLoyaltyToAllCountries(double coeff) {
		getPopulation().forEach(p -> p.decreaseLoyaltyToAllCountries(coeff));
	}

	public void decreaseLoyaltyToAllStates(double coeff) {
		getPopulation().forEach(p -> p.decreaseLoyaltyToAllStates(coeff));
	}

	public double getLoyaltyToCountry() {
		return getLoyaltyToCountry(getCountryId());
	}

	public double getLoyaltyToCountry(Integer countryId) {
		if (countryId == null) {
			return 0;
		}
		return getLoyaltyToCountry(countryId, getPopulationAmount());
	}

	double getLoyaltyToCountry(Integer countryId, int provincePopulation) {
		if (provincePopulation == 0) {
			return 0;
		}
		double loyalty = getPopulation().stream()
				.mapToDouble(pop -> pop.getAmount() * pop.getLoyaltyToCountry(countryId)).sum() / provincePopulation;
		if (ComparisonTool.isEqual(countryId, getCountryId())) {
			loyalty += getLoyaltyToCountryFromLocalCasualties();
			loyalty += country.getLoyaltyToCountryFromCountryCasualties();
			loyalty = country.getModifiers().getModifiedValue(CountryModifier.PROVINCE_LOYALTY, loyalty);
			double countryLoyaltyFromArmy = getLoyaltyToCountryFromArmy();
			if (countryLoyaltyFromArmy != 0) {
				loyalty = Math.max(loyalty, Math.min(loyalty + countryLoyaltyFromArmy,
						map.getGame().getGameParams().getPopulationLoyaltyArmyMax()));
			}
		}
		return Math.max(0, Math.min(loyalty, DataPopulation.LOYALTY_MAX));
	}

	/**
	 * only for UI
	 */
	public double getRawLoyaltyToCountryForUI() {
		int populationAmount = getPopulationAmount();
		if (getCountryId() == null || populationAmount == 0) {
			return 0;
		} else {
			return getPopulation().stream()
					.mapToDouble(pop -> pop.getAmount() * pop.getLoyaltyToCountry(getCountryId())).sum()
					/ populationAmount;
		}
	}

	Set<Integer> getCountriesWithLoyalty() {
		Set<Integer> countriesIds = new HashSet<>();
		getPopulation().forEach(pop -> countriesIds.addAll(pop.getLoyaltyToCountries().keySet()));
		return countriesIds;
	}

	Map<Integer, Integer> getLifeInCountries() {
		Map<Integer, Integer> lifeInCountries = new HashMap<>();
		int provincePopulation = getPopulationAmount();
		for (Population pop : getPopulation()) {
			Map<Integer, Integer> lInCs = pop.getLifeInCountries();
			for (Entry<Integer, Integer> entry : lInCs.entrySet()) {
				Integer years = lifeInCountries.get(entry.getKey());
				if (years == null) {
					years = 0;
				}
				lifeInCountries.put(entry.getKey(),
						(int) (years + 1.0 * entry.getValue() * pop.getAmount() / provincePopulation));
			}
		}
		return lifeInCountries;
	}

	public double getLoyaltyToCountryFromArmy() {
		Country c = getCountry();
		if (c == null) {
			return 0;
		}
		long soldiers = getArmies().stream().filter(a -> c.equals(a.getCountry())).mapToLong(a -> a.getSoldiers())
				.sum();
		if (soldiers == 0) {
			return 0;
		}
		double loyltyFromArmy = 1.0 * soldiers / getPopulationAmount()
				/ map.getGame().getGameParams().getPopulationLoyaltyArmySoldiersToPopulationThreshold();
		return loyltyFromArmy;
	}

	protected double getLoyaltyToCountryFromLocalCasualties() {
		long populationAmount = getPopulationAmount();
		if (populationAmount == 0) {
			return 0;
		}
		long casualties = getPopulation().stream().mapToLong(pop -> pop.getCasualties()).sum();
		return -Math.min(map.getGame().getGameParams().getPopulationCasualtiesLocalLoyaltyMaxSuffer(),
				1.0 * casualties / populationAmount);
	}

	public double getLoyaltyToState() {
		State c = getState();
		if (c == null) {
			return 0;
		}
		int populationAmount = getPopulationAmount();
		if (populationAmount == 0) {
			return 0;
		}
		return getPopulation().stream().mapToDouble(pop -> pop.getAmount() * pop.getLoyaltyToState(c.getId())).sum()
				/ populationAmount;
	}

	public boolean isPassable(Country c) {
		return new ProvincePassabilityCriteria(c).isPassable(this);
	}

	public boolean isPassable(int countryId) {
		return isPassable(map.getGame().findCountryById(countryId));
	}

	public boolean hasWaterNeighbor() {
		if (hasWaterNeighbor == null) {
			hasWaterNeighbor = getNeighbors().stream().filter(n -> n.getTerrainType().isWater()).findAny().isPresent();
		}
		return hasWaterNeighbor;
	}

	public double getDiseaseResistanceLevel() {
		return map.getGame().getScienceModificators().getDiseaseResistanceLevel(this);
	}

}
