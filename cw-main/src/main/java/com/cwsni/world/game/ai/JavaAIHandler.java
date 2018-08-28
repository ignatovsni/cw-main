package com.cwsni.world.game.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.player.SimplePair;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPGameParams;
import com.cwsni.world.model.player.interfaces.IPMoneyBudget;
import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.model.player.interfaces.IPScienceBudget;
import com.cwsni.world.util.Heap;

@Component
@Qualifier("javaAIHandler")
public class JavaAIHandler implements IAIHandler {

	private static final int MAX_ARMIES = 100;

	@Override
	public void processCountry(AIData4Country data) {
		checkCapital(data);
		manageMoneyBudget(data);
		manageScienceBudget(data);
		processArmyBudget(data);
		mergeAndSplitArmies(data);
		moveArmies(data);
	}

	public void manageMoneyBudget(AIData4Country data) {
		IPMoneyBudget budget = data.getCountry().getMoneyBudget();
		budget.setProvinceTax(0.5);
		budget.setArmyWeight(1);
		budget.setScienceWeight(1);
		budget.setSavingWeight(1);
	}

	public void manageScienceBudget(AIData4Country data) {
		IPScienceBudget budget = data.getCountry().getScienceBudget();
		budget.setAdministrationWeight(1);
		budget.setAgricultureWeight(2);
		budget.setMedicineWeight(1);
	}

	public void checkCapital(AIData4Country data) {
		IPCountry country = data.getCountry();
		IPProvince capital = country.getCapital();
		IPProvince firstCapital = country.getFirstCapital();
		if (firstCapital != null && !firstCapital.equals(capital)) {
			if (ComparisonTool.isEqual(country.getId(), firstCapital.getCountryId()) && firstCapital.getState() != null
					&& firstCapital.equals(firstCapital.getState().getCapital())) {
				country.setCapital(firstCapital);
				return;
			}
		}
		if (capital == null || capital.getPopulationAmount() == 0
				|| (capital.getState() != null && !capital.equals(capital.getState().getCapital()))) {
			int maxPop = -1;
			int maxPopStateCapital = -1;
			IPProvince candidate = null;
			IPProvince candidateStateCapital = null;
			for (IPProvince p : country.getProvinces()) {
				int popAmount = p.getPopulationAmount();
				if (popAmount > maxPop) {
					maxPop = popAmount;
					candidate = p;
				}
				if (popAmount > maxPopStateCapital) {
					maxPopStateCapital = popAmount;
					candidateStateCapital = p;
				}
			}
			if (candidateStateCapital != null && candidateStateCapital.getPopulationAmount() > 0) {
				country.setCapital(candidateStateCapital);
			} else if (candidate != null) {
				country.setCapital(candidate);
			}
		}
	}

	public void processArmyBudget(AIData4Country data) {
		IPGameParams params = data.getGame().getGameParams();
		IPMoneyBudget budget = data.getCountry().getMoneyBudget();
		double availableMoneyForArmy = budget.getAvailableMoneyForArmy();
		Collection<IPArmy> armies = data.getCountry().getArmies();
		if (availableMoneyForArmy < 0 && !armies.isEmpty()) {
			// we spend all money for existing armies
			// try to dismiss some
			// I can just sort array instead of using Heap
			Heap<IPArmy> armiesByValues = new Heap<>((x, y) -> x.getSoldiers() - y.getSoldiers());
			armies.forEach(a -> armiesByValues.put(a));
			IPArmy weakestArmy = armiesByValues.poll();
			while (availableMoneyForArmy < 0 && weakestArmy != null) {
				if (!weakestArmy.getLocation().getTerrainType().isWater()) {
					double costForSoldier = weakestArmy.getCostForSoldierPerYear();
					double howManySoldiersNeedToDismiss = -availableMoneyForArmy / costForSoldier;
					if (howManySoldiersNeedToDismiss >= weakestArmy.getSoldiers()) {
						availableMoneyForArmy += weakestArmy.getCostPerYear();
						weakestArmy.dismiss();
					} else {
						weakestArmy.dismissSoldiers((int) howManySoldiersNeedToDismiss);
						availableMoneyForArmy = 0; // += howManySoldiersNeedToDismiss * costForSoldier;
					}
				}
				weakestArmy = armiesByValues.poll();
			}
		}

		if (armies.size() >= MAX_ARMIES) {
			return;
		}
		// new armies
		double baseHiringCostPerSoldier = params.getBudgetBaseHiringCostPerSoldier();
		double baseCostPerSoldier = params.getBudgetBaseCostPerSoldier();
		double canAllowNewSoldiers = Math.min(availableMoneyForArmy / baseCostPerSoldier,
				budget.getMoney() / baseHiringCostPerSoldier);

		Collection<IPProvince> provinces = data.getCountry().getProvinces();
		if (canAllowNewSoldiers >= params.getArmyMinAllowedSoldiers() && !provinces.isEmpty()) {
			Heap<IPProvince> provsBySoldiers = new Heap<>((x, y) -> y.getPopulationAmount() - x.getPopulationAmount());
			provinces.forEach(p -> provsBySoldiers.put(p));
			IPProvince provForHiring = provsBySoldiers.poll();
			if (provForHiring.equals(data.getCountry().getCapital()) && !provsBySoldiers.isEmpty()) {
				// we don't want to hire in capital
				IPProvince anotherProv = provsBySoldiers.poll();
				if (anotherProv.getPopulationAmount() > params.getArmyMinAllowedSoldiers() * 2) {
					provForHiring = anotherProv;
				}
			}
			data.getCountry().createArmy(provForHiring.getId(), (int) canAllowNewSoldiers);
		}
	}

