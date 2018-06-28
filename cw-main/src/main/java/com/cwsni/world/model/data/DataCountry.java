package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

public class DataCountry {

	private int id;
	private String name;
	private Color color;
	private Integer capital;
	private Integer firstCapital;
	private List<DataArmy> armies = new ArrayList<>();
	private boolean isAI = true;

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

	public boolean isAI() {
		return isAI;
	}

	public void setAI(boolean isAI) {
		this.isAI = isAI;
	}

	public Integer getCapital() {
		return capital;
	}

	public void setCapital(Integer capital) {
		this.capital = capital;
	}

	public Integer getFirstCapital() {
		return firstCapital;
	}

	public void setFirstCapital(Integer firstCapital) {
		this.firstCapital = firstCapital;
	}

}
