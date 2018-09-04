package com.cwsni.world.model.engine;

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

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataScience;
import com.cwsni.world.model.data.DataScienceCollection;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.util.ComparisonTool;
import com.cwsni.world.util.CwException;

public class Population {

	private static final Log logger = LogFactory.getLog(Population.class);

	private Game game;
	private DataPopulation data;
	private Province province;
	private ScienceCollection science;
	private Culture culture;

	public Population(Game game) {
		this.game = game;
		data = new DataPopulation();
		science = new ScienceCollection(data.getScience());
		culture = new Culture(data.getCulture());
	}

	protected Game getGame() {
		return game;
	}

	protected Turn getTurn() {
		return game.getTurn();
	}

	public int getAmount() {
		return data.getAmount();
	}

	protected void setAmount(int amount) {
		amount = Math.min(amount, getGame().getGameParams().getPopulationLimitInProvince());
		data.setAmount(amount);
		if (province != null) {
			double maxWealth = amount * getGame().getGameParams().getBudgetMaxWealthPerPerson();
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

	protected Population recruitPopForArmy(int soldiers) {
		int maxRecruits = getAvailablePeopleForRecruiting();
		if (maxRecruits <= 0) {
			return null;
		}
		if (maxRecruits < 1.1 * getGame().getGameParams().getArmyMinAllowedSoldiers()) {
			return null;
		}
		soldiers = Math.min(soldiers, maxRecruits);
		// It is not very accurate, because we calculate percent for current population
		// instead of full (current + already recruited), but it is OK for now.
		double deltaPercent = 1.0 * soldiers / getAmount();
		double newRecruitedPercent = getRecruitedPercent() + deltaPercent;
		Population newPop = createNewPopFromThis(soldiers);
		newPop.setRecruitedPercent(0);
		setRecruitedPercent(newRecruitedPercent);
		addLoyaltyToCountry(province.getCountryId(), -deltaPercent);
		if (province.getState() != null) {
			addLoyaltyToState(province.getStateId(), -deltaPercent / 4);
		}
		return newPop;
	}

	public int getAvailablePeopleForRecruiting() {
		return (int) Math.max(0, 1.0 * getAmount()
				* (getGame().getGameParams().getPopulationRecruitPercentBaseMax() - getRecruitedPercent()));
	}

	/**
	 * Main purpose: migration. Army should invoke
	 * {@link Population#recruitPopForArmy(int)}} instead of this method
	 */
	protected Population createNewPopFromThis(int migrantsCount) {
		migrantsCount = Math.min(migrantsCount, data.getAmount());
		Population newPop = new Population(getGame());
		newPop.setWealth(getWealth() * migrantsCount / getAmount());
		addWealth(-newPop.getWealth());
		newPop.data.setCasualties(data.getCasualties() * migrantsCount / getAmount());
		data.setCasualties(data.getCasualties() - newPop.data.getCasualties());
		newPop.setAmount(migrantsCount);
		newPop.setRecruitedPercent(getRecruitedPercent());
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

	protected void addPop(Population pop) {
		double ownFraction = (double) getAmount() / (getAmount() + pop.getAmount());
		setAmount(getAmount() + pop.getAmount());
		setWealth(getWealth() + pop.getWealth());
		data.setCasualties(data.getCasualties() + pop.data.getCasualties());
		setRecruitedPercent(getRecruitedPercent() * ownFraction + pop.getRecruitedPercent() * (1 - ownFraction));
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

	protected DataPopulation getPopulationData() {
		return data;
	}

	private void setRecruitedPercent(double recruitedPercent) {
		data.setRecruitedPercent(recruitedPercent);
	}

	public double getRecruitedPercent() {
		return data.getRecruitedPercent();
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	protected double getWealth() {
		return data.getWealth();
	}

	protected double getWealthLevel() {
		return getWealth() / (getAmount() + 1) / getGame().getGameParams().getBudgetMaxWealthPerPerson();
	}

	private void setWealth(double wealth) {
		data.setWealth(wealth);
	}

	protected void addWealth(double delta) {
		data.setWealth(Math.max(0, data.getWealth() + delta));
	}

	public int sufferFromWar(double loss) {
		setWealth(getWealth() * (1 - loss));
		int oldAmount = getAmount();
		setAmount((int) (getAmount() * (1 - loss)));
		int casualties = oldAmount - getAmount();
		addCasualties(casualties, province.getCountry());
		game.getGameStats().addDiedFromInvasion(casualties);
		return casualties;
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
		currentLoyalty = DataFormatter.doubleWithPrecison(currentLoyalty, 6);
		if ((currentLoyalty < DataPopulation.LOYALTY_MAX / 100 && delta <= 0) && (province == null
				|| province.getCountry() == null || !ComparisonTool.isEqual(province.getCountryId(), id))) {
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

	protected void processNewTurnAsArmy() {
		processCasualtiesNewTurn();
	}

	private void processCasualtiesNewTurn() {
		data.setCasualties((long) (1.0 * data.getCasualties()
				* game.getTurn().multiplyPerYear(getGame().getGameParams().getPopulationCasualtiesCoeffPerYear())));
	}

	protected long getCasualties() {
		return data.getCasualties();
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
		if (mPops > provsTo.size() * 50) {
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
				double populationBaseGrowth = game.getTurn()
						.multiplyPerYear(1 + gParams.getPopulationBaseGrowthPerYear());
				prov.getPopulation().forEach(p -> {
					int newAmount = (int) (1.0 * p.getAmount() * populationBaseGrowth);
					newAmount = (int) Math.min(newAmount, 1.0 * populationAmount / currentPopFromMax);
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
		// recruits pool growth && casualties decreasing
		double populationRecruitPercentRestore = game.getTurn()
				.addPerYear(game.getGameParams().getPopulationRecruitPercentBaseRestore());
		prov.getPopulation().forEach(p -> {
			p.setRecruitedPercent(p.getRecruitedPercent() - populationRecruitPercentRestore);
			p.processCasualtiesNewTurn();
		});
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
			int diseaseResistance = 0; // TODO in events
			double effectiveDeathRate = deathRate * (1 - diseaseResistance);
			int died = (int) (p.getAmount() * effectiveDeathRate);
			p.setAmount(p.getAmount() - died);
			game.getGameStats().addDiedFromDisease(died);
			p.addCasualties((int) (1.0 * died * game.getGameParams().getPopulationCasualtiesFromDiseasesCoeff()),
					from.getCountry());
		});
		// armies
		double effectiveDeathRate = deathRate * (1 - from.getDiseaseResistance());
		from.getArmies().forEach(a -> {
			int died = (int) (a.getSoldiers() * effectiveDeathRate);
			a.setSoldiers(a.getSoldiers() - died);
			a.getCountry().addCasualties(
					(int) (1.0 * died * game.getGameParams().getPopulationCasualtiesFromDiseasesCoeff()));
			game.getGameStats().addDiedFromDisease(died);
		});
	}

	protected void addCasualties(int delta, Country country) {
		if (delta < 0) {
			System.out.println("population addCasualties: delta < 0, delta=" + delta);
		}
		data.setCasualties(data.getCasualties() + delta);
		if (country != null) {
			country.addCasualties(delta);
		}
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
		int delta = (int) getTurn().addPerWeek(1);
		Integer countryId = province.getCountryId();
		if (countryId != null) {
			Integer value = data.getLifeInCountries().get(countryId);
			value = delta * 2 + (value != null ? value : 0);
			data.getLifeInCountries().put(countryId, value);
		}
		Iterator<Entry<Integer, Integer>> iter = data.getLifeInCountries().entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int value = entry.getValue() - delta;
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
		Turn turn = game.getTurn();
		Country country = p.getCountry();
		State state = p.getState();
		GameParams gParams = game.getGameParams();

		// decreasing - regular (mostly for other countries and states)
		double populationLoyaltyDecreasingCoeff = turn
				.multiplyPerYear(gParams.getPopulationLoyaltyDecreasingCoeffDefaultPerYear());
		double rl = p.getLoyaltyToCountry();
		p.decreaseLoyaltyToAllCountries(populationLoyaltyDecreasingCoeff);
		p.decreaseLoyaltyToAllStates(populationLoyaltyDecreasingCoeff);
		double rl2 = p.getLoyaltyToCountry();

		if (state != null) {
			Double maxStateLoyalty = state.getMaxStateLoyalty();
			p.addLoyaltyToState(state.getId(), turn.addPerYear(gParams.getPopulationLoyaltyIncreasingForStatePerYear()),
					maxStateLoyalty);
			// TODO
			/*
			 * if (p.getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
			 * p.addLoyaltyToState(state.getId(),
			 * gParams.getPopulationLoyaltyDecreasingEpidemic(), maxStateLoyalty); }
			 */
		}

		if (country == null) {
			return;
		}

		// decreasing - diseases TODO
		/*
		 * if (p.getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
		 * p.addLoyaltyToCountry(country.getId(),
		 * gParams.getPopulationLoyaltyDecreasingEpidemic()); }
		 */

		// decreasing - overpopulation
		if (p.getPopulationExcess() > 1) {
			p.addLoyaltyToCountry(country.getId(),
					turn.addPerYear(gParams.getPopulationLoyaltyDecreasingOverpopulationPerYear())
							* p.getPopulationExcess());
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
					* turn.addPerYear(gParams.getPopulationLoyaltyWealthThresholdCoeffPerYear());
			pop.addLoyaltyToCountry(country.getId(), delta);
		}

		// increasing - government influence
		p.addLoyaltyToCountry(country.getId(), p.getGovernmentInfluence()
				* turn.addPerYear(gParams.getPopulationLoyaltyIncreasingGovernmnentCoeffPerYear()));
		// increasing - capital influence
		if (p.equals(country.getCapital())) {
			p.addLoyaltyToCountry(country.getId(),
					turn.addPerYear(gParams.getPopulationLoyaltyIncreasingCapitalPerYear()));
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
			double delta = Math.log10(lifeInTheCountry)
					* turn.addPerYear(gParams.getPopulationLoyaltyIncreasingForLifeInTheCountryPerYear());
			p.addLoyaltyToCountry(country.getId(), delta);
		}
	}

	public static String createDescriptionForLoyaltyChangesPerYear(Game game, Province p,
			LocaleMessageSource messageSource) {
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
						* (1 - gParams.getPopulationLoyaltyDecreasingCoeffDefaultPerYear()) * 100)
				+ " " + messageSource.getMessage("info.pane.prov.country.loyalty.description.default-decreasing"))
				.append("\n");

		// decreasing - diseases TODOS
		/*
		 * if (p.getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
		 * sb.append(gParams.getPopulationLoyaltyDecreasingEpidemic() * 100 + " " +
		 * messageSource.getMessage(
		 * "info.pane.prov.country.loyalty.description.epidemic")).append("\n"); }
		 */

		// decreasing - overpopulation
		if (p.getPopulationExcess() > 1) {
			double delta = gParams.getPopulationLoyaltyDecreasingOverpopulationPerYear() * p.getPopulationExcess();
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
					* gParams.getPopulationLoyaltyWealthThresholdCoeffPerYear() * pop.getAmount();
		}
		delta /= provincePopulation;
		if (delta > 0) {
			sb.append("+");
		}
		sb.append(DataFormatter.doubleWith2points(delta * 100) + " "
				+ messageSource.getMessage("info.pane.prov.country.loyalty.description.wealth")).append("\n");

		// increasing - government influence
		delta = p.getGovernmentInfluence() * gParams.getPopulationLoyaltyIncreasingGovernmnentCoeffPerYear();
		sb.append("+" + DataFormatter.doubleWith2points(delta * 100) + " "
				+ messageSource.getMessage("info.pane.prov.country.loyalty.description.government-influence"))
				.append("\n");
		if (p.equals(country.getCapital())) {
			sb.append("+" + gParams.getPopulationLoyaltyIncreasingCapitalPerYear() * 100 + " "
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
			delta = Math.log10(lifeInTheCountry) * gParams.getPopulationLoyaltyIncreasingForLifeInTheCountryPerYear();
			sb.append("+" + DataFormatter.doubleWith2points(delta * 100) + " "
					+ messageSource
							.getMessage("info.pane.prov.country.loyalty.description.population.life-in-the-country"))
					.append("\n");
		}

		sb.append("---------------\n");
		// current raw loyalty
		sb.append("=" + DataFormatter.doubleWithPrecison(p.getRawLoyaltyToCountryForUI() * 100, 4) + " "
				+ messageSource.getMessage("info.pane.prov.country.loyalty.description.current")).append("\n");

		// increasing - temporary from army
		long lfa = Math.round(p.getLoyaltyToCountryFromArmy() * 100);
		if (lfa != 0) {
			sb.append("+" + lfa + " " + messageSource.getMessage("info.pane.prov.country.loyalty.description.army"))
					.append("\n");
		}

		// increasing - temporary from focus
		double countryFocus = country.getFocus().getLoyaltyFlatBonus();
		if (countryFocus != 0) {
			if (countryFocus > 0) {
				sb.append("+");
			}
			sb.append(DataFormatter.doubleWith2points(countryFocus * 100) + " "
					+ messageSource.getMessage("info.pane.prov.country.loyalty.description.focus")).append("\n");
		}

		// decreasing - temporary from casualties
		double localCasualtiesLoyalty = p.getLoyaltyToCountryFromLocalCasualties();
		if (localCasualtiesLoyalty != 0) {
			sb.append(DataFormatter.doubleWith2points(localCasualtiesLoyalty * 100) + " "
					+ messageSource.getMessage("info.pane.prov.country.loyalty.description.casualties-local"))
					.append("\n");
		}
		double countryCasualtiesLoyalty = country.getLoyaltyToCountryFromCountryCasualties();
		if (countryCasualtiesLoyalty != 0) {
			sb.append(DataFormatter.doubleWith2points(countryCasualtiesLoyalty * 100) + " "
					+ messageSource.getMessage("info.pane.prov.country.loyalty.description.casualties-country"))
					.append("\n");
		}

		return sb.toString();
	}

}
