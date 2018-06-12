package com.cwsni.world.model;

public class Province {
	
	private Point center;	
	
	public Province(double x, double y) {
		this.center = new Point(x, y);
	}
	
	public Point getCenter() {
		return center;
	}

}
