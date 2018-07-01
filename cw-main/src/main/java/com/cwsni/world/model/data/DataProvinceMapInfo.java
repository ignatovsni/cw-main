package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

/**
 * In fact, this data is a map information, "hard-coded" or "starting"
 *
 */
public class DataProvinceMapInfo {

	private int id;
	private String name;
	private Point center;
	private List<Point> points;
	private List<Integer> neighbors;
	private TerrainType terrainType;
	private int size;

	public DataProvinceMapInfo() {
		this(-1, null, null);
	}

	public DataProvinceMapInfo(int id, Point center, List<Point> points) {
		this.setId(id);
		this.points = points;
		this.name = String.valueOf(id);
		this.center = center;
		this.neighbors = new ArrayList<>();
		this.terrainType = TerrainType.OCEAN;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Integer> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(List<Integer> neighbors) {
		this.neighbors = neighbors;
	}

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TerrainType getTerrainType() {
		return terrainType;
	}

	public void setTerrainType(TerrainType terrainType) {
		this.terrainType = terrainType;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
