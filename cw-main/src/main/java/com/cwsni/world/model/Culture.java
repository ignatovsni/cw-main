package com.cwsni.world.model;

import com.cwsni.world.model.data.DataCulture;

public class Culture {

	private DataCulture culture;

	public Culture() {
		culture = new DataCulture();
	}

	public Culture(DataCulture culture) {
		this.culture = culture;
	}

	public int getRed() {
		return culture.getRed();
	}

	public int getGreen() {
		return culture.getGreen();
	}

	public int getBlue() {
		return culture.getBlue();
	}

	public void buildFrom(DataCulture culture) {
		this.culture = culture;
	}

	public void cloneFrom(Culture from) {
		culture.cloneFrom(from.getCulture());
	}

	DataCulture getCulture() {
		return culture;
	}

	public void mergeFrom(Culture from, double ownFraction) {
		// double avg = (ownFraction * scienceType.getAmount() +
		// fromScienceType.getAmount() * (1 - ownFraction));
		ownFraction = Math.sqrt(ownFraction);
		if (ownFraction < 0.99) {
			culture.setRed(merge(culture.getRed(), from.getCulture().getRed(), ownFraction));
			culture.setGreen(merge(culture.getGreen(), from.getCulture().getGreen(), ownFraction));
			culture.setBlue(merge(culture.getBlue(), from.getCulture().getBlue(), ownFraction));
		}
	}
	
	private int merge(int to, int from, double ownFraction) {
		return (int) Math.round((to*ownFraction + from*(1-ownFraction)));
	}

}
