package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.cwsni.world.CwException;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.events.Event;

public class Population {

	private DataPopulation data;
	private ScienceCollection science;

	public Population() {
		data = new DataPopulation();
		science = new ScienceCollection(data.getScience());
	}

	public int getAmount() {
		return data.getAmount();
	}

	public void setAmount(int amount) {
		data.setAmount(amount);
	}

	public ScienceCollection getScience() {
		return science;
	}

	public void buildFrom(DataPopulation dpop) {
		this.data = dpop;
		this.science = new ScienceCollection();
		science.buildFrom(dpop.getScience());
	}

	private Population createNewEmigrant(int migrantsCount) {
		Population newPop = new Population();
		newPop.setAmount(migrantsCount);
		setAmount(getAmount() - migrantsCount);
		newPop.getScience().cloneFrom(getScience());
		return newPop;
	}

	private void addPop(Population pop) {
		getScience().mergeFrom(pop.getScience(), (double) getAmount() / (getAmount() + pop.getAmount()));
		setAmount(getAmount() + pop.getAmount());
	}

	// --------------- static section -----------------------

	static public void migrate(Province from, Game game) {
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
			mPops = (int) (from.getPopulationAmount() * (gParams.getPopulationMaxExcess() - 1) / 2);
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
				to.getImmigrants().add(fromPop.createNewEmigrant(migrantsCount));
			}
		});
	}

	static public void growPopulation(Province from, Game game) {
		GameParams gParams = game.getGameParams();
		if (from.getSoilFertility() >= 1) {
			from.getPopulation().forEach(p -> {
				p.setAmount((int) (p.getAmount() * (1 + gParams.getPopulationBaseGrowth())));
			});
		} else {
			dieFromHunger(game, from, 1 - from.getSoilFertility());
		}
		int maxPopulation = Math.max(from.getMaxPopulation(), 1);
		if (from.getPopulationAmount() > maxPopulation * gParams.getPopulationMaxExcess()) {
			double needDivideTo = (double) from.getPopulationAmount() / maxPopulation
					/ gParams.getPopulationMaxExcess();
			double multi = 1 / needDivideTo;
			dieFromHunger(game, from, 1 - multi);
		}
	}

	public static void processEvents(Province p, Game game) {
		List<Event> provEvents = new ArrayList<>(p.getEvents().getEvents());
		provEvents.forEach(e -> Event.processEvent(game, p, e));
	}

	private static void dieFromHunger(Game game, Province from, double fraction) {
		double kf = Math.min(fraction / 5, 0.1);
		game.getGameStats().addDiedFromHunger(from.getPopulationAmount() * kf);
		from.getPopulation().forEach(p -> {
			p.setAmount((int) (p.getAmount() * (1 - kf)));
		});
	}

	public static void dieFromDisease(Game game, Province from, double deathRate) {
		game.getGameStats().addDiedFromDisease(from.getPopulationAmount() * deathRate);
		from.getPopulation().forEach(p -> {
			p.setAmount((int) (p.getAmount() * (1 - deathRate)));
		});
	}

	public static void processImmigrantsAndMergePops(Province p, Game game) {
		if (!p.getImmigrants().isEmpty()) {
			p.getPopulation().addAll(p.getImmigrants());
			p.getImmigrants().clear();
		}
		if (p.getPopulation().size() <= 1) {
			return;
		}
		Population toPop = p.getPopulation().get(0);
		for (int i = 1; i < p.getPopulation().size(); i++) {
			toPop.addPop(p.getPopulation().get(i));
		}
		p.getPopulation().clear();
		p.getPopulation().add(toPop);

	}

	DataPopulation getPopulationData() {
		return data;
	}

}
