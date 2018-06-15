package com.cwsni.world.model;

public class Province {
	
	private int id;
	private Point center;	
	
	public Province() {
		this(-1, 0, 0);
	}
	
	public Province(int id, double x, double y) {
		this.id = id;
		this.center = new Point(x, y);
	}
	
	public Point getCenter() {
		return center;
	}
	
	public void setCenter(Point center) {
		this.center = center;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	
}
