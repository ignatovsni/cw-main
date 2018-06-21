package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.data.GameParams;
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
		for (Event e : getEvents().getEventsWithType(Event.EVENT_GLOBAL_CLIMATE_CHANGE)) {
			v *= e.getEffectDouble1();
		}
		return v;
	}

	public int getSoilArea() {
		return data.getSoilArea();
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
		GameParams gParams = map.getGame().getGameParams();
		Population.processEvents(map.getGame(), this);
		Population.migrate(this, gParams);
		Population.growPopulation(this, map.getGame());
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
		this.neighbors = new ArrayList<>();
		this.population = new ArrayList<>();
		data.getPopulation().forEach(dpop -> {
			Population pop = new Population();
			pop.buildFrom(this, dpop);
			population.add(pop);
		});
		data.getNeighbors().forEach(id -> neighbors.add(map.findProvById(id)));
		events.buildFrom(dp, worldMap.getGame());
	}

}
