package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataProvince {

	// --------------- map section --------------------

	private int id;
	private String name;
	private Point center;
	private List<Integer> neighbors;
	private TerrainType terrainType;
	private int size;
	private double soilFertility;

	// --------------- data section -------------------

	private Integer country;
	/**
	 * current area
	 */
	private int soilArea;
	/**
	 * size of the province, it is a synthetic concept
	 */
	private int infrastructure;
	private List<DataPopulation> population;
	private Set<Integer> events;

	public DataProvince() {
		this(-1, null, 0, 0);
	}

	public DataProvince(int id, String name, double x, double y) {
		this.id = id;
		this.name = name != null ? name : "#" + String.valueOf(id);
		this.center = new Point(x, y);
		this.population = new ArrayList<>(1);
		this.neighbors = new ArrayList<>();
		this.terrainType = TerrainType.OCEAN;
		events = new HashSet<>();
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

	public Set<Integer> getEvents() {
		return events;
	}

	public void setEvents(Set<Integer> eventsIds) {
		this.events = eventsIds;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getInfrastructure() {
		return infrastructure;
	}

	public void setInfrastructure(int infrastructure) {
		this.infrastructure = infrastructure;
	}

	public Integer getCountry() {
		return country;
	}

	public void setCountry(Integer country) {
		this.country = country;
	}

}
