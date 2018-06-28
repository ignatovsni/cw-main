package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.cwsni.world.CwException;
import com.cwsni.world.common.algorithms.Node;
import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.data.DataScience;
import com.cwsni.world.model.data.Point;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.events.Event;
import com.cwsni.world.model.events.EventCollection;
import com.cwsni.world.model.events.EventTarget;

public class Province implements EventTarget, Node<Province> {

	private DataProvince data;
	private List<Province> neighbors;
	private List<Population> population;
	private WorldMap map;
	private EventCollection events;
	private List<Population> immigrants;
	private List<Army> armies;

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
			v = v * 0.5 * (1 + getInfrastructure());
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
		long scienceValue = getPopulation().stream()
				.mapToLong(pop -> (long) pop.getAmount() * getter4Science.apply(pop.getScience()).getAmount()).sum()
				/ getPopulationAmount();
		return (int) scienceValue;
	}

	public int getSoilArea() {
		return data.getSoilArea();
	}

	public int getInfrastructureAbsoluteValue() {
		return data.getInfrastructure();
	}

	public double getInfrastructure() {
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
		double r = Math.min(0.9,
				getScienceMedicine() * getMap().getGame().getGameParams().getScienceMedicineMultiplicatorForEpidemic());
		return r * r;
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

	private void processProvincePropertiesNewTurn() {
		processSoilArea();
		processInfrastructure();
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

	private void processInfrastructure() {
		// natural growth can not excess infrastructureNaturalLimitFromPopulation
		int populationAmount = getPopulationAmount();
		int neededInfrastructure = (int) (populationAmount
				* map.getGame().getGameParams().getInfrastructureNaturalLimitFromPopulation());
		int currentInfrastructure = Math.max(getInfrastructureAbsoluteValue(), 1);
		if (neededInfrastructure < currentInfrastructure) {
			// degradation
			double decreasing = Math.max((double) neededInfrastructure / currentInfrastructure, 0.99);
			setInfrastructure((int) (decreasing * getInfrastructureAbsoluteValue()));
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
		if (getPopulationAmount() != 0) {
			ScienceCollection.growScienceNewTurn(this, map.getGame());
		}
		Population.processEventsNewTurn(this, map.getGame());
		if (getPopulationAmount() != 0) {
			Population.migrateNewTurn(this, map.getGame());
		}
		processProvincePropertiesNewTurn();
		if (getPopulationAmount() != 0) {
			Population.growPopulationNewTurn(this, map.getGame());
		}
		removeDiedPops();
	}

	private void removeDiedPops() {
		Iterator<Population> iter = population.iterator();
		while (iter.hasNext()) {
			Population pop = iter.next();
			if (pop.getAmount() == 0) {
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

	@Override
	public int hashCode() {
		return data.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		return ((Province) obj).getId() == getId();
	}

	@Override
	public String toString() {
		return "province with id = " + getId() + ";";
	}
}
