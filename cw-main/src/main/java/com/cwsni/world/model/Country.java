package com.cwsni.world.model;

import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.data.DataCountry;
import com.cwsni.world.util.CwRandom;

public class Country {

	private DataCountry data;

	public void buildFrom(DataCountry dc) {
		this.data = dc;
	}

	public int getId() {
		return data.getId();
	}

	public String getName() {
		return data.getName();
	}

	public Color getColor() {
		return data.getColor();
	}

	DataCountry getCountryData() {
		return data;
	}

	public static void createNewCountry(Game game, Province p) {
		if (p.getCountry() != null) {
			return;
		}
		DataCountry dc = new DataCountry();
		dc.setId(game.nextCountryId());
		dc.setName(String.valueOf(dc.getId()));
		dc.setColor(createNewColorForCountry(game));
		Country c = new Country();
		c.buildFrom(dc);
		p.setCountry(c);
		game.addCountry(c);
	}

	private static Color createNewColorForCountry(Game game) {
		CwRandom random = game.getGameParams().getRandom();
		int minValue = 50;
		int minDiff = 50;
		Color color = null;
		while (color == null) {
			int r = minValue + random.nextInt(255 - minValue);
			int g = minValue + random.nextInt(255 - minValue);
			int b = minValue + random.nextInt(255 - minValue);
			// TODO match with colors of others countries
			if (Math.abs(r - g) >= minDiff && Math.abs(r - b) >= minDiff && Math.abs(b - g) >= minDiff) {
				color = new Color(r, g, b);
			}
		}
		return color;
	}

}
