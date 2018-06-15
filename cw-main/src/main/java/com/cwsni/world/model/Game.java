package com.cwsni.world.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "map" })
public class Game {

	final static String CURRENT_VERSION = "0.1";

	private String version = CURRENT_VERSION;
	private WorldMap map;

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

	public static Game createTestGame(int rows, int columns, int provinceRadius) {
		Game game = new Game();
		WorldMap map = WorldMap.createMap(rows, columns, provinceRadius);
		game.setMap(map);
		return game;
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

}
