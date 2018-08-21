package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	private void setAmount(int amount) {
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
		newPop.setWealth(migrantsCount / getAmount() * getWealth());
		addWealth(-newPop.getWealth());
		newPop.setAmount(migrantsCount);
		setAmount(getAmount() - migrantsCount);
		newPop.getScience().cloneFrom(getScience());
		newPop.getCulture().cloneFrom(getCulture());
		DataScienceCollection.allGetter4Science().forEach(sG -> {
			DataScience scienceType = sG.apply(newPop.getScience().getScienceData());
			scienceType.setMax(scienceType.getAmount());
		});
		newPop.data.setCountriesLoyalty(new HashMap<>(getCountriesLoyalty()));
		newPop.data.setStatesLoyalty(new HashMap<>(getStatesLoyalty()));
		return newPop;
	}

	void addPop(Population pop) {
		double ownFraction = (double) getAmount() / (getAmount() + pop.getAmount());
		getScience().mergeFrom(pop.getScience(), ownFraction);
		getCulture().mergeFrom(pop.getCulture(), ownFraction);
		setAmount(getAmount() + pop.getAmount());
		setWealth(getWealth() + pop.getWealth());
		mergeLoyalty(ownFraction, getCountriesLoyalty(), pop.getCountriesLoyalty(), (x, y) -> addCountryLoyalty(x, y));
		mergeLoyalty(ownFraction, getStatesLoyalty(), pop.getStatesLoyalty(), (x, y) -> addStateLoyalty(x, y));
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

	public void addCountryLoyalty(int id, double delta) {
		data.addCountryLoyalty(id, delta);
	}

	public void addStateLoyalty(int id, double delta) {
		data.addStateLoyalty(id, delta);
	}

	public void addAllCountriesLoyalty(double delta) {
		data.addAllCountriesLoyalty(delta);
	}

	public void addAllStatesLoyalty(double delta) {
		data.addAllStatesLoyalty(delta);
	}

	public Map<Integer, Double> getCountriesLoyalty() {
		return Collections.unmodifiableMap(data.getCountriesLoyalty());
	}

	public Double getCountryLoyalty(Integer id) {
		return data.getCountryLoyalty(id);
	}

	public Double getStateLoyalty(Integer id) {
		return data.getStateLoyalty(id);
	}

	public Map<Integer, Double> getStatesLoyalty() {
		return Collections.unmodifiableMap(data.getStatesLoyalty());
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

	public static void processLoyaltyNewTurn(Province p, Game game) {
		if (p.getPopulationAmount() == 0) {
			return;
		}
		Country country = p.getCountry();
		State state = p.getState();
		GameParams gParams = game.getGameParams();

		// decreasing - regular (mostly for other countries and states)
		p.addAllCountriesLoyalty(gParams.getPopulationLoyaltyDecreasingDefault());
		p.addAllStateLoyalty(gParams.getPopulationLoyaltyDecreasingDefault());

		// decreasing - diseases
		if (p.getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
			if (country != null) {
				p.addCountryLoyalty(country.getId(), gParams.getPopulationLoyaltyDecreasingEpidemic());
			}
			if (state != null) {
				p.addStateLoyalty(state.getId(), gParams.getPopulationLoyaltyDecreasingEpidemic());
			}
		}

		// decreasing - overpopulation
		if (p.getPopulationExcess() > 1 && country != null) {
			p.addCountryLoyalty(country.getId(),
					gParams.getPopulationLoyaltyDecreasingOverpopulation() * p.getPopulationExcess());
		}

		// increasing/decreasing - wealth
		if (country != null) {
			int provincePopulation = p.getPopulationAmount();
			double provinceWealthLevel = p.getWealthOfProvince() / provincePopulation
					/ gParams.getBudgetMaxWealthPerPerson();
			for (Population pop : p.getPopulation()) {
				double populationWealthLevel = pop.getWealth() / pop.getAmount()
						/ gParams.getBudgetMaxWealthPerPerson();
				double wealthLevel = (populationWealthLevel + provinceWealthLevel) / 2;
				if (wealthLevel > 1) {
					logger.warn("wealthLevel must not be > 1, but it is " + wealthLevel);
					wealthLevel = 1;
				}
				double delta = (wealthLevel - gParams.getPopulationLoyaltyWealthThreshold())
						* gParams.getPopulationLoyaltyWealthThresholdCoeff();
				pop.addCountryLoyalty(country.getId(), delta);
			}
		}

		// increasing - government influence
		if (country != null) {
			p.addCountryLoyalty(country.getId(),
					p.getGovernmentInfluence() * gParams.getPopulationLoyaltyIncreasingGovernmnentCoeff());
			// increasing - capital influence
			if (p.equals(country.getCapital())) {
				p.addCountryLoyalty(country.getId(), gParams.getPopulationLoyaltyIncreasingCapital());
			}
		}
		if (state != null) {
			p.addStateLoyalty(state.getId(), 2.0);
		}

	}

	public static String createDescriptionForLoyaltyChanges(Game game, Province p, LocaleMessageSource messageSource) {
		StringBuilder sb = new StringBuilder();
		GameParams gParams = game.getGameParams();
		Country country = p.getCountry();

		// decreasing - regular (mostly for other countries and states)
		sb.append(gParams.getPopulationLoyaltyDecreasingDefault() + " "
				+ messageSource.getMessage("info.pane.prov.country.loyalty.description.default-decreasing"))
				.append("\n");

		// decreasing - diseases
		if (p.getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
			if (country != null) {
				sb.append(gParams.getPopulationLoyaltyDecreasingEpidemic() + " "
						+ messageSource.getMessage("info.pane.prov.country.loyalty.description.epidemic")).append("\n");
			}
		}

		// decreasing - overpopulation
		if (p.getPopulationExcess() > 1) {
			double delta = DataFormatter.doubleWith2points(
					gParams.getPopulationLoyaltyDecreasingOverpopulation() * p.getPopulationExcess());
			sb.append(
					delta + " " + messageSource.getMessage("info.pane.prov.country.loyalty.description.overpopulation"))
					.append("\n");
		}

		// increasing/decreasing - wealth
		if (country != null) {
			int provincePopulation = p.getPopulationAmount();
			double provinceWealthLevel = p.getWealthOfProvince() / provincePopulation
					/ gParams.getBudgetMaxWealthPerPerson();
			double delta = 0;
			for (Population pop : p.getPopulation()) {
				double populationWealthLevel = pop.getWealth() / pop.getAmount()
						/ gParams.getBudgetMaxWealthPerPerson();
				double wealthLevel = (populationWealthLevel + provinceWealthLevel) / 2;
				delta += (wealthLevel - gParams.getPopulationLoyaltyWealthThreshold())
						* gParams.getPopulationLoyaltyWealthThresholdCoeff() * pop.getAmount();
			}
			delta /= provincePopulation;
			if (delta > 0) {
				sb.append("+");
			}
			sb.append(DataFormatter.doubleWith2points(delta) + " "
					+ messageSource.getMessage("info.pane.prov.country.loyalty.description.wealth")).append("\n");
		}

		// increasing - government influence
		if (country != null) {
			double delta = DataFormatter.doubleWith2points(
					p.getGovernmentInfluence() * gParams.getPopulationLoyaltyIncreasingGovernmnentCoeff());
			sb.append("+" + delta + " "
					+ messageSource.getMessage("info.pane.prov.country.loyalty.description.government-influence"))
					.append("\n");
			if (p.equals(country.getCapital())) {
				sb.append("+" + gParams.getPopulationLoyaltyIncreasingCapital() + " "
						+ messageSource.getMessage("info.pane.prov.country.loyalty.description.capital")).append("\n");
			}
		}
		return sb.toString();
	}

	public static void addPopulationFromArmy(Province p, int soldiers) {
		double fraction = 1.0 * soldiers / p.getPopulationAmount();
		p.getPopulation().forEach(pop -> pop.setAmount((int) (pop.getAmount() + pop.getAmount() * fraction)));
	}

}
