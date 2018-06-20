package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.model.events.Event;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "turn", "gameParams", "gameStats", "map" })
public class DataGame {

	final static String CURRENT_VERSION = "0.1";

	private String version = CURRENT_VERSION;
	private GameParams gameParams;
	private DataWorldMap map;
	private Turn turn;
	private List<Event> events = new ArrayList<>();
	private int lastEventId;

	private GameStats gameStats = new GameStats();

	public DataGame() {}

	public DataGame(GameParams gameParams) {
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

	public Turn getTurn() {
		return turn;
	}

	public void setTurn(Turn turn) {
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

}
