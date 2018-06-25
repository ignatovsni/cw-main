package com.cwsni.world.model.data;

public class Color {

	private int r;
	private int g;
	private int b;

	public Color() {
	}

	public Color(int red, int green, int blue) {
		this.r = red;
		this.g = green;
		this.b = blue;
	}

	public int getR() {
		return r;
	}

	public void setR(int red) {
		this.r = red;
	}

	public int getG() {
		return g;
	}

	public void setG(int green) {
		this.g = green;
	}

	public int getB() {
		return b;
	}

	public void setB(int blue) {
		this.b = blue;
	}

}
