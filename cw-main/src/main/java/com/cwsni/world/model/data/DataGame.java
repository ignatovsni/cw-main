package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.model.data.relationships.DataRelationshipsCollection;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "turn", "gameParams", "gameStats", "countries", "states", "map" })
public class DataGame {

	final static String CURRENT_VERSION = "0.1";

	private String version = CURRENT_VERSION;
	private GameParams gameParams;
	private DataWorldMap map;
	private DataTurn turn;
	private List<DataCountry> countries = new ArrayList<>();
	private HistoryData history = new HistoryData();
	private List<DataState> states = new ArrayList<>();
	private List<DataEvent> events = new ArrayList<>();
	private DataRelationshipsCollection relationships = new DataRelationshipsCollection();
	private int lastCountryId;
	private int lastStateId;
	private int lastEventId;
	private long lastArmyId;
	private long lastPopulationId;

	private GameStats gameStats = new GameStats();

	public DataGame() {
	}

	public DataGame(GameParams gameParams) {
		this.gameParams = gameParams;
	}

	public List<DataEvent> getEvents() {
		return events;
	}

	public void setEvents(List<DataEvent> events) {
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

	public DataWorldMap getMap() {
		return map;
	}

	public void setMap(DataWorldMap map) {
		this.map = map;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public GameParams getGameParams() {
		return gameParams;
	}

	public void setGameParams(GameParams gameParams) {
		this.gameParams = gameParams;
	}

	public DataTurn getTurn() {
		return turn;
	}

	public void setTurn(DataTurn turn) {
		this.turn = turn;
	}

	public String logDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("version=" + version);
		return sb.toString();
	}

	public GameStats getGameStats() {
		return gameStats;
	}

	public void setGameStats(GameStats gameStats) {
		this.gameStats = gameStats;
	}

	public List<DataCountry> getCountries() {
		return countries;
	}

	public void setCountries(List<DataCountry> countries) {
		this.countries = countries;
	}

	public int getLastCountryId() {
		return lastCountryId;
	}

	public void setLastCountryId(int lastCountryId) {
		this.lastCountryId = lastCountryId;
	}

	public int nextCountryId() {
		return ++lastCountryId;
	}

	public long getLastArmyId() {
		return lastArmyId;
	}

	public void setLastArmyId(long lastArmyId) {
		this.lastArmyId = lastArmyId;
	}

	public long nextArmyId() {
		return ++lastArmyId;
	}

	public long nextPopulationId() {
		return ++lastPopulationId;
	}

	public int getLastStateId() {
		return lastStateId;
	}

	public void setLastStateId(int lastStateId) {
		this.lastStateId = lastStateId;
	}

	public int nextStateId() {
		return ++lastStateId;
	}

	public List<DataState> getStates() {
		return states;
	}

	public void setStates(List<DataState> states) {
		this.states = states;
	}

	public HistoryData getHistory() {
		return history;
	}

	public void setHistory(HistoryData history) {
		this.history = history;
	}

	public DataRelationshipsCollection getRelationships() {
		return relationships;
	}

	public void setRelationships(DataRelationshipsCollection relationships) {
		this.relationships = relationships;
	}

	public long getLastPopulationId() {
		return lastPopulationId;
	}

	public void setLastPopulationId(long lastPopulationId) {
		this.lastPopulationId = lastPopulationId;
	}

}
