package com.cwsni.world.model;

import com.cwsni.world.model.data.DataCulture;

public class Culture {

	private DataCulture data;

	public Culture() {
		data = new DataCulture();
	}

	public Culture(DataCulture culture) {
		this.data = culture;
	}

	public int getRed() {
		return data.getRed();
	}

	public int getGreen() {
		return data.getGreen();
	}

	public int getBlue() {
		return data.getBlue();
	}

	public void buildFrom(DataCulture culture) {
		this.data = culture;
	}

	public void cloneFrom(Culture from) {
		data.cloneFrom(from.getCultureData());
	}

	DataCulture getCultureData() {
		return data;
	}

	public void mergeFrom(Culture from, double ownFraction) {
		// double avg = (ownFraction * scienceType.getAmount() +
		// fromScienceType.getAmount() * (1 - ownFraction));
		ownFraction = Math.sqrt(ownFraction);
		if (ownFraction < 0.99) {
			data.setRed(merge(data.getRed(), from.getCultureData().getRed(), ownFraction));
			data.setGreen(merge(data.getGreen(), from.getCultureData().getGreen(), ownFraction));
			data.setBlue(merge(data.getBlue(), from.getCultureData().getBlue(), ownFraction));
		}
	}
	
	private int merge(int to, int from, double ownFraction) {
		return (int) Math.round((to*ownFraction + from*(1-ownFraction)));
	}

}
