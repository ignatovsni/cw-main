package com.cwsni.world.model;

import java.util.List;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.GameStats;
import com.cwsni.world.model.data.Turn;
import com.cwsni.world.model.events.Event;
import com.cwsni.world.model.events.EventCollection;
import com.cwsni.world.model.events.EventTarget;

public class Game implements EventTarget {

	private DataGame data;
	private GameTransientStats gameTransientStats;
	private WorldMap map;
	private EventCollection events;

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
		events.add(e);
	}

	@Override
	public void removeEvent(Event e) {
		getMap().remove(e);
		getEvents().remove(e);
		events.removeEvent(e);
	}

	public Event findEventById(Integer id) {
		return events.getEventById(id);
	}

	public boolean hasEventWithType(String type) {
		return events.hasEventWithType(type);
	}

	private void calcGameStats() {
		GameTransientStats stats = new GameTransientStats(this);
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
		map.getProvinces().forEach(p -> p.processImmigrantsAndMergePops());
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
		events = new EventCollection();
		// fake province to be able to use EventCollection
		events.setDataProvince(new DataProvince());
		data.getEvents().forEach(e -> {
			registerEventByIdAndType(e);
		});
		map.buildFrom(this, data.getMap());
		calcGameStats();
	}

	public DataGame getSaveData() {
		return data;
	}

}
