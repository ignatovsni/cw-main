package com.cwsni.world.game.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPBudget;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPGameParams;
import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.util.Heap;

@Component
@Qualifier("javaAIHandler")
public class JavaAIHandler implements IAIHandler {

	@Override
	public void processCountry(AIData4Country data) {
		checkCapital(data);
		processArmyBudget(data);
		mergeAndSplitArmies(data);
		moveArmies(data);
	}

	public void checkCapital(AIData4Country data) {
		IPCountry country = data.getCountry();
		IPProvince capital = country.getCapital();
		if (capital == null) {
			int maxPop = -1;
			IPProvince candidate = null;
			for (IPProvince p : country.getProvinces()) {
				int popAmount = p.getPopulationAmount();
				if (popAmount > maxPop) {
					maxPop = popAmount;
					candidate = p;
				}
			}
			if (candidate != null) {
				country.setCapital(candidate);
			}
		}
	}

	public void processArmyBudget(AIData4Country data) {
		IPGameParams params = data.getGame().getGameParams();
		IPBudget budget = data.getCountry().getBudget();
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
				double costForSoldier = weakestArmy.getCostForSoldierPerYear();
				double howManySoldiersNeedToDismiss = -availableMoneyForArmy / costForSoldier;
				if (howManySoldiersNeedToDismiss >= weakestArmy.getSoldiers()) {
					availableMoneyForArmy += weakestArmy.getCostPerYear();
					weakestArmy.dismiss();
					weakestArmy = armiesByValues.poll();
				} else {
					weakestArmy.dismissSoldiers((int) howManySoldiersNeedToDismiss);
					availableMoneyForArmy = 0; // += howManySoldiersNeedToDismiss * costForSoldier;
				}
			}
		}

		if (armies.size() >= 10) {
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
			if (armiesInProv.size() < 2) {
				continue;
			}
			List<IPProvince> alienNeighbors = army.getLocation().getNeighbors().stream()
					.filter(n -> n.getTerrainType().isPopulationPossible() && !n.isMyProvince())
					.collect(Collectors.toList());
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
			// TODO split
			processed.addAll(armiesInProv);
		}
	}

	public void moveArmies(AIData4Country data) {
		Collection<IPArmy> armies = data.getCountry().getArmies();
		if (armies.isEmpty()) {
			return;
		}
		armies.stream().forEach(a -> moveArmy(data, a));
	}

	private void moveArmy(AIData4Country data, IPArmy army) {
		if (!army.isCanMove()) {
			return;
		}
		if (!ComparisonTool.isEqual(army.getCountry().getId(), army.getLocation().getCountryId())
				&& !army.getLocation().getTerrainType().isWater()) {
			// alien province, stay here
			return;
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
			a.moveTo(p);
		}
		return true;
	}

	private double calculateImportanceOfProvince(AIData4Country data, IPProvince neighbor, IPCountry pCountry) {
		IPProvince capital = pCountry.getCapital();
		if (capital == null) {
			capital = pCountry.getFirstCapital();
		}
		return 1 / Math.max(data.getGame().relativeDistance(neighbor, capital), 1);
		// return calculateImportanceOfProvinceByCountOfNeighborsPops(data, neighbor,
		// pCountry.getId());
	}

	private double calculateImportanceOfProvinceByCountOfNeighborsPops(AIData4Country data, IPProvince p,
			Integer countryId) {
		return p.getNeighbors().stream().filter(pn -> countryId.equals(pn.getCountryId()))
				.mapToLong(pn -> pn.getPopulationAmount()).sum();
	}

	private void tryMovingArmyFurther(AIData4Country data, IPArmy a) {
		IPProvince nearestProv = null;
		double minDistance = Double.MAX_VALUE;
		for (IPProvince p : data.getCountry().getNeighborsProvs()) {
			double distance = data.getGame().relativeDistance(a.getLocation(), p);
			if (distance < minDistance) {
				minDistance = distance;
				nearestProv = p;
			}
		}
		if (nearestProv != null) {
			List<Object> path = data.getGame().findShortestPath(a.getLocation().getId(), nearestProv.getId());
			a.moveTo(path);
		}
	}

}
