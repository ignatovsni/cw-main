package com.cwsni.world.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "map" })
public class Game {

	final static String CURRENT_VERSION = "0.1";

	private String version = CURRENT_VERSION;
	private WorldMap map;
	
	@JsonIgnore
	private GameStats gameStats;

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
	
	public GameStats getGameStats() {
		return gameStats;
	}

	public void setGameStats(GameStats gameStats) {
		this.gameStats = gameStats;
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
	
	private void calcGameStats() {
		GameStats stats = new GameStats();
		getMap().getProvinces().forEach(p -> {
			int pop = p.getPopulationAmount();
			if (stats.getMaxPopulationInProvince() < pop) {
				stats.setMaxPopulationInProvince(pop);
			}
			stats.setTotalPopulation(stats.getTotalPopulation() + pop);
		});		
		setGameStats(stats);
	}

	public void postConstruct() {
		calcGameStats();
	}

}
