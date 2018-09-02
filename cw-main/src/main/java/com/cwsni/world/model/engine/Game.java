package com.cwsni.world.model.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.GameStats;
import com.cwsni.world.model.data.Turn;
import com.cwsni.world.model.data.events.Event;
import com.cwsni.world.model.data.events.EventCollection;
import com.cwsni.world.model.data.events.EventTarget;
import com.cwsni.world.model.engine.relationships.RelationshipsCollection;
import com.cwsni.world.services.GameEventListener;
import com.cwsni.world.util.ComparisonTool;
import com.cwsni.world.util.CwException;

public class Game implements EventTarget {

	private DataGame data;
	private GameTransientStats gameTransientStats;
	private WorldMap map;
	private History history;
	private EventCollection events;
	private CountryCollection countries;
	private StateCollection states;
	private Map<Integer, Army> armies;
	private Map<Integer, Map<Integer, Army>> newArmiesWithIdLessThanZero;
	private RelationshipsCollection relationships;

	private LocaleMessageSource messageSource;
	private GameEventListener gameEventListener;
	private int lastAutoSaveTurn;

	public List<Event> getEvents() {
		return events.getEvents();
	}

	public int nextEventId() {
		return data.nextEventId();
	}

	public int nextCountryId() {
		return data.nextCountryId();
	}

