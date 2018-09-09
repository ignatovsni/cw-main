package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataProvince {

	// --------------- map section --------------------

	private int id;
	private String name;
	private int continentId;
	private Point center;
	private List<Integer> neighbors;
	private TerrainType terrainType;
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double size;
	private List<DataFoodResource> foodResources;

	// --------------- data section -------------------

	/**
	 * size of the province, it is a synthetic concept
	 */
	private int infrastructure;
	private List<DataPopulation> population;

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double wealth;

	public DataProvince() {
		this(-1, null, 0, 0);
	}

	public DataProvince(int id, String name, double x, double y) {
		this.id = id;
		this.name = name != null ? name : "#" + String.valueOf(id);
		this.center = new Point(x, y);
		this.population = new ArrayList<>(1);
		this.neighbors = new ArrayList<>();
		this.foodResources = new ArrayList<>(1);
		this.terrainType = TerrainType.OCEAN;
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DataProvince)) {
			return false;
		}
		return ((DataProvince) obj).getId() == getId();
	}

	@Override
	public String toString() {
		return "province with id = " + getId() + ";";
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

	public TerrainType getTerrainType() {
		return terrainType;
	}

	public void setTerrainType(TerrainType terrainType) {
		this.terrainType = terrainType;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public int getInfrastructure() {
		return infrastructure;
	}

	public void setInfrastructure(int infrastructure) {
		this.infrastructure = infrastructure;
	}

	public double getWealth() {
		return wealth;
	}

	public void setWealth(double wealth) {
		this.wealth = wealth;
	}

	public int getContinentId() {
		return continentId;
	}

	public void setContinentId(int continentId) {
		this.continentId = continentId;
	}

	public List<DataFoodResource> getFoodResources() {
		return foodResources;
	}

	public void setFoodResources(List<DataFoodResource> foodResources) {
		this.foodResources = foodResources;
	}

}