	public void mergeAndSplitArmies(AIData4Country data) {
		IPCountry country = data.getCountry();
		List<IPArmy> armies = new ArrayList<>(country.getArmies());
		Set<IPArmy> processed = new HashSet<>();
		for (IPArmy army : armies) {
			if (processed.contains(army)) {
				continue;
			}
			processed.add(army);
			IPProvince location = army.getLocation();
			if (location == null) {
				continue;
			}
			Collection<IPArmy> armiesInProv = country.findArmiesInProv(location);
			processed.addAll(armiesInProv);
			List<IPProvince> alienNeighbors = army.getLocation().getNeighbors().stream()
					.filter(n -> n.getTerrainType().isPopulationPossible() && !n.isMyProvince())
					.collect(Collectors.toList());
			if (armiesInProv.size() < alienNeighbors.size()) {
				// split
				int needMoreArmies = alienNeighbors.size() - armiesInProv.size();
				long totalSoldiers = armiesInProv.stream().mapToLong(a -> a.getSoldiers()).sum();
				int newArmySize = (int) Math.max(totalSoldiers / alienNeighbors.size(),
						data.getGame().getGameParams().getArmyMinAllowedSoldiers() * 2);
				long maxPopInAlienNeighbors = alienNeighbors.stream().mapToLong(p -> p.getPopulationAmount()).max()
						.getAsLong();
				int needMaxSoldiers = (int) (maxPopInAlienNeighbors
						* data.getCountry().getArmySoldiersToPopulationForSubjugation());
				newArmySize = Math.max(newArmySize, needMaxSoldiers);
				Optional<IPArmy> bigArmy = armiesInProv.stream().max((x, y) -> x.getSoldiers() - y.getSoldiers());
				while (country.getArmies().size() < MAX_ARMIES && needMoreArmies > 0
						&& bigArmy.get().getSoldiers() >= newArmySize * 2) {
					bigArmy.get().splitArmy(newArmySize);
					--needMoreArmies;
				}
			} else {
				// merge
				if (armiesInProv.size() < 2) {
					continue;
				}
				if (alienNeighbors.size() < 2) {
					for (IPArmy a : armiesInProv) {
						if (a != army) {
							army.merge(a);
						}
					}
				} else {
					Heap<IPArmy> weakArmies = new Heap<>((x, y) -> x.getSoldiers() - y.getSoldiers());
					armiesInProv.forEach(a -> weakArmies.put(a));
					while (weakArmies.size() > alienNeighbors.size()) {
						IPArmy weakestArmy1 = weakArmies.poll();
						IPArmy weakestArmy2 = weakArmies.poll();
						weakestArmy2.merge(weakestArmy1);
						weakArmies.put(weakestArmy2);
					}
				}
			}
		}
	}

	public void moveArmies(AIData4Country data) {
		Collection<IPArmy> armies = new ArrayList<>(data.getCountry().getArmies());
		if (armies.isEmpty()) {
			return;
		}
		armies.stream().forEach(a -> moveArmy(data, a));
	}

