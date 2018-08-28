package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "continents", "oceans", "provinces" })
public class DataWorldMap {

	private List<DataProvince> provinces;
	private int continents;
	private int oceans;

	public DataWorldMap() {
		provinces = new ArrayList<>();
	}

	public List<DataProvince> getProvinces() {
		return provinces;
	}

	public void setProvinces(List<DataProvince> provinces) {
		this.provinces = provinces;
	}

	public void addProvince(DataProvince province) {
		provinces.add(province);
	}

	public int getContinents() {
		return continents;
	}

	public void setContinents(int continents) {
		this.continents = continents;
	}

	public int getOceans() {
		return oceans;
	}

	public void setOceans(int oceans) {
		this.oceans = oceans;
	}

}
