package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

public class DataWorldMap {

	private List<DataProvince> provinces;

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

}
