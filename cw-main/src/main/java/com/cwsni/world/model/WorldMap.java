package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;

public class WorldMap {

	private List<Province> provinces = new ArrayList<>();
	private double provinceRadius;

	public WorldMap() {
		this(0);
	}

	public WorldMap(double provinceRadius) {
		this.provinceRadius = provinceRadius;
		provinces = new ArrayList<>();
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
				Province province = new Province(idx++, x, y);
				map.provinces.add(province);
				x += provinceRadius * xStep;
			}
			y += provinceRadius * yStep;
		}
		return map;
	}

}
