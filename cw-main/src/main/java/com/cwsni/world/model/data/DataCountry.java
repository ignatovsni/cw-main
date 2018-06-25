package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

public class DataCountry {

	private int id;
	private String name;
	private Color color;
	private List<DataArmy> armies = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public List<DataArmy> getArmies() {
		return armies;
	}

	public void setArmies(List<DataArmy> armies) {
		this.armies = armies;
	}

}
