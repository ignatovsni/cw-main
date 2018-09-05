package com.cwsni.world.game.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cwsni.world.model.player.SimplePair;
import com.cwsni.world.model.player.interfaces.IData4Country;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPGame;
import com.cwsni.world.model.player.interfaces.IPGameParams;
import com.cwsni.world.model.player.interfaces.IPMoneyBudget;
import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.model.player.interfaces.IPRTribute;
import com.cwsni.world.model.player.interfaces.IPRTruce;
import com.cwsni.world.model.player.interfaces.IPRWar;
import com.cwsni.world.model.player.interfaces.IPRandom;
import com.cwsni.world.model.player.interfaces.IPScienceBudget;
import com.cwsni.world.util.ComparisonTool;
import com.cwsni.world.util.Heap;

@Component
public class JavaAIHandler {

	private static final int MAX_ARMIES = 100;

	public void processCountry(IData4Country data) {
		checkCapital(data);
		manageMoneyBudget(data);
		manageScienceBudget(data);
		manageDiplomacy(data);
		processArmyBudget(data);
		mergeAndSplitArmies(data);
		moveArmies(data);
	}

	public void manageMoneyBudget(IData4Country data) {
		IPMoneyBudget budget = data.getCountry().getMoneyBudget();
		if (data.isWar()) {
			budget.setProvinceTax(0.6);
			budget.setArmyWeight(3);
			budget.setScienceWeight(1);
			budget.setSavingWeight(1);
		} else {
			budget.setProvinceTax(0.3);
			budget.setArmyWeight(1);
			budget.setScienceWeight(1);
			budget.setSavingWeight(1);
		}
	}

	public void manageScienceBudget(IData4Country data) {
		IPScienceBudget budget = data.getCountry().getScienceBudget();
		budget.setAdministrationWeight(1);
		budget.setAgricultureWeight(2);
		budget.setMedicineWeight(1);
	}

	private void manageDiplomacy(IData4Country data) {
		int maxDesiredWars = 5;
		Map<Integer, Double> countriesCurrentWarStrength = getCurrentWarStrengthForCountries(data);
		IPCountry country = data.getCountry();
		double thisCountryPureWarStrength = getPureWarStrength(data, country);
		double thisCountryStrength = countriesCurrentWarStrength.get(country.getId());
		IPGame game = data.getGame();
		IPRandom rnd = data.getRandom();
		Map<Integer, IPRWar> countriesWithWar = game.getRelationships().getCountriesWithWar(country.getId());
		Map<Integer, IPRTruce> countriesWithTruce = game.getRelationships().getCountriesWithTruce(country.getId());
		Map<Integer, IPRTribute> countriesWithTribute = game.getRelationships()
				.getCountriesWithTribute(country.getId());
		double loyaltyToCountryFromCountryCasualties = country.getLoyaltyToCountryFromCountryCasualties();

		// check war
		if (loyaltyToCountryFromCountryCasualties > -0.05 && thisCountryStrength >= thisCountryPureWarStrength * 0.5
				&& 1.0 * countriesWithWar.size() / maxDesiredWars < rnd.nextDouble()) {
			Integer weakestEnemyCountryId = null;
			double weakestEnemyStrength = Double.MAX_VALUE;
			for (Entry<Integer, Double> e : countriesCurrentWarStrength.entrySet()) {
				Integer enemyCountryId = e.getKey();
				if (ComparisonTool.isEqual(enemyCountryId, country.getId())) {
					continue;
				}
				Double enemyStrength = e.getValue();
				if (enemyStrength < weakestEnemyStrength && !countriesWithWar.containsKey(enemyCountryId)
						&& !countriesWithTruce.containsKey(enemyCountryId)
						&& !countriesWithTribute.containsKey(enemyCountryId)) {
					weakestEnemyStrength = enemyStrength;
					weakestEnemyCountryId = enemyCountryId;
				}
			}
			if (weakestEnemyCountryId != null && thisCountryStrength > weakestEnemyStrength * 2) {
				game.getRelationships().declareWar(weakestEnemyCountryId);
			}
		}

		// check peace
		for (Entry<Integer, IPRWar> agreementWithId : countriesWithWar.entrySet()) {
			IPRWar war = agreementWithId.getValue();
			Integer enemyCountryId = agreementWithId.getKey();
			IPCountry enemyCountry = game.findCountryById(enemyCountryId);
			double enemyStrength = countriesCurrentWarStrength.get(enemyCountryId);
			boolean wantRegularPeace = false;
			boolean wantToBeMaster = false;
			boolean wantToBeSlave = false;
			if (thisCountryStrength > enemyStrength * 10) {
				// We want to crush them.
			} else if (thisCountryStrength > enemyStrength * 5 && enemyCountry.isAI()) {
				// We want to crush them. But we are gently with a human player.
				wantToBeMaster = true;
			} else if (thisCountryStrength > enemyStrength * 3) {
				wantToBeMaster = true;
			} else if (thisCountryStrength < enemyStrength / 3) {
				wantRegularPeace = true;
				wantToBeSlave = true;
			}
			if (game.getTurn().getYearsAfter(war.getStartTurn()) > 50
					|| loyaltyToCountryFromCountryCasualties < -0.15) {
				wantRegularPeace = true;
				wantToBeMaster = true;
			}
			if (wantRegularPeace || wantToBeMaster || wantToBeSlave) {
				game.getRelationships().makePeace(war, wantRegularPeace, wantToBeMaster, wantToBeSlave);
			}
		}

		// check tribute
		for (Entry<Integer, IPRTribute> agreementWithId : countriesWithTribute.entrySet()) {
			IPRTribute tribute = agreementWithId.getValue();
			Integer enemyCountryId = agreementWithId.getKey();
			IPCountry enemyCountry = game.findCountryById(enemyCountryId);
			double enemyStrength = countriesCurrentWarStrength.get(enemyCountryId);
			if (ComparisonTool.isEqual(tribute.getMasterId(), country.getId())) {
				// we are master
				if (countriesWithWar.size() < maxDesiredWars) {
					if (thisCountryStrength > enemyStrength * 10) {
						game.getRelationships().cancelTribute(tribute);
					} else if (thisCountryStrength > enemyStrength * 5 && enemyCountry.isAI()) {
						game.getRelationships().cancelTribute(tribute);
					}
				}
			} else {
				// we are slave
				if (thisCountryStrength > enemyStrength * 0.5) {
					game.getRelationships().cancelTribute(tribute);
				}
			}
		}

	}

