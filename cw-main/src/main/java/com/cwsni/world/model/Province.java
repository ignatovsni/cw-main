package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.model.events.Event;
import com.cwsni.world.model.events.EventCollection;
import com.cwsni.world.model.events.EventTarget;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Province implements EventTarget {

	private int id;
	private String name;
	private Point center;

	/**
	 * It is used only for save/load Primary list of neighbors is stored in list
	 * 'neighbors'
	 */
	private int[] neighborsById;
	@JsonIgnore
	private List<Province> neighbors;

	private TerrainType terrainType;

	private double soilFertility;
	private int soilArea;

	private List<Population> population;

	private EventCollection events = new EventCollection();

	@JsonIgnore
	private transient ProvinceTransientProps tp;

	private WorldMap worldMap;

	public Province() {
		this(-1, 0, 0);
	}

	public Province(int id, int x, int y) {
		this.tp = new ProvinceTransientProps();
		this.id = id;
		this.name = String.valueOf(id);
		this.center = new Point(x, y);
		this.population = new ArrayList<>(1);
		this.neighbors = new ArrayList<>();
		this.neighborsById = new int[0];
		this.terrainType = TerrainType.OCEAN;
	}

	public List<Province> getNeighbors() {
		return neighbors;
	}

	public int[] getNeighborsById() {
		neighborsById = new int[neighbors.size()];
		for (int i = 0; i < neighbors.size(); i++) {
			neighborsById[i] = neighbors.get(i).getId();
		}
		return neighborsById;
	}

	public void setNeighborsById(int[] neighborsById) {
		this.neighborsById = neighborsById;
	}

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Population> getPopulation() {
		return population;
	}

	public void setPopulation(List<Population> population) {
		this.population = population;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public double getSoilFertilityEff() {
		double v = getSoilFertility();
		for (Event e : events.getEvents()) {
			if (Event.EVENT_GLOBAL_CLIMATE_CHANGE.equals(e.getType())) {
				v *= e.getEffectDouble1();
			}
		}
		return v;
	}

	public double getSoilFertility() {
		return soilFertility;
	}

	public void setSoilFertility(double soilFertility) {
		this.soilFertility = soilFertility;
	}

	@JsonIgnore
	public int getSoilAreaEff() {
		return getSoilArea();
	}

	public int getSoilArea() {
		return soilArea;
	}

	public void setSoilArea(int soilArea) {
		this.soilArea = soilArea;
	}

	public TerrainType getTerrainType() {
		return terrainType;
	}

	public void setTerrainType(TerrainType terrainType) {
		this.terrainType = terrainType;
	}

	public EventCollection getEvents() {
		return events;
	}

	public void setEvents(EventCollection events) {
		this.events = events;
	}

	@JsonIgnore
	public int getPopulationAmount() {
		return getPopulation().stream().mapToInt(p -> p.getAmount()).sum();
	}

	/**
	 * Finishes game preparing after loading
	 */
	public void postLoad(WorldMap map) {
		this.worldMap = map;
		getNeighbors().clear();
		for (int id : neighborsById) {
			getNeighbors().add(map.findProvById(id));
		}
		if (getTerrainType() != TerrainType.OCEAN) {
			events.postLoad(map.getGame());
		}
	}

	public void postGenerate(WorldMap map) {
		this.worldMap = map;
		events.postGenerate(map.getGame());
	}

	public void checkCorrectness() {
		switch (getTerrainType()) {
		case OCEAN:
			internalAssert(getPopulation().size() == 0, "ocean province can not have population");
			internalAssert(getSoilArea() == 0, "ocean province must have soil amount = 0");
			internalAssert(getSoilFertility() == 0, "ocean province must have soil fertility = 0");
			break;
		case GRASSLAND:
			break;
		}
	}

	private void internalAssert(boolean c, String errorText) {
		if (!c) {
			throw new RuntimeException("Failed to check province " + getId() + ": " + errorText);
		}
	}

	@JsonIgnore
	public int getSoilQuality() {
		return (int) (getSoilAreaEff() * getSoilFertilityEff());
	}

	@JsonIgnore
	public int getMaxPopulation() {
		return (int) (getSoilAreaEff() * getSoilFertilityEff());
	}

	@JsonIgnore
	public double getPopulationExcess() {
		return (double) getPopulationAmount() / Math.max(getMaxPopulation(), 1);
	}

	public void processNewTurn() {
		if (!getTerrainType().isPopulationPossible() || getPopulationAmount() == 0) {
			return;
		}
		GameParams gParams = worldMap.getGame().getGameParams();
		Population.processEvents(worldMap.getGame(), this);
		Population.migrateIfHaveTo(this, gParams);
		Population.growPopulation(this, worldMap.getGame());
		tp.populationExcess = getPopulationAmount() / getMaxPopulation();
	}

	@Override
	public void addEvent(Event e) {
		events.add(e);
	}

	@Override
	public void removeEvent(Event e) {
		events.removeEvent(e);
	}

}
