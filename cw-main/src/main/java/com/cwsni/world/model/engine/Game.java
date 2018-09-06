package com.cwsni.world.model.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.GameStats;
import com.cwsni.world.model.engine.relationships.RelationshipsCollection;
import com.cwsni.world.services.PlayerEventListener;
import com.cwsni.world.util.ComparisonTool;
import com.cwsni.world.util.CwBaseRandom;
import com.cwsni.world.util.CwException;

public class Game {

	private DataGame data;
	private Turn turn;
	private GameTransientStats gameTransientStats;
	private WorldMap map;
	private History history;
	private EventCollection eventsCollection;
	private CountryCollection countries;
	private StateCollection states;
	private Map<Long, Army> armies;
	private Map<Integer, Map<Long, Army>> newArmiesWithIdLessThanZero;
	private RelationshipsCollection relationships;

	private PlayerEventListener gameEventListener;
	private int lastAutoSaveTurn;

	public int nextEventId() {
		return data.nextEventId();
	}

	public int nextCountryId() {
		return data.nextCountryId();
	}

	public int nextStateId() {
		return data.nextStateId();
	}

	public long nextArmyId() {
		return data.nextArmyId();
	}
	
	public long nextPopulationId() {
		return data.nextPopulationId();
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
		return turn;
	}

	public History getHistory() {
		return history;
	}

	public String logDescription() {
		return data.logDescription();
	}

	public PlayerEventListener getGameEventListener() {
		return gameEventListener;
	}

	public boolean hasEventWithType(String type) {
		return eventsCollection.hasEventWithType(type);
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

	public void processNewTurnBeforeEvents() {
		getTurn().increment();
		processFights();
		dismissEmptyArmies();
		armies.values().forEach(a -> a.processNewTurn());
		processNewProbablyCountries();
		dismissEmptyCountries();
		processNewProbablyStates();
		map.getProvinces().forEach(p -> p.processNewTurn());
	}

	public void processNewTurnAfterEvents() {
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
		double probability = getTurn().probablilityPerWeek(getGameParams().getNewCountryProbabilityPerWeek());
		map.getProvinces().stream()
				.filter(p -> p.getCountry() == null
						&& p.getPopulationAmount() > getGameParams().getNewCountryPopulationMin()
						&& getScienceModificators().isNewCountryPossible(p))
				.forEach(p -> {
					if (getGameParams().getRandom().nextDouble() <= probability) {
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

	public void buildFrom(DataGame dataGame, PlayerEventListener gameEventListener) {
		this.data = dataGame;
		this.gameEventListener = gameEventListener;

		turn = new Turn();
		turn.buildFrom(this, data.getTurn());

		armies = new HashMap<>();
		newArmiesWithIdLessThanZero = new HashMap<>();

		eventsCollection = new EventCollection();
		eventsCollection.buildFrom(this, data.getEvents());

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

	public Army findArmyById(Long id) {
		return armies.get(id);
	}

	/**
	 * Client side creates new armies with id < 0.
	 */
	public Army findArmyByIdForCommand(Integer countryId, Long armyId) {
		if (armyId >= 0) {
			return findArmyById(armyId);
		} else {
			Map<Long, Army> idsForCountry = newArmiesWithIdLessThanZero.get(countryId);
			if (idsForCountry != null) {
				return idsForCountry.get(armyId);
			} else {
				return null;
			}
		}
	}

	public void registerNewArmyWithIdLessThanZero(int countryId, long armyId, Army army) {
		if (armyId >= 0) {
			throw new CwException("armyId for new army must be < 0");
		}
		Map<Long, Army> idsForCountry = newArmiesWithIdLessThanZero.get(countryId);
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

	public CwBaseRandom getRandomForCurrentTurn(Long externalSeed) {
		long seed = (long) (getTurn().getDateTurn() + 1) * (getCountries().size() + 1)
				+ (externalSeed != null ? externalSeed : 1);
		return new CwBaseRandom(seed);
	}

	public CwBaseRandom getRandomForCurrentTurn(int externalSeed) {
		return getRandomForCurrentTurn((long) externalSeed);
	}

	public EventCollection getEventsCollection() {
		return eventsCollection;
	}

	public ScienceModificators getScienceModificators() {
		return new ScienceModificators(this);
	}

}
