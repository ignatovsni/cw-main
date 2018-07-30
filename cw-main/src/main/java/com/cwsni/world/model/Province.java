package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.cwsni.world.CwException;
import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.data.DataScience;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.Point;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.events.Event;
import com.cwsni.world.model.events.EventCollection;
import com.cwsni.world.model.events.EventEpidemic;
import com.cwsni.world.model.events.EventTarget;

public class Province implements EventTarget {

	private DataProvince data;
	private List<Province> neighbors;
	private List<Population> population;
	private WorldMap map;
	private EventCollection events;
	private List<Population> immigrants;
	private List<Army> armies;
	private Integer oldCapitalId;
	private double distanceToCapital;

	@Override
	public int hashCode() {
		return data.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
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
		if (data.getCountry() == null) {
			return null;
		} else {
			return getMap().getGame().findCountryById(data.getCountry());
		}
	}

	public Integer getCountryId() {
		return data.getCountry();
	}

	public void setCountry(Country c) {
		data.setCountry(c != null ? c.getId() : null);
	}

	public double getSoilFertility() {
		double v = data.getSoilFertility();
		// science
		v += (double) getScienceAgriculture()
				* map.getGame().getGameParams().getScienceAgricultureMultiplicatorForFertility();
		// climate change
		for (Event e : getEvents().getEventsWithType(Event.EVENT_GLOBAL_CLIMATE_CHANGE)) {
			v *= e.getEffectDouble1();
		}
		// infrastructure
		if (getPopulationAmount() > 0) {
			v = v * 0.5 * (1 + getInfrastructurePercent());
		}
		return v;
	}

	public int getScienceAgriculture() {
		return getScienceTypeValue(ds -> ds.getAgriculture());
	}

	public int getScienceMedicine() {
		return getScienceTypeValue(ds -> ds.getMedicine());
	}

	public int getScienceAdministration() {
		return getScienceTypeValue(ds -> ds.getAdministration());
	}

	private int getScienceTypeValue(Function<ScienceCollection, DataScience> getter4Science) {
		if (getPopulationAmount() == 0) {
			return 0;
		}
		long scienceValue = (long) (getPopulation().stream()
				.mapToDouble(pop -> pop.getAmount() * getter4Science.apply(pop.getScience()).getAmount()).sum()
				/ getPopulationAmount());
		return (int) scienceValue;
	}

