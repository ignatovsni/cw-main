package com.cwsni.world.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "gameParams", "map" })
public class Game {

	final static String CURRENT_VERSION = "0.1";

	private String version = CURRENT_VERSION;

	private GameParams gameParams;

	private WorldMap map;

	@JsonIgnore
	private GameStats gameStats;

	public Game() {
	}

	public Game(GameParams gameParams) {
		this.gameParams = gameParams;
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

	public GameStats getGameStats() {
		return gameStats;
	}

	public void setGameStats(GameStats gameStats) {
		this.gameStats = gameStats;
	}

	public GameParams getGameParams() {
		return gameParams;
	}

	public void setGameParams(GameParams gameParams) {
		this.gameParams = gameParams;
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
			// max population
			int pop = p.getPopulationAmount();
			if (stats.getMaxPopulationInProvince() < pop) {
				stats.setMaxPopulationInProvince(pop);
			}
			// total population
			stats.setTotalPopulation(stats.getTotalPopulation() + pop);
			// max soil quality
			int soilQuality = p.getSoilQuality();
			if (stats.getMaxSoilQuality() < soilQuality) {
				stats.setMaxSoilQuality(soilQuality);
			}
		});
		setGameStats(stats);
	}

	/**
	 * Finishes game preparing after generation
	 */
	public void postGenerate() {
		map.postGenerate();
		map.checkCorrect();
		calcGameStats();
	}

	/**
	 * Finishes game preparing after loading
	 */
	public void postLoad() {
		map.postLoad();
		map.checkCorrect();
		calcGameStats();
	}

}
