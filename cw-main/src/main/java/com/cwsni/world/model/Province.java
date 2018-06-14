package com.cwsni.world.model;

public class Province {
	
	final private int id;
	private Point center;	
	
	public Province(int id, double x, double y) {
		this.id = id;
		this.center = new Point(x, y);
	}
	
	public Point getCenter() {
		return center;
	}

	public int getId() {
		return id;
	}
	
}