	public int getSoilArea() {
		return data.getSoilArea();
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

	public int getSize() {
		return data.getSize();
	}

	public EventCollection getEvents() {
		return events;
	}

	public int getPopulationAmount() {
		return getPopulation().stream().mapToInt(p -> p.getAmount()).sum();
	}

	public int getSoilQuality() {
		return (int) (getSoilArea() * getSoilFertility());
	}

	public int getMaxPopulation() {
		return (int) (getSoilArea() * getSoilFertility());
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

	public double getDiseaseResistance() {
		return EventEpidemic.getDiseaseResistance(getScienceMedicine());
	}

	@Override
	public void addEvent(Event e) {
		getEvents().addEvent(e);
	}

	@Override
	public void removeEvent(Event e) {
		getEvents().removeEvent(e);
	}

	public void buildFrom(WorldMap worldMap, DataProvince dp) {
		this.map = worldMap;
		this.data = dp;
		events = new EventCollection();
		neighbors = new ArrayList<>();
		population = new ArrayList<>();
		immigrants = new ArrayList<>();
		data.getPopulation().forEach(dpop -> {
			Population pop = new Population();
			pop.buildFrom(this, dpop);
			population.add(pop);
		});
		data.getNeighbors().forEach(id -> neighbors.add(map.findProvById(id)));
		events.buildFrom(dp, worldMap.getGame());

		armies = new ArrayList<>();
	}

	private void processSoilArea() {
		int populationAmount = getPopulationAmount();
		double neededFieldsArea = (int) (populationAmount / getSoilFertility());
		int currentFieldsArea = getSoilArea();
		if (neededFieldsArea < currentFieldsArea) {
			// degradation
			double decreasing = Math.max(neededFieldsArea / currentFieldsArea, 0.98);
			setSoilArea((int) (decreasing * currentFieldsArea));
		} else {
			// improving
			if (populationAmount > 0) {
				double improving = Math.min(neededFieldsArea / currentFieldsArea, 1.002);
				int addSoilArea = (int) (currentFieldsArea * improving - currentFieldsArea);
				addSoilArea = Math.max(addSoilArea, 100);
				setSoilArea(currentFieldsArea + addSoilArea);
			}
		}
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

	private void setSoilArea(int newSoilArea) {
		data.setSoilArea(Math.min(Math.max(0, newSoilArea),
				data.getSize() * map.getGame().getGameParams().getSoilAreaPerSize()));
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
		population.add(pop);
		pop.setProvince(this);
	}

	public void removePopulation(Population pop) {
		population.remove(pop);
	}

	public void processNewTurn() {
		if (!getTerrainType().isPopulationPossible()) {
			return;
		}
		processSoilArea();
		if (getPopulationAmount() != 0) {
			ScienceCollection.growScienceNewTurn(this, map.getGame());
			Population.migrateNewTurn(this, map.getGame());
			Population.growPopulationNewTurn(this, map.getGame());
			Culture.influenceOfCountry(this, map.getGame());
		}
		Population.processEventsNewTurn(this, map.getGame());
		processNaturalInfrastructure();
		if (getPopulationAmount() != 0) {
			processTaxesAndOthersInNewTurn();
		}
		removeDiedPops();
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

	public double getWealth() {
		return getPopulation().stream().mapToDouble(p -> p.getWealth()).sum() + data.getWealth();
	}

	public double getGovernmentInfluence() {
		Country country = getCountry();
		if (country == null || country.getCapitalId() == null) {
			return 0;
		}
		Integer capitalId = country.getCapitalId();
		if (!ComparisonTool.isEqual(oldCapitalId, capitalId)) {
			if (ComparisonTool.isEqual(capitalId, getId())) {
				distanceToCapital = 0;
			} else {
				distanceToCapital = map.findDistanceBetweenProvs(capitalId, getId());
			}
			oldCapitalId = capitalId;
		}
		double adminScience = getScienceAdministration() + country.getCapital().getScienceAdministration();
		double effectiveDistanceToCapital = distanceToCapital - Math.log10(adminScience);
		if (effectiveDistanceToCapital <= 0) {
			return 1;
		} else {
			return Math.pow(map.getGame().getGameParams().getProvinceInfluenceFromCapitalWithDistanceDecrease(),
					effectiveDistanceToCapital);
		}
	}

	public double getFederalIncomePerYear() {
		return sumTaxForYear() * getCountry().getBudget().getProvinceTax() * getGovernmentInfluence();
	}

	private double getLocalIncomePerYear() {
		if (getCountryId() == null) {
			return sumTaxForYear() / 2;
		} else {
			return sumTaxForYear() * (1 - getCountry().getBudget().getProvinceTax() * getGovernmentInfluence());
		}
	}

	private double sumTaxForYear() {
		int populationAmount = getPopulationAmount();
		if (populationAmount <= 0) {
			return 0;
		}
		GameParams gParams = map.getGame().getGameParams();
		// absolutely poor people
		double income = populationAmount * gParams.getBudgetBaseTaxPerPerson();
		// wealth people
		income += getWealth() / gParams.getBudgetMaxWealthPerPerson() / 2
				* (gParams.getBudgetBaseTaxPerWealthPerson() - gParams.getBudgetBaseTaxPerPerson());
		// if the province lost population, then wealth can be much more than current
		// pops, so we should ignore too much wealth
		income = Math.min(income, populationAmount * gParams.getBudgetBaseTaxPerWealthPerson());
		return income;
	}

	private void processTaxesAndOthersInNewTurn() {
		GameParams gParams = map.getGame().getGameParams();
		int populationAmount = getPopulationAmount();
		double income = getLocalIncomePerYear();
		double wealthForProvince = income / 3;
		double wealthForPops = income / 3;
		double wealthForInfrastructure = income / 3;

		// province wealth
		double maxWealthForProv = populationAmount * gParams.getBudgetMaxWealthPerPerson();
		double addWealthForProv = Math.min(maxWealthForProv - data.getWealth(), wealthForProvince);
		addWealth(addWealthForProv);
		income -= addWealthForProv;

		// population wealth
		for (Population p : getPopulation()) {
			if (p.getAmount() > 0) {
				double maxWealth = p.getAmount() * gParams.getBudgetMaxWealthPerPerson();
				double addWealth = Math.min(maxWealth - p.getWealth(),
						wealthForPops / populationAmount * p.getAmount());
				p.addWealth(addWealth);
				income -= addWealth;
			}
		}

		// infrastructure
		if (getCountryId() != null) {
			double newInfrastructure = Math.min(getInfrastructure() + wealthForInfrastructure,
					getPopulationAmount() * gParams.getInfrastructureNaturalLimitWithLocalGovernment());
			setInfrastructure((int) newInfrastructure);
		}
	}

	private void setWealth(double wealth) {
		data.setWealth(wealth);
	}

	private void addWealth(double delta) {
		setWealth(Math.max(0, data.getWealth() + delta));
	}

	void sufferFromFight(List<Army> attackerArmies, List<Army> defenderArmies) {
		double loss = map.getGame().getGameParams().getProvinceLossFromFight();
		setWealth(data.getWealth() * (1 - loss));
		setInfrastructure((int) (getInfrastructure() * (1 - loss)));
		int populationAmount = getPopulationAmount();
		if (populationAmount > 0) {
			int attackerSoldiers = attackerArmies.stream().mapToInt(a -> a.getSoldiers()).sum();
			int defenderSoldiers = defenderArmies.stream().mapToInt(a -> a.getSoldiers()).sum();
			loss = Math.min(0.9, loss + (attackerSoldiers + defenderSoldiers) / populationAmount);
			for (Population p : getPopulation()) {
				p.sufferFromWar(loss);
			}
		}
	}

	void sufferFromInvading() {
		double loss = map.getGame().getGameParams().getProvinceLossFromFight();
		setWealth(data.getWealth() * (1 - loss));
		setInfrastructure((int) (getInfrastructure() * (1 - loss)));
		for (Population p : getPopulation()) {
			p.sufferFromWar(loss);
		}
	}

	void spendMoneyForScience(double money) {
		ScienceCollection.spendMoneyForScience(map.getGame(), this, money);
	}

	public void hirePeopleForArmy(Army a, int soldiers) {
		int populationAmount = getPopulationAmount();
		if (populationAmount == 0) {
			return;
		}
		double fraction = Math.min(1, (double) soldiers / populationAmount);
		int hiredSoldiers = 0;
		List<Population> pops = new ArrayList<>(getPopulation());
		for (Population pop : pops) {
			int soldiersFromPop = (int) (pop.getAmount() * fraction);
			if (fraction >= 1 || soldiersFromPop == pop.getAmount()) {
				hiredSoldiers += pop.getAmount();
				removePopulation(pop);
			} else {
				hiredSoldiers += pop.createNewPopFromThis(soldiersFromPop).getAmount();
			}
		}
		a.setSoldiers(hiredSoldiers);
	}
}
