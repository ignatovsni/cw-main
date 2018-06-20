package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.model.data.DataWorldMap;
import com.cwsni.world.model.events.Event;

public class WorldMap {

	private List<Province> provinces;
	private Map<Integer, Province> mapProvById;
	private Game game;

	public List<Province> getProvinces() {
		return provinces;
	}

	public Province findProvById(Integer id) {
		return mapProvById.get(id);
	}

	Game getGame() {
		return game;
	}

	public void remove(Event e) {
		getProvinces().forEach(p -> p.removeEvent(e));
	}

	public void buildFrom(Game game, DataWorldMap map) {
		this.game = game;
		provinces = new ArrayList<>(map.getProvinces().size());
		mapProvById = new HashMap<>(map.getProvinces().size());
		map.getProvinces().stream().forEach(dp -> {
			Province p = new Province();
			provinces.add(p);
			mapProvById.put(dp.getId(), p);
		});
		map.getProvinces().stream().forEach(dp -> {
			// second step because we need full initialized mapProvById
			getProvinces().forEach(p -> p.buildFrom(this, dp));
		});

	}

}
