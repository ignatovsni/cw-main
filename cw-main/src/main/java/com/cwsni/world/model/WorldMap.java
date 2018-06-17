package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WorldMap {

	private List<Province> provinces = new ArrayList<>();
	private Map<Integer, Province> mapProvById;
	private Game game;

	public WorldMap() {
		provinces = new ArrayList<>();
		mapProvById = new HashMap<>();
	}

	public List<Province> getProvinces() {
		return Collections.unmodifiableList(provinces);
	}

	public void setProvinces(List<Province> provinces) {
		this.provinces = provinces;
	}

	public Province findProvById(Integer id) {
		return mapProvById.get(id);
	}

	public void addProvince(Province p) {
		provinces.add(p);
		mapProvById.put(p.getId(), p);
	}

	public void postGenerate(Game game) {
		this.game = game;
		getProvinces().forEach(p -> p.postGenerate(this));
	}

	/**
	 * Finishes game preparing after loading
	 */
	public void postLoad(Game game) {
		this.game = game;
		mapProvById.clear();
		getProvinces().forEach(p -> mapProvById.put(p.getId(), p));
		getProvinces().forEach(p -> p.postLoad(this));
	}

	public void checkCorrect() {
		getProvinces().forEach(p -> p.checkCorrectness());
	}

	@JsonIgnore
	public Game getGame() {
		return game;
	}

}
