package com.cwsni.world.model.data;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "name", "color", "capital" })
public class DataState {
	private int id;
	private String name;
	private Color color;
	private Integer capital;
	private Set<Integer> provinces = new HashSet<>();
	private Integer lastRebelCountryId;

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

	public Integer getCapital() {
		return capital;
	}

	public void setCapital(Integer capital) {
		this.capital = capital;
	}

	public Set<Integer> getProvinces() {
		return provinces;
	}

	public void setProvinces(Set<Integer> provinces) {
		this.provinces = provinces;
	}

	public Integer getLastRebelCountryId() {
		return lastRebelCountryId;
	}

	public void setLastRebelCountryId(Integer lastRebelCountryId) {
		this.lastRebelCountryId = lastRebelCountryId;
	}

}
