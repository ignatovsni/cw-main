package com.cwsni.world.model;

import java.util.ArrayList;
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
		return provinces;
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

	public static WorldMap createMap(int rows, int columns, double provinceRadius) {
		double xStep = 1.75;
		double yStep = 1.51;
		WorldMap map = new WorldMap(provinceRadius);
		double x = provinceRadius;
		double y = provinceRadius;
		int idx = 0;
		for (int row = 0; row < rows; row++) {
			x = provinceRadius * xStep / 2 * (row % 2);
			for (int column = 0; column < columns; column++) {
				Province province = new Province(idx++, (int) x, (int) y);
				map.provinces.add(province);
				map.mapProvById.put(province.getId(), province);
				setLinks(province, map, row, column, columns);
				x += provinceRadius * xStep;
			}
			y += provinceRadius * yStep;
		}
		return map;
	}

	private static void setLinks(Province p, WorldMap map, int y, int x, int columns) {
		int id = p.getId();
		if (x > 0) {
			Province leftProv = map.mapProvById.get(id - 1);
			p.getNeighbors().add(leftProv);
			leftProv.getNeighbors().add(p);
		}
		if (y > 0) {
			if (y % 2 == 1) {
				Province leftProv = map.mapProvById.get((y - 1) * columns + x);
				p.getNeighbors().add(leftProv);
				leftProv.getNeighbors().add(p);
				if (x < (columns - 1)) {
					Province rightProv = map.mapProvById.get((y - 1) * columns + x + 1);
					p.getNeighbors().add(rightProv);
					rightProv.getNeighbors().add(p);
				}
			} else {
				Province rightProv = map.mapProvById.get((y - 1) * columns + x);
				p.getNeighbors().add(rightProv);
				rightProv.getNeighbors().add(p);
				if (x > 0) {
					Province leftProv = map.mapProvById.get((y - 1) * columns + x - 1);
					p.getNeighbors().add(leftProv);
					leftProv.getNeighbors().add(p);
				}
			}
		}
	}

	/**
	 * Finishes game preparing after loading
	 */
	public void postLoad() {
		mapProvById.clear();
		getProvinces().forEach(p -> mapProvById.put(p.getId(), p));
		getProvinces().forEach(p -> p.postLoad(this));
	}

}
