package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldMap {

	private List<Province> provinces = new ArrayList<>();
	private Map<Integer, Province> mapProvById;
	private double provinceRadius;

	public WorldMap() {
		this(0);
	}

	public WorldMap(double provinceRadius) {
		this.provinceRadius = provinceRadius;
		provinces = new ArrayList<>();
		mapProvById = new HashMap<>();
	}

	public List<Province> getProvinces() {
		return Collections.unmodifiableList(provinces);
	}

	public void setProvinces(List<Province> provinces) {
		this.provinces = provinces;
	}

	public double getProvinceRadius() {
		return provinceRadius;
	}

	public void setProvinceRadius(double provinceRadius) {
		this.provinceRadius = provinceRadius;
	}

	public Province findProvById(Integer id) {
		return mapProvById.get(id);
	}

	public void addProvince(Province p) {
		provinces.add(p);
		mapProvById.put(p.getId(), p);
	}

	public void postGenerate() {
		getProvinces().forEach(p -> p.postGenerate());
	}

	/**
	 * Finishes game preparing after loading
	 */
	public void postLoad() {
		mapProvById.clear();
		getProvinces().forEach(p -> mapProvById.put(p.getId(), p));
		getProvinces().forEach(p -> p.postLoad(this));
	}

	public void checkCorrect() {
		getProvinces().forEach(p -> p.checkCorrect());
	}

}
