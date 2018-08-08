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
	processArmyBudget(data);
	processArmies(data);
}

def processArmyBudget(AIData4Country data) {
	PGameParams params = data.getGame().getParams();
	PBudget budget = data.getCountry().getBudget();
	double availableMoneyForArmy = budget.getAvailableMoneyForArmy();
	List<PArmy> armies = data.getCountry().getArmies();
	if (availableMoneyForArmy < 0 && !armies.isEmpty()) {
		// we spend all money for existing armies
		// try to dismiss some
		Heap<PArmy> armiesByValues = new Heap<>( {x, y -> x.getSoldiers() - y.getSoldiers() });
		armies.forEach({a -> armiesByValues.put(a)});
		PArmy weakestArmy = armiesByValues.poll();
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

	List<PProvince> provinces = data.getCountry().getProvinces();
	if (canAllowNewSoldiers >= params.getArmyMinAllowedSoldiers() && !provinces.isEmpty()) {
		Heap<PProvince> provsBySoldiers = new Heap<>({x, y -> y.getPopulationAmount() - x.getPopulationAmount()});
		provinces.forEach({p -> provsBySoldiers.put(p)});
		PProvince provForHiring = provsBySoldiers.poll();
		data.getCountry().createArmy(provForHiring.getId(), (int) canAllowNewSoldiers);
		// data.getCountry().createArmy(provForHiring.getId(), (int) Math.min(1000 +
		// Math.sqrt(canAllowNewSoldiers), canAllowNewSoldiers));
	}
}

def processArmies(AIData4Country data) {
	List<PArmy> armies = data.getCountry().getArmies();
	if (armies.isEmpty()) {
		return;
	}
	armies.stream().filter({a -> a.isAbleToWork()}).forEach({a -> processArmy(data, a)});
}

def processArmy(AIData4Country data, PArmy a) {
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

def tryMovingArmyFurther(AIData4Country data, PArmy a) {
	PProvince nearestProv = null;
	double minDistance = Double.MAX_VALUE;
	for (PProvince p : data.getCountry().getNeighborsProvs()) {
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

def tryMovingArmyToNeighbors(AIData4Country data, PArmy a) {
	PProvince target = null;
	double maxWeight = -1;
	Integer armyCountryId = a.getCountry().getId();
	for (PProvince neighbor : a.getLocation().getNeighbors()) {
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

def calculateImportanceOfProvince(AIData4Country data, PProvince neighbor, PCountry pCountry) {
	PProvince capital = pCountry.getCapital();
	if (capital == null) {
		capital = pCountry.getFirstCapital();
	}
	return 1 / Math.max(data.getGame().relativeDistance(neighbor, capital), 1);
	// return calculateImportanceOfProvinceByCountOfNeighborsPops(data, neighbor,
	// pCountry.getId());
}

def calculateImportanceOfProvinceByCountOfNeighborsPops(AIData4Country data, PProvince p,
		Integer countryId) {
	return p.getNeighbors().stream().filter({pn -> countryId.equals(pn.getCountryId())})
			.mapToLong({pn -> pn.getPopulationAmount()}).sum();
}

