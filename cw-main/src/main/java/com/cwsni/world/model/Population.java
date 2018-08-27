package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.CwException;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataScience;
import com.cwsni.world.model.data.DataScienceCollection;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.events.Event;
import com.cwsni.world.model.events.EventEpidemic;

public class Population {

	private static final Log logger = LogFactory.getLog(Population.class);

	private DataPopulation data;
	private Province province;
	private ScienceCollection science;
	private Culture culture;

	public Population() {
		data = new DataPopulation();
		science = new ScienceCollection(data.getScience());
		culture = new Culture(data.getCulture());
	}

	public int getAmount() {
		return data.getAmount();
	}

	void setAmount(int amount) {
		data.setAmount(amount);
		if (province != null) {
			double maxWealth = amount * province.getMap().getGame().getGameParams().getBudgetMaxWealthPerPerson();
			if (data.getWealth() > maxWealth) {
				data.setWealth(maxWealth);
			}
		}
	}

	public ScienceCollection getScience() {
		return science;
	}

	public Culture getCulture() {
		return culture;
	}

	public void buildFrom(Province province, DataPopulation dpop) {
		this.data = dpop;
		this.province = province;
		this.science = new ScienceCollection();
		science.buildFrom(dpop.getScience());
		this.culture = new Culture();
		culture.buildFrom(dpop.getCulture());
	}

	Population createNewPopFromThis(int migrantsCount) {
		migrantsCount = Math.min(migrantsCount, data.getAmount());
		Population newPop = new Population();
		newPop.setWealth(getWealth() * migrantsCount / getAmount());
		addWealth(-newPop.getWealth());
		newPop.setAmount(migrantsCount);
		setAmount(getAmount() - migrantsCount);
		newPop.getScience().cloneFrom(getScience());
		newPop.getCulture().cloneFrom(getCulture());
		DataScienceCollection.allGetter4Science().forEach(sG -> {
			DataScience scienceType = sG.apply(newPop.getScience().getScienceData());
			scienceType.setMax(scienceType.getAmount());
		});
		newPop.data.setLoyaltyToCountries(new HashMap<>(getLoyaltyToCountries()));
		newPop.data.setLoyaltyToStates(new HashMap<>(getLoyaltyToStates()));
		newPop.data.setLifeInCountries(new HashMap<>(data.getLifeInCountries()));
		return newPop;
	}

	void addPop(Population pop) {
		double ownFraction = (double) getAmount() / (getAmount() + pop.getAmount());
		setAmount(getAmount() + pop.getAmount());
		setWealth(getWealth() + pop.getWealth());
		getScience().mergeFrom(pop.getScience(), ownFraction);
		getCulture().mergeFrom(pop.getCulture(), ownFraction);
		if (ownFraction < 0.95) {
			mergeLoyalty(ownFraction, getLoyaltyToCountries(), pop.getLoyaltyToCountries(),
					(x, y) -> addLoyaltyToCountry(x, y));
			mergeLoyalty(ownFraction, getLoyaltyToStates(), pop.getLoyaltyToStates(),
					(x, y) -> addLoyaltyToState(x, y));
			mergeLifeInCountries(ownFraction, data.getLifeInCountries(), pop.data.getLifeInCountries());
		}
	}

	private void mergeLoyalty(double ownFraction, Map<Integer, Double> mergeTo, Map<Integer, Double> mergeFrom,
			BiConsumer<Integer, Double> func) {
		Set<Integer> ids = new HashSet<>();
		ids.addAll(mergeTo.keySet());
		ids.addAll(mergeFrom.keySet());
		for (Integer id : ids) {
			double newLoyalty = 0;
			Double thisL = mergeTo.get(id);
			Double otherL = mergeFrom.get(id);
			if (thisL == null) {
				thisL = 0.0;
			}
			if (otherL == null) {
				otherL = 0.0;
			}
			newLoyalty = thisL * ownFraction + otherL * (1 - ownFraction);
			func.accept(id, newLoyalty - thisL);
		}
	}

	private void mergeLifeInCountries(double ownFraction, Map<Integer, Integer> mergeTo,
			Map<Integer, Integer> mergeFrom) {
		Set<Integer> ids = new HashSet<>();
		ids.addAll(mergeTo.keySet());
		ids.addAll(mergeFrom.keySet());
		for (Integer id : ids) {
			int newValue = 0;
			Integer thisL = mergeTo.get(id);
			Integer otherL = mergeFrom.get(id);
			if (thisL == null) {
				thisL = 0;
			}
			if (otherL == null) {
				otherL = 0;
			}
			newValue = (int) (thisL * ownFraction + otherL * (1 - ownFraction));
			if (newValue > 0) {
				mergeTo.put(id, newValue);
			} else {
				mergeTo.remove(id);
			}
		}
	}

