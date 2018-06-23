package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.data.DataScience;
import com.cwsni.world.model.data.Point;
import com.cwsni.world.model.data.TerrainType;
import com.cwsni.world.model.events.Event;
import com.cwsni.world.model.events.EventCollection;
import com.cwsni.world.model.events.EventTarget;

public class Province implements EventTarget {

	private DataProvince data;
	private List<Province> neighbors;
	private List<Population> population;
	private WorldMap map;
	private EventCollection events;
	private List<Population> immigrants;

	public List<Province> getNeighbors() {
		return neighbors;
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

	public String getName() {
		return data.getName();
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
		return v;
	}

	public int getScienceAgriculture2() {
		if (getPopulationAmount() == 0) {
			return 0;
		}
		long agricultureLevel = getPopulation().stream()
				.mapToLong(pop -> (long) pop.getAmount() * pop.getScience().getAgriculture().getAmount()).sum()
				/ getPopulationAmount();
		return (int) agricultureLevel;
	}

	public int getScienceAgriculture() {
		return getScienceTypeValue(ds -> ds.getAgriculture());
	}

	public int getScienceMedicine() {
		return getScienceTypeValue(ds -> ds.getMedicine());
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

	public int getArea() {
		return data.getArea();
	}

	public TerrainType getTerrainType() {
		return data.getTerrainType();
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
		return Math.min(0.99,
				getScienceMedicine() * getMap().getGame().getGameParams().getScienceMedicineMultiplicatorForEpidemic());
	}

	@Override
	public void addEvent(Event e) {
		getEvents().add(e);
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

	}

	public void processNewTurn() {
		if (!getTerrainType().isPopulationPossible() || getPopulationAmount() == 0) {
			return;
		}
		ScienceCollection.growScienceNewTurn(this, map.getGame());		
		Population.processEventsNewTurn(this, map.getGame());		
		Population.migrateNewTurn(this, map.getGame());
		processProvincePropertiesNewTurn();
		Population.growPopulationNewTurn(this, map.getGame());		
	}

	private void processProvincePropertiesNewTurn() {
		double neededFieldsArea = (int) (getPopulationAmount() / getSoilFertility());
		int currentFieldsArea = getSoilArea();
		if (neededFieldsArea < currentFieldsArea) {
			// degradation
			double decreasing = Math.max(neededFieldsArea / currentFieldsArea, 0.98);
			setSoilArea((int) (decreasing * getSoilArea()));
		} else {
			// improving
			double improving = Math.min(neededFieldsArea / currentFieldsArea, 1.002);
			setSoilArea((int) (getSoilArea() * improving));
		}
	}

	private void setSoilArea(int newSoilArea) {
		data.setSoilArea((int) Math.min(data.getArea(), Math.max(newSoilArea,
				data.getArea() * map.getGame().getGameParams().getSoilAreaMinPercentFromMaxArea())));
	}

	public void processImmigrantsAndMergePops() {
		if (!getTerrainType().isPopulationPossible()) {
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

}
