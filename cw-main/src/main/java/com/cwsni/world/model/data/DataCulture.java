package com.cwsni.world.model.data;

public class DataCulture {

	private int red;
	private int green;
	private int blue;

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public void cloneFrom(DataCulture culture) {
		red = culture.red;
		green = culture.green;
		blue = culture.blue;
	}

}
