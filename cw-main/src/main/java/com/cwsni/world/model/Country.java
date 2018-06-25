package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.data.DataArmy;
import com.cwsni.world.model.data.DataCountry;
import com.cwsni.world.util.CwRandom;

public class Country {

	private DataCountry data;
	private Game game;
	private List<Army> armies;

	public void buildFrom(Game game, DataCountry dc) {
		this.game = game;
		this.data = dc;
		armies = new ArrayList<>();
		dc.getArmies().forEach(da -> {
			Army a = new Army();
			a.buildFrom(this, da);
			armies.add(a);
		});
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

	public Game getGame() {
		return game;
	}

	DataCountry getCountryData() {
		return data;
	}

	public List<Army> getArmies() {
		return Collections.unmodifiableList(armies);
	}

	private void addArmy(Army a) {
		armies.add(a);
		data.getArmies().add(a.getDataArmy());
	}

	// --------------------- static -------------------------------

	public static void createNewCountry(Game game, Province p) {
		if (p.getCountry() != null) {
			return;
		}
		DataCountry dc = new DataCountry();
		dc.setId(game.nextCountryId());
		dc.setName(String.valueOf(dc.getId()));
		dc.setColor(createNewColorForCountry(game));
		Country c = new Country();
		c.buildFrom(game, dc);
		p.setCountry(c);
		game.addCountry(c);

		// test army
		Army a = new Army();
		a.buildFrom(c, new DataArmy());
		a.setSoldiers(1000);
		a.setEquipment(1);
		a.setOrganisation(2);
		a.setTraining(3);
		c.addArmy(a);
		a.setProvince(p);
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
