package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.cwsni.world.CwException;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataScience;
import com.cwsni.world.model.data.DataScienceCollection;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.events.Event;
import com.cwsni.world.model.events.EventEpidemic;

public class Population {

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
			if (maxWealth < data.getWealth()) {
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
		return newPop;
	}

	void addPop(Population pop) {
		getScience().mergeFrom(pop.getScience(), (double) getAmount() / (getAmount() + pop.getAmount()));
		getCulture().mergeFrom(pop.getCulture(), (double) getAmount() / (getAmount() + pop.getAmount()));
		setAmount(getAmount() + pop.getAmount());
		setWealth(getWealth() + pop.getWealth());
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

	public static void addPopulationFromArmy(Province p, int soldiers) {
		double fraction = 1.0 * soldiers / p.getPopulationAmount();
		p.getPopulation().forEach(pop -> pop.setAmount((int) (pop.getAmount() + pop.getAmount() * fraction)));
	}

}