	public double getDiseaseResistance() {
		return EventEpidemic.getDiseaseResistance(getScience().getMedicine().getAmount());
	}

	DataPopulation getPopulationData() {
		return data;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	double getWealth() {
		return data.getWealth();
	}

	double getWealthLevel() {
		return getWealth() / getAmount() / province.getMap().getGame().getGameParams().getBudgetMaxWealthPerPerson();
	}

	private void setWealth(double wealth) {
		data.setWealth(wealth);
	}

	void addWealth(double delta) {
		data.setWealth(Math.max(0, data.getWealth() + delta));
	}

	public void sufferFromWar(double loss) {
		setWealth(getWealth() * (1 - loss));
		setAmount((int) (getAmount() * (1 - loss)));
	}

	public void addLoyaltyToCountry(int id, double delta) {
		addLoyalty(data.getLoyaltyToCountries(), id, delta, null);
	}

	public void addLoyaltyToState(int id, double delta) {
		addLoyaltyToState(id, delta, null);
	}

	public void addLoyaltyToState(int id, double delta, Double maxStateLoyalty) {
		addLoyalty(data.getLoyaltyToStates(), id, delta, maxStateLoyalty);
	}

	private void addLoyalty(Map<Integer, Double> loyalties, int id, double delta, Double maxLoyalty) {
		Double currentLoyalty = loyalties.get(id);
		if (currentLoyalty == null) {
			currentLoyalty = 0.0;
		}
		currentLoyalty = Math.min(Math.max(currentLoyalty + delta, 0),
				maxLoyalty != null ? maxLoyalty : DataPopulation.LOYALTY_MAX);
		currentLoyalty = DataFormatter.doubleWith4points(currentLoyalty);
		if (currentLoyalty < DataPopulation.LOYALTY_MAX / 120 && delta <= 0) {
			loyalties.remove(id);
		} else {
			loyalties.put(id, currentLoyalty);
		}
	}

	public void decreaseLoyaltyToAllCountries(double coeff) {
		// We need new List because DataPopulation::addCountryLoyalty can remove
		// elements from collection.
		List<Integer> ids = new ArrayList<>(data.getLoyaltyToCountries().keySet());
		for (Integer id : ids) {
			Double loyalty = getLoyaltyToCountry(id);
			if (loyalty != null) {
				double newLoyalty = loyalty * coeff;
				addLoyaltyToCountry(id, newLoyalty - loyalty);
			}
		}
	}

	public void decreaseLoyaltyToAllStates(double coeff) {
		// We need new List because DataPopulation::addCountryLoyalty can remove
		// elements from collection.
		List<Integer> ids = new ArrayList<>(data.getLoyaltyToStates().keySet());
		for (Integer id : ids) {
			Double loyalty = getLoyaltyToState(id);
			if (loyalty != null) {
				double newLoyalty = loyalty * coeff;
				addLoyaltyToState(id, newLoyalty - loyalty);
			}
		}
	}

	public Map<Integer, Double> getLoyaltyToCountries() {		
		return Collections.unmodifiableMap(data.getLoyaltyToCountries());
	}

	public Map<Integer, Integer> getLifeInCountries() {
		return Collections.unmodifiableMap(data.getLifeInCountries());
	}

	public Double getLoyaltyToCountry(Integer id) {
		Double loyalty = data.getLoyaltyToCountries().get(id);
		if (loyalty != null) {
			return loyalty;
		} else {
			return 0.0;
		}
	}

	public Double getLoyaltyToState(Integer id) {
		Double loyalty = data.getLoyaltyToStates().get(id);
		if (loyalty != null) {
			return loyalty;
		} else {
			return 0.0;
		}
	}

	public Map<Integer, Double> getLoyaltyToStates() {
		return Collections.unmodifiableMap(data.getLoyaltyToStates());
	}

	// --------------- static section -----------------------

	static public void migrateNewTurn(Province from, Game game) {
		GameParams gParams = game.getGameParams();
		List<Province> provsTo = from.getNeighbors().stream()
				.filter(n -> n.getTerrainType().isPopulationPossible() && n.getPopulationExcess() < 1)
				.collect(Collectors.toList());
		if (provsTo.size() == 0) {
			return;
		}
		int mPops = 0;
		int maxPopulation = from.getMaxPopulation();
		if (from.getPopulationAmount() > maxPopulation || from.getSoilFertility() < 1) {
			// hunger migration
			mPops = (int) (from.getPopulationAmount() * (gParams.getPopulationMaxExcess() - 1) / 10);
		} else {
			// regular migration
			mPops = (int) (from.getPopulationAmount() * gParams.getPopulationBaseMigration());
		}
		if (mPops > provsTo.size()) {
			int mPopsToEachNeighbor = mPops / provsTo.size();
			provsTo.forEach(p -> migrateTo(from, mPopsToEachNeighbor, p));
		}
	}

	static private void migrateTo(Province from, int popAmount, Province to) {
		int totalAmount = from.getPopulation().stream().mapToInt(p -> p.getAmount()).sum();
		if (popAmount > totalAmount) {
			throw new CwException("popAmount > totalAmount : prov.id = " + from.getId());
		}
		double fractionFromEach = (double) popAmount / from.getPopulationAmount();
		from.getPopulation().forEach(fromPop -> {
			int migrantsCount = (int) (fromPop.getAmount() * fractionFromEach);
			if (migrantsCount > 0 && migrantsCount < fromPop.getAmount()) {
				to.getImmigrants().add(fromPop.createNewPopFromThis(migrantsCount));
			}
		});
	}

	static public void growPopulationNewTurn(Province prov, Game game) {
		GameParams gParams = game.getGameParams();
		int populationAmount = prov.getPopulationAmount();
		int maxPopulation = (int) Math.max(1, prov.getMaxPopulation() * gParams.getPopulationMaxExcess());
		if (prov.getCountry() == null && maxPopulation > gParams.getPopulationLimitWithoutGovernment()) {
			maxPopulation = (int) (gParams.getPopulationLimitWithoutGovernment() * prov.getSoilFertility());
		}
		double currentPopFromMax = (double) populationAmount / maxPopulation;
		if (currentPopFromMax < 1) {
			if (prov.getSoilFertility() >= 1) {
				// growth pops
				prov.getPopulation().forEach(p -> {
					int newAmount = (int) (p.getAmount()
							* (1 + gParams.getPopulationBaseGrowth() + p.getDiseaseResistance() / 100));
					newAmount = (int) Math.min(newAmount, populationAmount / currentPopFromMax);
					p.setAmount(newAmount);
				});
			} else {
				// not enough food from fields
				dieFromHunger(game, prov, 1 - prov.getSoilFertility());
			}
		} else {
			// overpopulation
			dieFromOverpopulation(game, prov, 1 - 1 / currentPopFromMax);
		}
	}

	public static void processEventsNewTurn(Province prov, Game game) {
		List<Event> provEvents = new ArrayList<>(prov.getEvents().getEvents());
		provEvents.forEach(e -> Event.processEvent(game, prov, e));
	}

	private static void dieFromHunger(Game game, Province from, double fraction) {
		double kf = Math.min(fraction / 5, 0.1);
		game.getGameStats().addDiedFromHunger(from.getPopulationAmount() * kf);
		from.getPopulation().forEach(p -> {
			p.setAmount((int) (p.getAmount() * (1 - kf)));
		});
	}

	public static void dieFromDisease(Game game, Province from, double deathRate) {
		// regular population
		from.getPopulation().forEach(p -> {
			double effectiveDeathRate = deathRate * (1 - p.getDiseaseResistance());
			int died = (int) (p.getAmount() * effectiveDeathRate);
			p.setAmount(p.getAmount() - died);
			game.getGameStats().addDiedFromDisease(died);
		});
		// armies
		double effectiveDeathRate = deathRate * (1 - from.getDiseaseResistance());
		from.getArmies().forEach(a -> {
			int died = (int) (a.getSoldiers() * effectiveDeathRate);
			a.setSoldiers(a.getSoldiers() - died);
			game.getGameStats().addDiedFromDisease(died);
		});
	}

	private static void dieFromOverpopulation(Game game, Province from, double fraction) {
		double kf = Math.min(fraction / 5, 0.1);
		game.getGameStats().addDiedFromOverpopulation(from.getPopulationAmount() * kf);
		from.getPopulation().forEach(p -> {
			p.setAmount((int) (p.getAmount() * (1 - kf)));
		});
	}

	public static void processImmigrantsAndMergePops(Province p, Game game) {
		if (!p.getImmigrants().isEmpty()) {
			p.getImmigrants().forEach(immg -> p.addPopulation(immg));
			p.getImmigrants().clear();
		}
		if (p.getPopulation().size() <= 1) {
			return;
		}
		List<Population> currentList = new ArrayList<>(p.getPopulation());
		Population toPop = currentList.get(0);
		for (int i = 1; i < currentList.size(); i++) {
			Population fromPop = currentList.get(i);
			toPop.addPop(fromPop);
			p.removePopulation(fromPop);
		}
	}

	public void processLifeInTheCountryNewTurn() {
		Integer countryId = province.getCountryId();
		if (countryId != null) {
			Integer value = data.getLifeInCountries().get(countryId);
			value = 2 + (value != null ? value : 0);
			data.getLifeInCountries().put(countryId, value);
		}
		Iterator<Entry<Integer, Integer>> iter = data.getLifeInCountries().entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int value = entry.getValue() - 1;
			if (value > 0) {
				data.getLifeInCountries().put(entry.getKey(), value);
			} else {
				iter.remove();
			}
		}
	}

