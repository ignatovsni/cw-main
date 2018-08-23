package com.cwsni.world.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

	public Color(Color c) {
		this(c.getR(), c.getG(), c.getB());
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

	@JsonIgnore
	public javafx.scene.paint.Color getJavaFxColor() {
		return new javafx.scene.paint.Color(getR() / 255.0, getG() / 255.0, getB() / 255.0, 1);
	}

}