	private void moveArmy(AIData4Country data, IPArmy army) {
		if (!army.isCanMove()) {
			return;
		}
		IPProvince location = army.getLocation();
		if (!ComparisonTool.isEqual(army.getCountry().getId(), location.getCountryId())
				&& !location.getTerrainType().isWater()) {
			if (location.getPopulationAmount() == 0 && location.getSoilFertilityWithPopFromArmy(army) > 2.0) {
				// Colonize.
				army.dismiss();
				return;
			} else if (isAbleToSubjugate(data, army, location)) {
				// alien province and army is able to subjugate it, stay here
				return;
			}
		}
		if (tryMovingArmyToNeighbors(data, army)) {
			return;
		}
		tryMovingArmyFurther(data, army);
	}

	private boolean tryMovingArmyToNeighbors(AIData4Country data, IPArmy army) {
		Map<IPProvince, Double> importanceOfProvinces = new HashMap<>();
		for (IPProvince neighbor : army.getLocation().getNeighbors()) {
			if (neighbor.getTerrainType().isPopulationPossible() && !neighbor.isMyProvince()) {
				double weight = calculateImportanceOfProvince(data, neighbor, army.getCountry());
				importanceOfProvinces.put(neighbor, weight);
			}
		}
		if (importanceOfProvinces.isEmpty()) {
			return false;
		}
		Heap<IPProvince> importantProvinces = new Heap<>(false,
				(x, y) -> (importanceOfProvinces.get(x) - importanceOfProvinces.get(y)) > 0 ? 1 : -1);
		importanceOfProvinces.keySet().forEach(p -> importantProvinces.put(p));

		Collection<IPArmy> armiesInProv = data.getCountry().findArmiesInProv(army.getLocation());
		Heap<IPArmy> strongArmies = new Heap<>(false, (x, y) -> x.getStrength() - y.getStrength());
		armiesInProv.stream().filter(a -> a.isCanMove()).forEach(a -> strongArmies.put(a));

		while (!importantProvinces.isEmpty() && !strongArmies.isEmpty()) {
			IPProvince p = importantProvinces.poll();
			IPArmy a = strongArmies.poll();
			if (isAbleToSubjugate(data, a, p)) {
				a.moveTo(p);
			}
		}
		return !army.isCanMove();
	}

	private boolean isAbleToSubjugate(AIData4Country data, IPArmy army, IPProvince location) {
		return army.getSoldiers() > 1.05 * location.getPopulationAmount()
				* data.getCountry().getArmySoldiersToPopulationForSubjugation();
	}

	private double calculateImportanceOfProvince(AIData4Country data, IPProvince neighbor, IPCountry pCountry) {
		IPProvince capital = pCountry.getCapital();
		if (capital == null) {
			capital = pCountry.getFirstCapital();
		}
		return 1 / Math.max(data.getGame().findDistance(neighbor, capital), 1);
		// return calculateImportanceOfProvinceByCountOfNeighborsPops(data, neighbor,
		// pCountry.getId());
	}

	private void tryMovingArmyFurther(AIData4Country data, IPArmy a) {
		IPProvince capital = data.getCountry().getCapital();
		SimplePair<IPProvince, Double> nearestLandProv = findNearestProvince(data, capital, a.getLocation(),
				data.getCountry().getReachableLandBorderAlienProvs());
		SimplePair<IPProvince, Double> nearestLandProvThroughWater = findNearestProvince(data, capital, a.getLocation(),
				data.getCountry().getReachableLandAlienProvincesThroughWater());
		SimplePair<IPProvince, Double> nearestProv = nearestLandProv.b <= nearestLandProvThroughWater.b * 2
				? nearestLandProv
				: nearestLandProvThroughWater;
		if (nearestProv.a != null) {
			List<Object> path = data.getGame().findShortestPath(a.getLocation().getId(), nearestProv.a.getId(), a);
			// path.size = 0 means that army can't reach target province
			// path.size = 1 means that path contains only from province
			if (path.size() > 1) {
				a.moveTo(path);
			}
		}
	}

	private SimplePair<IPProvince, Double> findNearestProvince(AIData4Country data, IPProvince capital,
			IPProvince province, Set<IPProvince> provinces) {
		IPProvince nearestProv = null;
		double minDistance = Double.MAX_VALUE;
		for (IPProvince p : provinces) {
			if (p.equals(province)) {
				continue;
			}
			double distance = data.getGame().findDistance(province, p);
			if (capital != null && capital.getContinentId() != p.getContinentId()) {
				// Provinces are less important if they are at different continent.
				distance *= 2;
			}
			if (distance < minDistance) {
				minDistance = distance;
				nearestProv = p;
			}
		}
		return new SimplePair<IPProvince, Double>(nearestProv, minDistance);
	}

}
