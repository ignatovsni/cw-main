package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.GameStats;
import com.cwsni.world.model.data.Turn;
import com.cwsni.world.model.events.Event;
import com.cwsni.world.model.events.EventTarget;

public class Game implements EventTarget {

	private DataGame data;
	private Map<Integer, Event> eventsById;
	private Map<String, List<Event>> eventsByType;
	private GameTransientStats gameTransientStats;
	private WorldMap map;

	public List<Event> getEvents() {
		return data.getEvents();
	}

	public void setLastEventId(int lastEventId) {
		data.setLastEventId(lastEventId);
	}

	public int getLastEventId() {
		return data.getLastEventId();
	}

	public int nextEventId() {
		return data.nextEventId();
	}

	public WorldMap getMap() {
		return map;
	}

	public void setMap(WorldMap map) {
		this.map = map;
	}

	public GameTransientStats getGameTransientStats() {
		return gameTransientStats;
	}

	public GameParams getGameParams() {
		return data.getGameParams();
	}

	public Turn getTurn() {
		return data.getTurn();
	}

	public String logDescription() {
		return data.logDescription();
	}

	@Override
	public void addEvent(Event e) {
		getEvents().add(e);
		registerEventByIdAndType(e);
	}

	private void registerEventByIdAndType(Event e) {
		eventsById.put(e.getId(), e);
		List<Event> listByType = eventsByType.get(e.getType());
		if (listByType == null) {
			listByType = new ArrayList<>(1);
			eventsByType.put(e.getType(), listByType);
		}
		listByType.add(e);
	}

	@Override
	public void removeEvent(Event e) {
		getMap().remove(e);
		getEvents().remove(e);
		eventsById.remove(e.getId());
		List<Event> listByType = eventsByType.get(e.getType());
		if (listByType != null) {
			listByType.remove(e);
		}
	}

	public Event findEventById(Integer id) {
		return eventsById.get(id);
	}

	public boolean hasEventWithType(String type) {
		List<Event> listByType = eventsByType.get(type);
		return !(listByType == null || listByType.isEmpty());
	}

	private void calcGameStats() {
		GameTransientStats stats = new GameTransientStats();
		getMap().getProvinces().forEach(p -> {
			if (p.getTerrainType().isPopulationPossible()) {
				// max population
				int pop = p.getPopulationAmount();
				if (stats.getMaxPopulationInProvince() < pop) {
					stats.setMaxPopulationInProvince(pop);
				}
				// total population
				stats.setTotalPopulation(stats.getTotalPopulation() + pop);
			}
			if (p.getTerrainType().isSoilPossible()) {
				// max soil quality
				int soilQuality = p.getSoilQuality();
				if (stats.getMaxSoilQuality() < soilQuality) {
					stats.setMaxSoilQuality(soilQuality);
				}
				// max soil fertility
				double soilFertility = p.getSoilFertility();
				if (stats.getMaxSoilFertility() < soilFertility) {
					stats.setMaxSoilFertility(soilFertility);
				}
				// min soil fertility
				stats.setMinSoilFertility(stats.getMaxSoilFertility());
				if (stats.getMinSoilFertility() > soilFertility) {
					stats.setMinSoilFertility(soilFertility);
				}
			}
		});
		setGameTransientStats(stats);
		if (stats.getTotalPopulation() > getGameStats().getMaxPopulationForAllTime()) {
			getGameStats().setMaxPopulationForAllTime(stats.getTotalPopulation());
		}

	}

	private void setGameTransientStats(GameTransientStats stats) {
		this.gameTransientStats = stats;
	}

	public void processNewTurn(LocaleMessageSource messageSource) {
		getTurn().increment();
		map.getProvinces().forEach(p -> p.processNewTurn());
		Event.processEvents(this, messageSource);
		calcGameStats();
	}

	public GameStats getGameStats() {
		return data.getGameStats();
	}

	public void setGameStats(GameStats gameStats) {
		data.setGameStats(gameStats);
	}

	public void setTurn(Turn turn) {
		data.setTurn(turn);
	}

	public void buildFrom(DataGame dataGame) {
		this.data = dataGame;
		map = new WorldMap();
		map.buildFrom(this, data.getMap());
		eventsById = new HashMap<>();
		eventsByType = new HashMap<>();
		data.getEvents().forEach(e -> {
			registerEventByIdAndType(e);
		});
		calcGameStats();
	}

	public DataGame getSaveData() {
		return data;
	}

}
