package com.cwsni.world.model;

import java.util.List;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.data.DataGame;
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
	private CountryCollection countries;

	public List<Event> getEvents() {
		return events.getEvents();
	}

	public int nextEventId() {
		return data.nextEventId();
	}

	public int nextCountryId() {
		return data.nextCountryId();
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
		data.getEvents().add(e);
		events.addEvent(e);
	}

	@Override
	public void removeEvent(Event e) {
		getMap().remove(e);
		data.getEvents().remove(e);
		events.removeEvent(e);
	}

	public Event findEventById(Integer id) {
		return events.getEventById(id);
	}

	public boolean hasEventWithType(String type) {
		return events.hasEventWithType(type);
	}

	public List<Country> getCountries() {
		return countries.getCountries();
	}

	public void addCountry(Country c) {
		data.getCountries().add(c.getCountryData());
		countries.addCountry(c);
	}

	public void removeCountry(Country c) {
		data.getCountries().remove(c.getCountryData());
		countries.removeCountry(c);
	}

	public Country findCountryById(Integer countryId) {
		return countries.findCountryById(countryId);
	}

	private void calcGameStats() {
		GameTransientStats stats = new GameTransientStats(this);
		setGameTransientStats(stats);
		if (stats.getPopulationTotal() > getGameStats().getMaxPopulationForAllTime()) {
			getGameStats().setMaxPopulationForAllTime(stats.getPopulationTotal());
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
		processNewProbablyCountries();
		calcGameStats();
	}

	private void processNewProbablyCountries() {
		map.getProvinces().stream()
				.filter(p -> p.getCountry() == null
						&& p.getPopulationAmount() > getGameParams().getNewCountryPopulationMin()
						&& p.getScienceAdministration() > getGameParams().getNewCountryScienceAdministrationMin())
				.forEach(p -> {
					if (getGameParams().getRandom().nextDouble() <= getGameParams().getNewCountryProbability()) {
						Country.createNewCountry(this, p);
					}
				});
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

		events = new EventCollection();
		events.buildFrom(this, data.getEvents());

		map = new WorldMap();
		map.buildFrom(this, data.getMap());

		countries = new CountryCollection();
		countries.buildFrom(this, data.getCountries());

		calcGameStats();
	}

	DataGame getGameData() {
		return data;
	}

	public Object getSaveData() {
		return data;
	}

}
