package com.cwsni.world.model.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataState;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.util.ComparisonTool;
import com.cwsni.world.util.CwException;
import com.cwsni.world.util.CwRandom;

public class State {

	private DataState data;
	private Game game;
	private Collection<Province> provinces;
	private Set<State> neighbors;
	private boolean isRevoltSuccessfulThisTurn;

	public void buildFrom(Game game, DataState ds) {
		this.game = game;
		this.data = ds;
		this.provinces = new HashSet<>();
		this.neighbors = new HashSet<>();

		data.getProvinces().forEach(pId -> {
			Province province = game.getMap().findProvinceById(pId);
			province.setState(this);
			provinces.add(province);
		});
	}

	public int getId() {
		return data.getId();
	}

	public String getName() {
		return data.getName();
	}

	public Color getColor() {
		return data.getColor();
	}

	public Game getGame() {
		return game;
	}

	public Province getCapital() {
		return game.getMap().findProvinceById(data.getCapital());
	}

	public void setCapital(Province province) {
		if (province == null) {
			data.setCapital(null);
		} else {
			if (this.equals(province.getState())) {
				data.setCapital(province.getId());
			} else {
				throw new CwException("Trying to set up capital in alien province: province state id = "
						+ province.getState() + " but state.id = " + getId());
			}
		}
	}

	public Integer getCapitalId() {
		return data.getCapital();
	}

	public void addProvince(Province p) {
		if (data.getProvinces().contains(p.getId())) {
			return;
		}
		if (p.getTerrainType().isPopulationPossible()) {
			p.setState(this);
			provinces.add(p);
			data.getProvinces().add(p.getId());
			initializeNeighbors();
		}
	}

	public void removeProvince(Province p) {
		if (!data.getProvinces().contains(p.getId())) {
			return;
		}
		p.setState(null);
		provinces.remove(p);
		data.getProvinces().remove(p.getId());
		if (ComparisonTool.isEqual(getCapitalId(), p.getId())) {
			setCapital(null);
		}
		initializeNeighbors();
	}

	public Collection<Province> getProvinces() {
		return Collections.unmodifiableCollection(provinces);
	}

	public Set<State> getNeighbors() {
		return Collections.unmodifiableSet(neighbors);
	}

	void setNeighbors(Set<State> neighbors) {
		this.neighbors = neighbors;
	}

	DataState getStateData() {
		return data;
	}