	private Map<Integer, Double> getCurrentWarStrengthForCountries(IData4Country data) {
		Map<Integer, Double> pureWarStrength = new HashMap<>();
		// this country
		pureWarStrength.put(data.getCountry().getId(), 0.0);
		// wars
		data.getGame().getRelationships().getCountriesWithWar(data.getCountry().getId()).keySet()
				.forEach(cId -> pureWarStrength.put(cId, 0.0));
		// tributes
		data.getGame().getRelationships().getCountriesWithTribute(data.getCountry().getId()).keySet()
				.forEach(cId -> pureWarStrength.put(cId, 0.0));
		// land neighbors
		data.getCountry().getReachableLandBorderAlienProvs().stream().filter(p -> p.getCountryId() != null)
				.forEach(p -> pureWarStrength.put(p.getCountryId(), 0.0));
		// water neighbors
		data.getCountry().getReachableLandProvincesThroughWater().stream().filter(p -> p.getCountryId() != null)
				.forEach(p -> pureWarStrength.put(p.getCountryId(), 0.0));
		// calculate pure strength
		pureWarStrength.entrySet()
				.forEach(e -> e.setValue(getPureWarStrength(data, data.getGame().findCountryById(e.getKey()))));
		// calculate strength taking in account current wars
		Map<Integer, Double> countriesCurrentWarStrength = new HashMap<>();
		for (Entry<Integer, Double> e : pureWarStrength.entrySet()) {
			Integer countryId = e.getKey();
			Double currentStrength = e.getValue();
			Set<Integer> warWith = data.getGame().getRelationships().getCountriesWithWar(countryId).keySet();
			for (Integer cId : warWith) {
				Double enemyPureStrength = pureWarStrength.get(cId);
				if (enemyPureStrength == null) {
					enemyPureStrength = getPureWarStrength(data, data.getGame().findCountryById(cId));
				}
				currentStrength -= enemyPureStrength * 0.1;
			}
			countriesCurrentWarStrength.put(countryId, currentStrength);
		}
		return countriesCurrentWarStrength;
	}

	private double getPureWarStrength(IData4Country data, IPCountry country) {
		return country.getFocusLevel() * country.getPopulationAmount();
	}

