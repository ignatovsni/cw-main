package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WorldMap {
	
	private List<Province> provinces = new ArrayList<>();
	
	public WorldMap(int numberOfprovinces) {
		provinces = new ArrayList<>(numberOfprovinces);
	}

	public static WorldMap createMap(int rows, int columns, int provinceRadius) {
		double xStep = 1.75;
		double yStep = 1.51;
		WorldMap map = new WorldMap(rows*columns);
		double x = provinceRadius;
		double y = provinceRadius;
		int idx = 0;
		for (int row=0; row<rows; row++) {
			x = provinceRadius * xStep/2 * (row%2) ;
			for (int column=0; column<columns; column++) {
				Province province = new Province(idx++, x, y);
				map.provinces.add(province);
				x += provinceRadius * xStep;
			}
			y += provinceRadius * yStep;
		}		
		return map;
	}

	public Stream<Province> getProvinces() {
		return provinces.stream();
	}

	public int getNumberOfProvinces() {
		return provinces.size();
	}

}
