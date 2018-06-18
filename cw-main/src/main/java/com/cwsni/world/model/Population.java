package com.cwsni.world.model;

import java.util.List;
import java.util.stream.Collectors;

import com.cwsni.world.CwException;

public class Population {

	private int amount;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	static public void migrateIfHaveTo(Province from, GameParams gParams) {
		int maxPopulation = Math.max(from.getMaxPopulation(), 1);
		if (from.getPopulationAmount() > maxPopulation || from.getSoilFertilityEff() < 1) {
			List<Province> prov = from.getNeighbors().stream()
					.filter(n -> n.getTerrainType().isPopulationPossible() && n.getPopulationExcess() < 1)
					.collect(Collectors.toList());
			if (prov.size() > 0) {
				int mPops = (int) (from.getPopulationAmount() * (gParams.getPopulationMaxExcess() - 1) / 2);
				int mPopsToEachNeighbor = mPops / prov.size();
				prov.forEach(p -> migrateTo(from, mPopsToEachNeighbor, p));
			}
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

	static public void growPopulation(Province from, GameParams gParams) {
		if (from.getSoilFertilityEff() >= 1) {
			from.getPopulation().forEach(p -> {
				p.setAmount((int) (p.getAmount() * gParams.getPopulationBaseGrowth()));
			});
		} else {
			from.getPopulation().forEach(p -> {
				p.setAmount((int) (p.getAmount() * from.getSoilFertilityEff()));
			});
		}
		int maxPopulation = Math.max(from.getMaxPopulation(), 1);
		if (from.getPopulationAmount() > maxPopulation * gParams.getPopulationMaxExcess()) {
			double needDivideTo = (double) from.getPopulationAmount() / maxPopulation
					/ gParams.getPopulationMaxExcess();
			from.getPopulation().forEach(p -> {
				p.setAmount((int) (p.getAmount() / needDivideTo));
			});
		}
	}
}
