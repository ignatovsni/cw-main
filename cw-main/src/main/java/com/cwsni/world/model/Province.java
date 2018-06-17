package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Province {

	private int id;
	private String name;
	private Point center;

	/**
	 * It is used only for save/load Primary list of neighbors is stored in list
	 * 'neighbors'
	 */
	private int[] neighborsById;
	private List<Province> neighbors;

	private TerrainType terrainType;

	private double soilFertility;
	private int soilArea;

	private List<Population> population;

	@JsonIgnore
	private transient Point coordAsHex;

	public Province() {
		this(-1, 0, 0);
	}

	public Province(int id, int x, int y) {
		this.id = id;
		this.name = String.valueOf(id);
		this.center = new Point(x, y);
		this.population = new ArrayList<>(1);
		this.neighbors = new ArrayList<>();
		this.neighborsById = new int[0];
		this.terrainType = TerrainType.OCEAN;
	}

	@JsonIgnore
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

	@JsonIgnore
	public int getPopulationAmount() {
		return getPopulation().stream().mapToInt(p -> p.getAmount()).sum();
	}

	/**
	 * Finishes game preparing after loading
	 */
	public void postLoad(WorldMap map) {
		getNeighbors().clear();
		for (int id : neighborsById) {
			getNeighbors().add(map.findProvById(id));
		}
	}

	public void postGenerate() {
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
		return (int) (getSoilArea() * getSoilFertility());
	}

	public Point getCoordAsHex() {
		return coordAsHex;
	}

	public void setCoordAsHex(int row, int column) {
		coordAsHex = new Point(row, column);
	}

}