	public static void processLoyaltyNewTurn(Province p, Game game) {
		int provPopulationAmount = p.getPopulationAmount();
		if (provPopulationAmount == 0) {
			return;
		}
		Country country = p.getCountry();
		State state = p.getState();
		GameParams gParams = game.getGameParams();

		// decreasing - regular (mostly for other countries and states)
		p.decreaseLoyaltyToAllCountries(gParams.getPopulationLoyaltyDecreasingCoeffDefault());
		p.decreaseLoyaltyToAllStates(gParams.getPopulationLoyaltyDecreasingCoeffDefault());

		if (state != null) {
			Double maxStateLoyalty = state.getMaxStateLoyalty();
			// TODO Loyalty should depend on (state population) / (capital state
			// population).
			// It will allow to have big countries with low populated provinces (like Russia
			// with Sibir).
			p.addLoyaltyToState(state.getId(), gParams.getPopulationLoyaltyIncreasingForState(), maxStateLoyalty);
			if (p.getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
				p.addLoyaltyToState(state.getId(), gParams.getPopulationLoyaltyDecreasingEpidemic(), maxStateLoyalty);
			}
		}

		if (country == null) {
			return;
		}

		// decreasing - diseases
		if (p.getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
			p.addLoyaltyToCountry(country.getId(), gParams.getPopulationLoyaltyDecreasingEpidemic());
		}

		// decreasing - overpopulation
		if (p.getPopulationExcess() > 1) {
			p.addLoyaltyToCountry(country.getId(),
					gParams.getPopulationLoyaltyDecreasingOverpopulation() * p.getPopulationExcess());
		}

		// increasing/decreasing - wealth
		double provinceWealthLevel = p.getWealthLevelOfProvince();
		for (Population pop : p.getPopulation()) {
			double wealthLevel = (pop.getWealthLevel() + provinceWealthLevel) / 2;
			if (wealthLevel > 1) {
				logger.warn("wealthLevel must not be > 1, but it is " + wealthLevel);
				wealthLevel = 1;
			}
			double delta = (wealthLevel - gParams.getPopulationLoyaltyWealthThreshold())
					* gParams.getPopulationLoyaltyWealthThresholdCoeff();
			pop.addLoyaltyToCountry(country.getId(), delta);
		}

		// increasing - government influence
		p.addLoyaltyToCountry(country.getId(),
				p.getGovernmentInfluence() * gParams.getPopulationLoyaltyIncreasingGovernmnentCoeff());
		// increasing - capital influence
		if (p.equals(country.getCapital())) {
			p.addLoyaltyToCountry(country.getId(), gParams.getPopulationLoyaltyIncreasingCapital());
		}

		// increasing - life in the country
		double lifeInTheCountry = p.getPopulation().stream().mapToDouble(pop -> {
			Integer value = pop.data.getLifeInCountries().get(country.getId());
			if (value == null) {
				return 0.0;
			} else {
				return value * pop.getAmount();
			}
		}).sum() / provPopulationAmount;
		if (lifeInTheCountry > 0) {
			double delta = Math.log10(lifeInTheCountry) * gParams.getPopulationLoyaltyIncreasingForLifeInTheCountry();
			p.addLoyaltyToCountry(country.getId(), delta);
		}
	}

