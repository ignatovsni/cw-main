package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.events.Event;
import com.cwsni.world.model.events.EventTarget;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "turn", "gameParams", "gameStats", "map" })
public class Game implements EventTarget {

	final static String CURRENT_VERSION = "0.1";

	private String version = CURRENT_VERSION;
	private GameParams gameParams;
	private WorldMap map;
	private Turn turn;
	private List<Event> events = new ArrayList<>();
	private List<Event> oldEvents = new ArrayList<>();
	private int lastEventId;

	private Map<Integer, Event> eventsById = new HashMap<>();
	private Map<String, List<Event>> eventsByType = new HashMap<>();

	private GameStats gameStats = new GameStats();

	@JsonIgnore
	private transient GameTransientStats gameTransientStats;

	@JsonIgnore
	private transient Map<String, Object> tempParams = new HashMap<>();

	public Game() {
	}

	public Game(GameParams gameParams) {
		this.gameParams = gameParams;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public void setLastEventId(int lastEventId) {
		this.lastEventId = lastEventId;
	}

	public int getLastEventId() {
		return lastEventId;
	}

	public int nextEventId() {
		return ++lastEventId;
	}

	public List<Event> getOldEvents() {
		return oldEvents;
	}

	public void setOldEvents(List<Event> oldEvents) {
		this.oldEvents = oldEvents;
	}

	public Map<String, Object> getTempParams() {
		return tempParams;
	}

	public WorldMap getMap() {
		return map;
	}

	public void setMap(WorldMap map) {
		this.map = map;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public GameTransientStats getGameTransientStats() {
		return gameTransientStats;
	}

	public void setGameTransientStats(GameTransientStats gameStats) {
		this.gameTransientStats = gameStats;
	}

	public GameParams getGameParams() {
		return gameParams;
	}

	public void setGameParams(GameParams gameParams) {
		this.gameParams = gameParams;
	}

	public Turn getTurn() {
		return turn;
	}

	public void setTurn(Turn turn) {
		this.turn = turn;
	}

	@JsonIgnore
	public boolean isCorrect() {
		return true;
	}

	/**
	 * If we need to clean something
	 */
	public void destroy() {
	}

	public String logDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("version=" + version);
		return sb.toString();
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
		// too many
		// getOldEvents().add(e);
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
				double soilFertility = p.getSoilFertilityEff();
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

	/**
	 * Finishes game preparing after generation
	 */
	public void postGenerate() {
		map.postGenerate(this);
		map.checkCorrect();
		calcGameStats();
	}

	/**
	 * Finishes game preparing after loading
	 */
	public void postLoad() {
		eventsById.clear();
		getEvents().forEach(e -> {
			registerEventByIdAndType(e);
		});
		map.postLoad(this);
		map.checkCorrect();
		calcGameStats();
	}

	public void processNewTurn(LocaleMessageSource messageSource) {
		getTurn().increment();
		map.getProvinces().forEach(p -> p.processNewTurn());
		Event.processEvents(this, messageSource);
		calcGameStats();
	}

	public GameStats getGameStats() {
		return gameStats;
	}

	public void setGameStats(GameStats gameStats) {
		this.gameStats = gameStats;
	}

}
