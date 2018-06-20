package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.model.events.EventCollection;

public class DataProvince {

	private int id;
	private String name;
	private Point center;
	/**
	 * It is used only for save/load Primary list of neighbors is stored in list
	 * 'neighbors'
	 */
	private List<Integer> neighbors;
	private TerrainType terrainType;
	private double soilFertility;
	private int soilArea;
	private List<DataPopulation> population;
	private EventCollection events;

	public DataProvince() {
		this(-1, 0, 0);
	}

	public DataProvince(int id, int x, int y) {
		this.id = id;
		this.name = String.valueOf(id);
		this.center = new Point(x, y);
		this.population = new ArrayList<>(1);
		this.neighbors = new ArrayList<>();
		this.terrainType = TerrainType.OCEAN;
		events = new EventCollection();
	}

	public List<Integer> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(List<Integer> neighbors) {
		this.neighbors = neighbors;
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

	public List<DataPopulation> getPopulation() {
		return population;
	}

	public void setPopulation(List<DataPopulation> population) {
		this.population = population;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getSoilFertility() {
		return soilFertility;
	}

	public void setSoilFertility(double soilFertility) {
		this.soilFertility = soilFertility;
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

}
