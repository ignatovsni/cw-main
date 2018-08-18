def processCountry(AIData4Country data) {
	//processCountryWithScript(data);
	processCountryWithJava(data);
}

// ------------------------------------------------
def processCountryWithJava(AIData4Country data) {
	data.javaAIHandler.processCountry(data);
}
// ------------------------------------------------

def processCountryWithScript(AIData4Country data) {
	checkCapital(data);
	processArmyBudget(data);
	processArmies(data);
}

def checkCapital(AIData4Country data) {
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

def processArmyBudget(AIData4Country data) {
	IPGameParams params = data.getGame().getGameParams();
	IPBudget budget = data.getCountry().getBudget();
	double availableMoneyForArmy = budget.getAvailableMoneyForArmy();
	List<IPArmy> armies = data.getCountry().getArmies();
	if (availableMoneyForArmy < 0 && !armies.isEmpty()) {
		// we spend all money for existing armies
		// try to dismiss some
		Heap<IPArmy> armiesByValues = new Heap<>( {x, y -> x.getSoldiers() - y.getSoldiers() });
		armies.forEach({a -> armiesByValues.put(a)});
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
	if (armies.size() >= 20) {
		return;
	}	
	// new armies
	double baseHiringCostPerSoldier = params.getBudgetBaseHiringCostPerSoldier();
	double baseCostPerSoldier = params.getBudgetBaseCostPerSoldier();
	double canAllowNewSoldiers = Math.min(availableMoneyForArmy / baseCostPerSoldier,
			budget.getMoney() / baseHiringCostPerSoldier);

	List<IPProvince> provinces = data.getCountry().getProvinces();
	if (canAllowNewSoldiers >= params.getArmyMinAllowedSoldiers() && !provinces.isEmpty()) {
		Heap<IPProvince> provsBySoldiers = new Heap<>({x, y -> y.getPopulationAmount() - x.getPopulationAmount()});
		provinces.forEach({p -> provsBySoldiers.put(p)});
		IPProvince provForHiring = provsBySoldiers.poll();
		data.getCountry().createArmy(provForHiring.getId(), (int) canAllowNewSoldiers);
		// data.getCountry().createArmy(provForHiring.getId(), (int) Math.min(1000 +
		// Math.sqrt(canAllowNewSoldiers), canAllowNewSoldiers));
	}
}

def processArmies(AIData4Country data) {
	List<IPArmy> armies = data.getCountry().getArmies();
	if (armies.isEmpty()) {
		return;
	}
	armies.stream().forEach({a -> processArmy(data, a)});
}

def processArmy(AIData4Country data, IPArmy a) {
	if (!ComparisonTool.isEqual(a.getCountry().getId(), a.getLocation().getCountryId())
			&& !a.getLocation().getTerrainType().isWater()) {
		// alien province, stay here
		return;
	}
	if (tryMovingArmyToNeighbors(data, a)) {
		return;
	}
	tryMovingArmyFurther(data, a);
}

def tryMovingArmyFurther(AIData4Country data, IPArmy a) {
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

def tryMovingArmyToNeighbors(AIData4Country data, IPArmy a) {
	IPProvince target = null;
	double maxWeight = -1;
	Integer armyCountryId = a.getCountry().getId();
	for (IPProvince neighbor : a.getLocation().getNeighbors()) {
		if (neighbor.getTerrainType().isPopulationPossible() && !armyCountryId.equals(neighbor.getCountryId())) {
			double weight = calculateImportanceOfProvince(data, neighbor, a.getCountry());
			if (weight > maxWeight) {
				maxWeight = weight;
				target = neighbor;
			}
		}
	}
	if (target != null) {
		a.moveTo(target);
		return true;
	} else {
		return false;
	}
}

def calculateImportanceOfProvince(AIData4Country data, IPProvince neighbor, IPCountry pCountry) {
	IPProvince capital = pCountry.getCapital();
	if (capital == null) {
		capital = pCountry.getFirstCapital();
	}
	return 1 / Math.max(data.getGame().relativeDistance(neighbor, capital), 1);
	// return calculateImportanceOfProvinceByCountOfNeighborsPops(data, neighbor,
	// pCountry.getId());
}

def calculateImportanceOfProvinceByCountOfNeighborsPops(AIData4Country data, IPProvince p,
		Integer countryId) {
	return p.getNeighbors().stream().filter({pn -> countryId.equals(pn.getCountryId())})
			.mapToLong({pn -> pn.getPopulationAmount()}).sum();
}