	public int nextStateId() {
		return data.nextStateId();
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

	public RelationshipsCollection getRelationships() {
		return relationships;
	}

	public GameParams getGameParams() {
		return data.getGameParams();
	}

	public Turn getTurn() {
		return data.getTurn();
	}

	public History getHistory() {
		return history;
	}

	public String logDescription() {
		return data.logDescription();
	}

	public GameEventListener getGameEventListener() {
		return gameEventListener;
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
		if (countries.findCountryById(c.getId()) != null) {
			throw new CwException("country is already registered, id = " + c.getId() + "; " + c);
		}
		data.getCountries().add(c.getCountryData());
		countries.addCountry(c);
	}

	public void unregisterCountry(Country c) {
		data.getCountries().remove(c.getCountryData());
		countries.removeCountry(c);
		relationships.unregisterCountry(c);
	}

	public Country findCountryById(Integer countryId) {
		return countries.findCountryById(countryId);
	}

	public List<State> getStates() {
		return states.getStates();
	}

	public void registerState(State c) {
		data.getStates().add(c.getStateData());
		states.addState(c);
	}

	public void unregisterState(State c) {
		data.getStates().remove(c.getStateData());
		states.removeState(c);
	}

	public State findStateById(Integer stateId) {
		return states.findStateById(stateId);
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
		processFights();
		dismissEmptyArmies();
		armies.values().forEach(a -> a.processNewTurn());
		processNewProbablyCountries();
		dismissEmptyCountries();
		processNewProbablyStates();
		map.getProvinces().forEach(p -> p.processNewTurn());
		Event.processEvents(this, messageSource);
		map.getProvinces().forEach(p -> p.processImmigrantsAndMergePops());
		countries.getCountries().forEach(c -> c.calculateBaseBudget());
		countries.getCountries().forEach(c -> c.calculateBudgetWithAgreements());
		countries.getCountries().forEach(c -> c.processNewTurn());
		processStates();
		relationships.processNewTurn();
		history.processNewTurn();
		calcGameStats();
	}

	private void processStates() {
		states.getStates().forEach(c -> c.processNewTurn());
		states.getStates().forEach(c -> c.resetFlagRevoltSuccessfulThisTurn());
		states.getStates().forEach(c -> c.processRebels());
	}

	private void processFights() {
		GameParams gParams = map.getGame().getGameParams();
		for (Province p : map.getProvinces()) {
			Map<Integer, List<Army>> armies = p.getArmies().stream().filter(a -> a.isCanFightThisTurn())
					.collect(Collectors.groupingBy(a -> a.getCountry().getId()));
			while (armies.keySet().size() > 1) {
				List<Integer> countryIds = new ArrayList<>(armies.keySet());
				int attacker = gParams.getRandom().nextInt(countryIds.size());
				int defender = -1;
				while (defender == -1) {
					int d = gParams.getRandom().nextInt(countryIds.size());
					if (d != attacker) {
						defender = d;
					}
				}
				if (ComparisonTool.isEqual(countryIds.get(attacker), p.getCountryId())) {
					// province owner is always a defender
					int temp = attacker;
					attacker = defender;
					defender = temp;
				}
				Integer countryAttacker = countryIds.get(attacker);
				Integer countryDefender = countryIds.get(defender);
				List<Army> attackerArmies = armies.get(countryAttacker);
				List<Army> defenderArmies = armies.get(countryDefender);
				p.sufferFromFight(attackerArmies, defenderArmies);
				double result = Army.fight(attackerArmies, defenderArmies);
				if (result > 1) {
					armies.remove(countryDefender);
				} else {
					armies.remove(countryAttacker);
				}
			}
		}
	}

	private void dismissEmptyCountries() {
		List<Country> countryList = new ArrayList<>(countries.getCountries());
		countryList.forEach(c -> {
			if ((c.getProvinces().isEmpty() || c.getProvinces().stream().mapToLong(p -> p.getPopulationAmount())
					.sum() <= getGameParams().getNewCountryPopulationMin() / 2)
					&& (c.getArmies().isEmpty()
							|| c.getArmiesSoldiers() < getGameParams().getNewCountryPopulationMin())) {
				c.dismiss();
				unregisterCountry(c);
				history.removeCountry(c);
			}
		});
	}

	private void dismissEmptyArmies() {
		countries.getCountries().forEach(c -> {
			List<Army> armiesList = new ArrayList<>(c.getArmies());
			armiesList.stream().filter(a -> !a.isAbleToWork()).forEach(a -> c.dismissArmy(a));
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

	private void processNewProbablyStates() {
		for (Country country : getCountries()) {
			State.createOrGrowthStates(country);
		}
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

	public void buildFrom(DataGame dataGame, LocaleMessageSource messageSource, GameEventListener gameEventListener) {
		this.data = dataGame;
		this.messageSource = messageSource;
		this.gameEventListener = gameEventListener;

		armies = new HashMap<>();
		newArmiesWithIdLessThanZero = new HashMap<>();

		events = new EventCollection();
		events.buildFrom(this, data.getEvents());

		map = new WorldMap();
		map.buildFrom(this, data.getMap());

		relationships = new RelationshipsCollection();
		relationships.buildFrom(this, data.getRelationships());

		countries = new CountryCollection();
		countries.buildFrom(this, data.getCountries());

		countries.getCountries().forEach(c -> {
			c.getArmies().forEach(a -> {
				a.getLocation().addArmy(a);
			});
		});

		states = new StateCollection();
		states.buildFrom(this, data.getStates());

		history = new History();
		history.buildFrom(this, data.getHistory());

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

	/**
	 * Client side creates new armies with id < 0.
	 */
	public Army findArmyByIdForCommand(Integer countryId, Integer armyId) {
		if (armyId >= 0) {
			return findArmyById(armyId);
		} else {
			Map<Integer, Army> idsForCountry = newArmiesWithIdLessThanZero.get(countryId);
			if (idsForCountry != null) {
				return idsForCountry.get(armyId);
			} else {
				return null;
			}
		}
	}

	public void registerNewArmyWithIdLessThanZero(int countryId, int armyId, Army army) {
		if (armyId >= 0) {
			throw new CwException("armyId for new army must be < 0");
		}
		Map<Integer, Army> idsForCountry = newArmiesWithIdLessThanZero.get(countryId);
		if (idsForCountry == null) {
			idsForCountry = new HashMap<>();
			newArmiesWithIdLessThanZero.put(countryId, idsForCountry);
		}
		idsForCountry.put(armyId, army);
	}

	public void resetNewArmiesWithIdLessThanZero() {
		newArmiesWithIdLessThanZero.clear();
	}

	public int getLastAutoSaveTurn() {
		return lastAutoSaveTurn;
	}

	public void setLastAutoSaveTurn(int lastAutoSaveTurn) {
		this.lastAutoSaveTurn = lastAutoSaveTurn;
	}

}
