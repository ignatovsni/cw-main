package com.cwsni.world.model.data;

import com.cwsni.world.client.desktop.util.DataFormatter;

public class DataCulture {

	private double red;
	private double green;
	private double blue;

	public double getRed() {
		return red;
	}

	public void setRed(double red) {
		this.red = correctValue(red);
	}

	public double getGreen() {
		return green;
	}

	public void setGreen(double green) {
		this.green = correctValue(green);
	}

	public double getBlue() {
		return blue;
	}

	public void setBlue(double blue) {
		this.blue = correctValue(blue);
	}

	private double correctValue(double v) {
		if (v <= 0) {
			return 0;
		} else if (v >= 255) {
			return 255;
		}
		v = DataFormatter.doubleWith3points(v);
		return v;
	}

	public void cloneFrom(DataCulture culture) {
		red = culture.red;
		green = culture.green;
		blue = culture.blue;
	}

}