	public void checkCapital(IData4Country data) {
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

	public void processArmyBudget(IData4Country data) {
		Collection<IPArmy> armies = data.getCountry().getArmies();
		IPGameParams params = data.getGame().getGameParams();
		IPMoneyBudget budget = data.getCountry().getMoneyBudget();
		double armyBudget = budget.getIncomePerYear() * budget.getArmyWeight() / budget.getTotalWeight();
		double armyCost = armies.stream().mapToDouble(a -> a.getCostPerYear()).sum();
		double availableMoneyForArmy = armyBudget - armyCost;
		if (!data.isWar()) {
			availableMoneyForArmy += budget.getMoney() * 0.2;
		}

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
						weakestArmy.dismiss((int) howManySoldiersNeedToDismiss);
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
		Heap<IPProvince> provsBySoldiers = new Heap<>(
				(x, y) -> y.getAvailablePeopleForRecruiting() - x.getAvailablePeopleForRecruiting());
		provinces.forEach(p -> provsBySoldiers.put(p));
		while (armies.size() < MAX_ARMIES && canAllowNewSoldiers >= params.getArmyMinAllowedSoldiers() * 2
				&& !provsBySoldiers.isEmpty()) {
			IPProvince provForHiring = provsBySoldiers.poll();
			if (provForHiring.getAvailablePeopleForRecruiting() > params.getArmyMinAllowedSoldiers() * 2) {
				IPArmy army = data.getCountry().createArmy(provForHiring.getId(), (int) canAllowNewSoldiers);
				if (army != null) {
					canAllowNewSoldiers -= army.getSoldiers();
				}
			}
		}
	}

	public void mergeAndSplitArmies(IData4Country data) {
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
					.filter(n -> n.canBeSubjugatedByMe()).collect(Collectors.toList());
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

	public void moveArmies(IData4Country data) {
		Collection<IPArmy> armies = new ArrayList<>(data.getCountry().getArmies());
		if (armies.isEmpty()) {
			return;
		}
		armies.stream().forEach(a -> moveArmy(data, a));
	}

	private void moveArmy(IData4Country data, IPArmy army) {
		if (!army.isCanMove()) {
			return;
		}
		IPProvince location = army.getLocation();
		if (!ComparisonTool.isEqual(army.getCountry().getId(), location.getCountryId())
				&& !location.getTerrainType().isWater()) {
			if (location.getCountryId() == null && location.getPopulationAmount() == 0
					&& location.getSoilFertilityWithPopFromArmy(army) > 2.0) {
				// Colonize.
				army.dismiss(army.getSoldiers() / 2);
				return;
			} else if (isAbleToSubjugate(data, army, location)) {
				army.subjugateProvince();
				return;
			}
		}
		if (tryMovingArmyToNeighbors(data, army)) {
			return;
		}
		tryMovingArmyFurther(data, army);
	}

	private boolean tryMovingArmyToNeighbors(IData4Country data, IPArmy army) {
		Map<IPProvince, Double> importanceOfProvinces = new HashMap<>();
		for (IPProvince neighbor : army.getLocation().getNeighbors()) {
			if (neighbor.canBeSubjugatedByMe() && neighbor.isPassable(army)) {
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

	private boolean isAbleToSubjugate(IData4Country data, IPArmy army, IPProvince location) {
		if (!location.canBeSubjugatedByMe()) {
			return false;
		}
		return army.getSoldiers() > 1.05 * location.getPopulationAmount()
				* data.getCountry().getArmySoldiersToPopulationForSubjugation();
	}

	private double calculateImportanceOfProvince(IData4Country data, IPProvince neighbor, IPCountry pCountry) {
		IPProvince capital = pCountry.getCapital();
		if (capital == null) {
			capital = pCountry.getFirstCapital();
		}
		return 1 / Math.max(data.getGame().findDistance(neighbor, capital), 1);
	}

	private void tryMovingArmyFurther(IData4Country data, IPArmy a) {
		IPProvince capital = data.getCountry().getCapital();
		SimplePair<IPProvince, Double> nearestLandProv = findNearestPriorityProvince(data, capital, a,
				data.getCountry().getReachableLandBorderAlienProvs());
		SimplePair<IPProvince, Double> nearestLandProvThroughWater = findNearestPriorityProvince(data, capital, a,
				data.getCountry().getReachableLandAlienProvincesThroughWater());
		SimplePair<IPProvince, Double> nearestProv = nearestLandProv.b <= nearestLandProvThroughWater.b * 2
				? nearestLandProv
				: nearestLandProvThroughWater;
		if (nearestProv.a != null) {
			List<? extends Object> path = data.getGame().findShortestPath(a.getLocation().getId(),
					nearestProv.a.getId(), a);
			// path.size = 0 means that army can't reach target province
			// path.size = 1 means that path contains only from province
			if (path.size() > 1) {
				a.moveTo(path);
			}
		}
	}

	private SimplePair<IPProvince, Double> findNearestPriorityProvince(IData4Country data, IPProvince capital,
			IPArmy army, Set<IPProvince> provinces) {
		IPProvince armyLocation = army.getLocation();
		IPProvince nearestProv = null;
		double minDistance = Double.MAX_VALUE;
		for (IPProvince p : provinces) {
			if (p.equals(armyLocation) || !p.canBeSubjugatedByMe() || !p.isPassable(null)) {
				continue;
			}
			double distance = data.getGame().findDistance(armyLocation, p);
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
