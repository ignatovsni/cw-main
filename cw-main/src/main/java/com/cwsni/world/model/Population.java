package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.cwsni.world.CwException;
import com.cwsni.world.model.events.Event;

public class Population {

	private int amount;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	static public void migrate(Province from, GameParams gParams) {
		List<Province> prov = from.getNeighbors().stream()
				.filter(n -> n.getTerrainType().isPopulationPossible() && n.getPopulationExcess() < 1)
				.collect(Collectors.toList());
		if (prov.size() == 0) {
			return;
		}
		int mPops = 0;
		int maxPopulation = from.getMaxPopulation();
		if (from.getPopulationAmount() > maxPopulation || from.getSoilFertilityEff() < 1) {
			mPops = (int) (from.getPopulationAmount() * (gParams.getPopulationMaxExcess() - 1) / 2);
		} else {
			mPops = (int) (from.getPopulationAmount() * gParams.getPopulationMinMigration());
		}
		if (mPops > prov.size()) {
			int mPopsToEachNeighbor = mPops / prov.size();
			prov.forEach(p -> migrateTo(from, mPopsToEachNeighbor, p));
		}
	}

	static private void migrateTo(Province from, int popAmount, Province to) {
		Population newPop = extractNewPop(from, popAmount);
		if (to.getPopulation().size() == 0) {
			to.getPopulation().add(newPop);
		} else {
			Population pop = to.getPopulation().get(0);
			pop.setAmount(pop.getAmount() + newPop.getAmount());
		}
	}

	static private Population extractNewPop(Province from, int popAmount) {
		int totalAmount = from.getPopulation().stream().mapToInt(p -> p.getAmount()).sum();
		if (popAmount > totalAmount) {
			throw new CwException("popAmount > totalAmount : prov.id = " + from.getId());
		}
		double needMultiplyTo = 1 - (double) popAmount / totalAmount;
		from.getPopulation().forEach(p -> p.setAmount((int) (p.getAmount() * needMultiplyTo)));
		Population pop = new Population();
		pop.setAmount(popAmount);
		return pop;
	}

	static public void growPopulation(Province from, Game game) {
		GameParams gParams = game.getGameParams();
		if (from.getSoilFertilityEff() >= 1) {
			from.getPopulation().forEach(p -> {
				p.setAmount((int) (p.getAmount() * gParams.getPopulationBaseGrowth()));
			});
		} else {
			dieFromHunger(game, from, 1 - from.getSoilFertilityEff());
		}
		int maxPopulation = Math.max(from.getMaxPopulation(), 1);
		if (from.getPopulationAmount() > maxPopulation * gParams.getPopulationMaxExcess()) {
			double needDivideTo = (double) from.getPopulationAmount() / maxPopulation
					/ gParams.getPopulationMaxExcess();
			double multi = 1 / needDivideTo;
			dieFromHunger(game, from, 1 - multi);
		}
	}

	public static void processEvents(Game game, Province p) {
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
}