	@Override
	public int hashCode() {
		return data.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof State)) {
			return false;
		}
		return ((State) obj).getId() == getId();
	}

	public void processNewTurn() {
		processScienceNewTurn();
	}

	public void processRebels() {
		processRebels(null);
	}

	public void resetFlagRevoltSuccessfulThisTurn() {
		isRevoltSuccessfulThisTurn = false;
	}

	private enum TypeOfRebelCountry {
		CREATED, RESTORED, JOINED
	}

	protected Turn getTurn() {
		return game.getTurn();
	}

	private void processRebels(Country revoltAttractionToCountry) {
		if (isRevoltSuccessfulThisTurn) {
			return;
		}
		Province stateCapital = getCapital();
		if (stateCapital == null) {
			return;
		}
		GameParams gParams = game.getGameParams();
		long statePopulation = getPopulationAmount();
		if (statePopulation < gParams.getNewCountryPopulationMin() * 2) {
			return;
		}

		Country originaltCountry = stateCapital.getCountry();
		if (originaltCountry == null) {
			return;
		}
		Collection<Province> stateProvinces = getProvinces();
		Optional<Province> checkCountryCapital = stateProvinces.stream()
				.filter(p -> p.getCountryId() != null && p.equals(p.getCountry().getCapital())).findFirst();
		if (checkCountryCapital.isPresent()) {
			// if a state has country capital, it can not rebel
			return;
		}

		double rebelChanceCoeff = getTurn().probablilityPerYear(gParams.getPopulationLoyaltyRebelChanceCoeff());
		rebelChanceCoeff *= (1 + originaltCountry.getRebelAddChances());
		double loyaltyToOriginalCountry = getLoayltyToCountry(originaltCountry.getId(), statePopulation);
		Set<Integer> countriesIds = getCountriesWithLoyalty();
		if (revoltAttractionToCountry != null) {
			countriesIds.add(revoltAttractionToCountry.getId());
		}
		CwRandom random = gParams.getRandom();
		for (int countryId : countriesIds) {
			if (!ComparisonTool.isEqual(countryId, originaltCountry.getId())) {
				double loyaltyToCountry = getLoayltyToCountry(countryId, statePopulation);
				double diffLoaylty = loyaltyToCountry - loyaltyToOriginalCountry
						- gParams.getPopulationLoyaltyRebelToCountryThreshold();
				if (revoltAttractionToCountry != null
						&& ComparisonTool.isEqual(revoltAttractionToCountry.getId(), countryId)) {
					diffLoaylty += gParams.getPopulationLoyaltyRebelChainAdditionalLoyalty();
					diffLoaylty *= gParams.getPopulationLoyaltyRebelChainProbabilityMultiplicator();
				}
				if (diffLoaylty > 0 && diffLoaylty * rebelChanceCoeff > random.nextDouble()) {
					TypeOfRebelCountry typeC = revoltToCountry(countryId, stateCapital);
					if (typeC != null) {
						isRevoltSuccessfulThisTurn = true;
						originaltCountry.setRebelAddChances(originaltCountry.getRebelAddChances() + 0.01);
						Country rebelCountry = stateCapital.getCountry();
						getNeighbors().forEach(n -> n.processRebels(rebelCountry));
						if (TypeOfRebelCountry.RESTORED.equals(typeC)) {
							rebelCountry.getProvinces()
									.forEach(p -> p.addLoyaltyToCountry(rebelCountry.getId(), p.getLoyaltyToState()));
						}
						game.getRelationships().newWar(originaltCountry.getId(), rebelCountry.getId());
						return;
					}
				}
			}
		}

		double stateLoyalty = getLoayltyToState(statePopulation);
		double diffLoaylty = stateLoyalty - loyaltyToOriginalCountry
				- gParams.getPopulationLoyaltyRebelToStateThreshold();
		if (diffLoaylty > 0 && diffLoaylty * rebelChanceCoeff > random.nextDouble()) {
			TypeOfRebelCountry typeC = revoltToState(stateCapital, random);
			if (typeC != null) {
				isRevoltSuccessfulThisTurn = true;
				originaltCountry.setRebelAddChances(originaltCountry.getRebelAddChances() + 0.01);
				Country rebelCountry = stateCapital.getCountry();
				getNeighbors().forEach(n -> n.processRebels(rebelCountry));
				rebelCountry.getProvinces()
						.forEach(p -> p.addLoyaltyToCountry(rebelCountry.getId(), p.getLoyaltyToState()));
				game.getRelationships().newWar(originaltCountry.getId(), rebelCountry.getId());
				return;
			}
			return;
		}
	}

	private TypeOfRebelCountry revoltToCountry(Integer countryToId, Province stateCapital) {
		Country countryFrom = stateCapital.getCountry();
		Country country2 = game.findCountryById(countryToId);
		if (country2 != null) {
			final Country countryTo = country2;
			getProvinces().stream().filter(p -> ComparisonTool.isEqual(countryFrom.getId(), p.getCountryId()))
					.forEach(p -> countryTo.addProvince(p));
			game.getGameEventListener().event(game, "Rebels provinces join to the country " + countryTo.getName()
					+ " with a state capital " + stateCapital.getName());
			return TypeOfRebelCountry.JOINED;
		} else {
			Country countryTo = Country.restoreCountry(game, game.getHistory().findCountry(countryToId));
			if (countryTo != null) {
				getProvinces().stream().filter(p -> ComparisonTool.isEqual(countryFrom.getId(), p.getCountryId()))
						.forEach(p -> countryTo.addProvince(p));
				countryTo.setCapital(stateCapital);
				countryTo.getMoneyBudget().addMoneyForNewRebelCountry(
						game.getGameParams().getPopulationLoyaltyRebelNewCountriesTakeMoneyForYears());
				game.getGameEventListener().event(game, "Rebels restored the country " + countryTo.getName()
						+ " with a state capital " + stateCapital.getName());
				return TypeOfRebelCountry.RESTORED;
			}
		}
		return null;
	}

	private TypeOfRebelCountry revoltToState(Province stateCapital, CwRandom random) {
		TypeOfRebelCountry typeOfRebelCountry = null;
		Country countryFrom = stateCapital.getCountry();
		Country country2 = null;
		// old disappeared country
		List<Entry<Integer, Integer>> lifeInCountries = new ArrayList<>(getLifeInCountries().entrySet().stream()
				.filter(e -> game.getHistory().containsCountry(e.getKey())).collect(Collectors.toList()));
		long totalYears = lifeInCountries.stream().mapToLong(e -> e.getValue()).sum();
		int countOfIteraion = 0;
		if (totalYears > 0) {
			Collections.sort(lifeInCountries, (x, y) -> y.getValue() - x.getValue());
			while (country2 == null) {
				for (Map.Entry<Integer, Integer> e : lifeInCountries) {
					double nextDouble = random.nextDouble();
					if (1.0 * e.getValue() / totalYears > nextDouble) {
						country2 = Country.restoreCountry(game, game.getHistory().findCountry(e.getKey()));
						if (country2 != null) {
							break;
						}
					}
				}
				if (countOfIteraion++ > 10) {
					break;
				}
			}
		}
		if (country2 != null) {
			typeOfRebelCountry = TypeOfRebelCountry.RESTORED;
			game.getGameEventListener().event(game, "Rebels restored the country " + country2.getName()
					+ " with a state capital " + stateCapital.getName());
		} else {
			country2 = Country.createNewCountryForRebelState(game, stateCapital);
			typeOfRebelCountry = TypeOfRebelCountry.CREATED;
			game.getGameEventListener().event(game, "Rebels created new country " + country2.getName()
					+ " with a state capital " + stateCapital.getName());
		}
		Country countryTo = country2;
		getProvinces().stream().filter(p -> ComparisonTool.isEqual(countryFrom.getId(), p.getCountryId()))
				.forEach(p -> countryTo.addProvince(p));
		countryTo.setCapital(stateCapital);
		countryTo.getMoneyBudget().addMoneyForNewRebelCountry(
				game.getGameParams().getPopulationLoyaltyRebelNewCountriesTakeMoneyForYears());
		return typeOfRebelCountry;
	}

	private double getLoayltyToState(long statePopulation) {
		return getProvinces().stream().mapToDouble(p -> p.getLoyaltyToState() * p.getPopulationAmount()).sum()
				/ statePopulation;
	}

	/**
	 * Please, use {@link State#getLoayltyToState(long)}} if it is possible
	 * (performance reason).
	 */
	public double getLoayltyToState() {
		return getLoayltyToState(getPopulationAmount());
	}

	private double getLoayltyToCountry(int countryId, long statePopulation) {
		return getProvinces().stream().mapToDouble(p -> p.getLoyaltyToCountry(countryId) * p.getPopulationAmount())
				.sum() / statePopulation;
	}

	/**
	 * Please, use {@link State#getLoayltyToCountry(int, long)}} if it is possible
	 * (performance reason).
	 */
	public double getLoayltyToCountry(int countryId) {
		return getLoayltyToCountry(countryId, getPopulationAmount());
	}

	public long getPopulationAmount() {
		return getProvinces().stream().mapToLong(p -> p.getPopulationAmount()).sum();
	}

	Set<Integer> getCountriesWithLoyalty() {
		Set<Integer> countriesIds = new HashSet<>();
		getProvinces().forEach(p -> countriesIds.addAll(p.getCountriesWithLoyalty()));
		return countriesIds;
	}

	private Map<Integer, Integer> getLifeInCountries() {
		Map<Integer, Integer> lifeInCountries = new HashMap<>();
		long statePopulation = getPopulationAmount();
		for (Province p : getProvinces()) {
			int provincePopulation = p.getPopulationAmount();
			Map<Integer, Integer> lInCs = p.getLifeInCountries();
			for (Entry<Integer, Integer> entry : lInCs.entrySet()) {
				Integer years = lifeInCountries.get(entry.getKey());
				if (years == null) {
					years = 0;
				}
				lifeInCountries.put(entry.getKey(),
						(int) (years + 1.0 * entry.getValue() * provincePopulation / statePopulation));
			}
		}
		return lifeInCountries;
	}

	private void processScienceNewTurn() {
		Province capital = getCapital();
		if (capital != null) {
			Country country = capital.getCountry();
			if (country != null) {
				MoneyBudget moneyBudget = country.getMoneyBudget();
				capital.spendMoneyForScience(
						moneyBudget.getAvailableMoneyForScience() * capital.getGovernmentInfluence() / 10);
			}
		}
	}

	public void setName(String name) {
		data.setName(name);
	}

	void initializeNeighbors() {
		neighbors.forEach(n -> n.neighbors.remove(this));
		neighbors.clear();
		for (Province p : getProvinces()) {
			neighbors.addAll(p.getNeighbors().stream().filter(n -> n.getState() != null && !this.equals(n.getState()))
					.map(n -> n.getState()).collect(Collectors.toSet()));
		}
		neighbors.forEach(n -> n.neighbors.add(this));
	}

	public Double getMaxStateLoyalty() {
		Province stateCapital = getCapital();
		if (stateCapital == null || stateCapital.getCountry() == null || stateCapital.getCountry().getCapital() == null
				|| stateCapital.getCountry().getCapital().getState() == null) {
			return null;
		}
		State capitalState = stateCapital.getCountry().getCapital().getState();
		if (this.equals(capitalState)) {
			return null;
		}
		double stateStrength = getPopulationAmount();
		if (stateStrength == 0) {
			return null;
		}
		double countryCapitalInfluence = stateCapital.getCapitalInfluence();
		double capitalStateStrength = capitalState.getPopulationAmount();
		if (countryCapitalInfluence < 0.3) {
			capitalStateStrength *= (countryCapitalInfluence + 0.7);
		}
		if (capitalStateStrength == 0) {
			return null;
		}
		return Math.min(1, stateStrength / capitalStateStrength + 0.2);
	}

	// --------------------- static -------------------------------

	public static void createOrGrowthStates(Country country) {
		GameParams gParams = country.getGame().getGameParams();
		Province capital = country.getCapital();
		int stateCreateWithMinProvinces = gParams.getStateCreateWithMinProvinces();
		if (capital != null && capital.getState() == null
				&& country.getProvinces().size() >= stateCreateWithMinProvinces * 2) {
			// create state for capital
			createNewState(country, capital, stateCreateWithMinProvinces);
		}
		List<Province> provs = country.getProvinces().stream().filter(p -> p.getState() == null)
				.collect(Collectors.toList());
		if (provs.isEmpty()) {
			return;
		}
		Province prov = provs.get(gParams.getRandom().nextInt(provs.size()));
		// create state for random province
		createNewState(country, prov, stateCreateWithMinProvinces);
		// if we can't create state then we try to join province to the nearest state
		if (prov.getState() == null) {
			Province n = prov.getNeighbors().get(gParams.getRandom().nextInt(prov.getNeighbors().size()));
			if (n.getState() != null) {
				n.getState().addProvince(prov);
			}
		}
	}

	private static void createNewState(Country country, Province prov, int minProvinces) {
		if (prov.getState() != null) {
			return;
		}
		Game game = country.getGame();
		List<Province> provs = new ArrayList<>();
		Set<Integer> provsIds = new HashSet<>();
		provs.add(prov);
		provsIds.add(prov.getId());
		for (int i = 0; i < provs.size() && provs.size() < minProvinces; i++) {
			List<Province> suitableNeighbors = provs.get(i).getNeighbors().stream().filter(p -> p.getState() == null
					&& !provsIds.contains(p.getId()) && p.getTerrainType().isPopulationPossible())
					.collect(Collectors.toList());
			Iterator<Province> iter = suitableNeighbors.iterator();
			while (iter.hasNext() && provs.size() < minProvinces) {
				Province p = iter.next();
				provs.add(p);
				provsIds.add(p.getId());
			}
		}
		if (provs.size() < minProvinces && game.getGameParams().getRandom().nextDouble() > 0.001 * provs.size()) {
			return;
		}
		DataState dc = new DataState();
		dc.setId(game.nextStateId());
		dc.setName("#" + String.valueOf(dc.getId()));
		dc.setColor(createNewColorForState(game));
		State state = new State();
		state.buildFrom(game, dc);
		provs.forEach(p -> {
			state.addProvince(p);
			p.addLoyaltyToState(state.getId(), DataPopulation.LOYALTY_MAX);
		});
		state.setCapital(prov);
		game.registerState(state);
	}

	private static Color createNewColorForState(Game game) {
		CwRandom random = game.getGameParams().getRandom();
		int minValue = 50;
		int minDiff = 50;
		Color color = null;
		while (color == null) {
			int r = minValue + random.nextInt(255 - minValue);
			int g = minValue + random.nextInt(255 - minValue);
			int b = minValue + random.nextInt(255 - minValue);
			// TODO ? match with colors of others states
			if (Math.abs(r - g) >= minDiff && Math.abs(r - b) >= minDiff && Math.abs(b - g) >= minDiff) {
				color = new Color(r, g, b);
			}
		}
		return color;
	}

}
