package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Province {
	
	private int id;
	private String name;
	private Point center;
	private int soilFertility;
	private int soilAmount;
	private List<Population> population;
	
	public Province() {
		this(-1, 0, 0);
	}
	
	public Province(int id, int x, int y) {
		this.id = id;
		this.name = String.valueOf(id);
		this.center = new Point(x, y);
		this.population = new ArrayList<>(1);
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
	
	public int getSoilFertility() {
		return soilFertility;
	}

	public void setSoilFertility(int soilFertility) {
		this.soilFertility = soilFertility;
	}

	public int getSoilAmount() {
		return soilAmount;
	}

	public void setSoilAmount(int soilAmount) {
		this.soilAmount = soilAmount;
	}

	@JsonIgnore
	public int getPopulationAmount() {
		return getPopulation().stream()
				.mapToInt(p -> p.getAmount())
				.sum();
	}
	
}
