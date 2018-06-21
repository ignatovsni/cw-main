package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.model.data.DataProvince;
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
		return population;
	}

	public String getName() {
		return data.getName();
	}

	public double getSoilFertility() {
		double v = data.getSoilFertility();
		// climate change
		for (Event e : getEvents().getEventsWithType(Event.EVENT_GLOBAL_CLIMATE_CHANGE)) {
			v *= e.getEffectDouble1();
		}
		// science
		v += (double)getScienceAgriculture() / 4000;
		return v;
	}

	public int getScienceAgriculture() {
		if (getPopulationAmount() == 0) {
			return 0;
		}
		long agricultureLevel = getPopulation().stream()
				.mapToLong(pop -> (long)pop.getAmount() * pop.getScience().getAgriculture().getAmount()).sum()
				/ getPopulationAmount();
		return (int)agricultureLevel;
	}

	public long getSoilArea() {
		return data.getSoilArea() + getScienceAgriculture()*10;
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

	public void processNewTurn() {
		if (!getTerrainType().isPopulationPossible() || getPopulationAmount() == 0) {
			return;
		}
		Population.processEvents(this, map.getGame());
		Population.migrate(this, map.getGame());
		Population.growPopulation(this, map.getGame());
		ScienceCollection.growScience(this, map.getGame());

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
			pop.buildFrom(dpop);
			population.add(pop);
		});
		data.getNeighbors().forEach(id -> neighbors.add(map.findProvById(id)));
		events.buildFrom(dp, worldMap.getGame());

	}

	public List<Population> getImmigrants() {
		return immigrants;
	}

	public void processImmigrantsAndMergePops() {
		if (!getTerrainType().isPopulationPossible()) {
			return;
		}
		Population.processImmigrantsAndMergePops(this, map.getGame());
		data.getPopulation().clear();
		getPopulation().forEach(pop -> data.getPopulation().add(pop.getPopulationData()));
	}

}