	public static String createDescriptionForLoyaltyChanges(Game game, Province p, LocaleMessageSource messageSource) {
		int provPopulationAmount = p.getPopulationAmount();
		if (provPopulationAmount == 0) {
			return null;
		}
		Country country = p.getCountry();
		if (country == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		GameParams gParams = game.getGameParams();

		// decreasing - regular (mostly for other countries and states)
		sb.append("-"
				+ DataFormatter.doubleWith2points(p.getLoyaltyToCountry(country.getId())
						* (1 - gParams.getPopulationLoyaltyDecreasingCoeffDefault()) * 100)
				+ " " + messageSource.getMessage("info.pane.prov.country.loyalty.description.default-decreasing"))
				.append("\n");

		// decreasing - diseases
		if (p.getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
			sb.append(gParams.getPopulationLoyaltyDecreasingEpidemic() * 100 + " "
					+ messageSource.getMessage("info.pane.prov.country.loyalty.description.epidemic")).append("\n");
		}

		// decreasing - overpopulation
		if (p.getPopulationExcess() > 1) {
			double delta = gParams.getPopulationLoyaltyDecreasingOverpopulation() * p.getPopulationExcess();
			sb.append(DataFormatter.doubleWith2points(delta * 100) + " "
					+ messageSource.getMessage("info.pane.prov.country.loyalty.description.overpopulation"))
					.append("\n");
		}

		// increasing/decreasing - wealth
		int provincePopulation = p.getPopulationAmount();
		double provinceWealthLevel = p.getWealthLevelOfProvince();
		double delta = 0;
		for (Population pop : p.getPopulation()) {
			double wealthLevel = (pop.getWealthLevel() + provinceWealthLevel) / 2;
			delta += (wealthLevel - gParams.getPopulationLoyaltyWealthThreshold())
					* gParams.getPopulationLoyaltyWealthThresholdCoeff() * pop.getAmount();
		}
		delta /= provincePopulation;
		if (delta > 0) {
			sb.append("+");
		}
		sb.append(DataFormatter.doubleWith2points(delta * 100) + " "
				+ messageSource.getMessage("info.pane.prov.country.loyalty.description.wealth")).append("\n");

		// increasing - government influence
		delta = p.getGovernmentInfluence() * gParams.getPopulationLoyaltyIncreasingGovernmnentCoeff();
		sb.append("+" + DataFormatter.doubleWith2points(delta * 100) + " "
				+ messageSource.getMessage("info.pane.prov.country.loyalty.description.government-influence"))
				.append("\n");
		if (p.equals(country.getCapital())) {
			sb.append("+" + gParams.getPopulationLoyaltyIncreasingCapital() * 100 + " "
					+ messageSource.getMessage("info.pane.prov.country.loyalty.description.capital")).append("\n");
		}

		// increasing - life in the country
		double lifeInTheCountry = p.getPopulation().stream().mapToDouble(pop -> {
			Integer value = pop.data.getLifeInCountries().get(country.getId());
			if (value == null) {
				return 0.0;
			} else {
				return 1.0 * value * pop.getAmount();
			}
		}).sum() / provPopulationAmount;
		if (lifeInTheCountry > 0) {
			delta = Math.log10(lifeInTheCountry) * gParams.getPopulationLoyaltyIncreasingForLifeInTheCountry();
			sb.append("+" + DataFormatter.doubleWith2points(delta * 100) + " "
					+ messageSource
							.getMessage("info.pane.prov.country.loyalty.description.population.life-in-the-country"))
					.append("\n");
		}

		sb.append("---------------\n");
		// increasing - temporary from army
		long lfa = Math.round(p.getLoyaltyToCountryFromArmy() * 100);
		if (lfa != 0) {
			sb.append("+" + lfa + " " + messageSource.getMessage("info.pane.prov.country.loyalty.description.army"))
					.append("\n");
		}

		// increasing - temporary from focus
		double countryFocus = DataFormatter.doubleWith2points(country.getFocus().getLoyaltyFlatBonus() * 100);
		if (countryFocus != 0) {
			if (countryFocus > 0) {
				sb.append("+");
			}
			sb.append(countryFocus + " " + messageSource.getMessage("info.pane.prov.country.loyalty.description.focus"))
					.append("\n");
		}

		return sb.toString();
	}

}
