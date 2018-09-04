package com.cwsni.world.model.engine;

import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.data.DataCulture;

public class Culture {

	private DataCulture data;

	public Culture() {
		data = new DataCulture();
	}

	public Culture(DataCulture culture) {
		this.data = culture;
	}

	public double getRed() {
		return data.getRed();
	}

	public double getGreen() {
		return data.getGreen();
	}

	public double getBlue() {
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

	private double merge(double to, double from, double ownFraction) {
		return (to * ownFraction + from * (1 - ownFraction));
	}

	private void influenceOfCountry(Province p, Game game) {
		Country c = p.getCountry();
		if (c == null || c.getCapitalId() == null) {
			return;
		}

		double perWeek = Math.log10(c.getCapital().getScienceAdministration() + 1) / 100000;
		double ownFraction = 0.9999 - Math.min(0.0001, game.getTurn().addPerWeek(perWeek)) * p.getGovernmentInfluence();
		Color color = c.getColor();
		data.setRed(merge(data.getRed(), color.getR(), ownFraction));
		data.setGreen(merge(data.getGreen(), color.getG(), ownFraction));
		data.setBlue(merge(data.getBlue(), color.getB(), ownFraction));
	}

	// --------------- static section -----------------------

	public static void influenceOfCountryFor(Province p, Game game) {
		p.getPopulation().forEach(pop -> pop.getCulture().influenceOfCountry(p, game));
	}

}
