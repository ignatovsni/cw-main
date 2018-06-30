package com.cwsni.world.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	private LocaleMessageSource messageSource;
	private Map<Integer, Army> armies;

	public List<Event> getEvents() {
		return events.getEvents();
	}

	public int nextEventId() {
		return data.nextEventId();
	}

	public int nextCountryId() {
		return data.nextCountryId();
	}

	public int nextArmyId() {
		return data.nextArmyId();
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

	public void registerCountry(Country c) {
		data.getCountries().add(c.getCountryData());
		countries.addCountry(c);
	}

	public void unregisterCountry(Country c) {
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

	public void processNewTurn() {
		getTurn().increment();
		armies.values().forEach(a -> a.processNewTurn());
		processNewProbablyCountries();
		dismissEmptyCountries();
		map.getProvinces().forEach(p -> p.processNewTurn());
		Event.processEvents(this, messageSource);
		map.getProvinces().forEach(p -> p.processImmigrantsAndMergePops());
		map.refreshCountriesBorders();
		calcGameStats();
	}

	private void dismissEmptyCountries() {
		List<Country> countryList = new LinkedList<>(countries.getCountries());
		countryList.forEach(c -> {
			if (c.getProvinces().isEmpty()) {
				c.dismiss();
				unregisterCountry(c);
			}
		});
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

	public void buildFrom(DataGame dataGame, LocaleMessageSource messageSource) {
		this.data = dataGame;
		this.messageSource = messageSource;

		armies = new HashMap<>();

		events = new EventCollection();
		events.buildFrom(this, data.getEvents());

		map = new WorldMap();
		map.buildFrom(this, data.getMap());

		countries = new CountryCollection();
		countries.buildFrom(this, data.getCountries());

		countries.getCountries().forEach(c -> {
			c.getArmies().forEach(a -> {
				a.getLocation().addArmy(a);
			});
		});

		map.refreshCountriesBorders();
		calcGameStats();
	}

	DataGame getGameData() {
		return data;
	}

	public Object getSaveData() {
		return data;
	}

	public void registerArmy(Army a) {
		armies.put(a.getId(), a);
	}

	public void unregisterArmy(Army a) {
		armies.remove(a.getId());
	}

	public Army findArmyById(Integer id) {
		return armies.get(id);
	}

}
