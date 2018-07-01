package com.cwsni.world.game.ai;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.player.PArmy;
import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.PGame;
import com.cwsni.world.model.player.PProvince;
import com.cwsni.world.services.algorithms.GameAlgorithms;

@Component
public class AIHandler {

	@Autowired
	private GameAlgorithms gameAlgorithms;

	public void processNewTurn(List<PGame> pGames) {
		pGames.forEach(pg -> processCountry(pg, pg.getCountry()));
	}

	private void processCountry(PGame game, PCountry c) {
		AIData4Country data = game.getAIData();
		data.initNewTurn(game, c);
		processArmies(data);
	}

	private void processArmies(AIData4Country data) {
		List<PArmy> armies = data.getCountry().getArmies();
		if (armies.isEmpty()) {
			return;
		}
		armies.forEach(a -> processArmy(data, a));
	}

	private void processArmy(AIData4Country data, PArmy a) {
		if (!ComparisonTool.isEqual(a.getCountry().getId(), a.getLocation().getCountryId())) {
			// stay here
			return;
		}
		if (tryMovingArmyToNeighbors(data, a)) {
			return;
		}
		tryMovingArmyFurther(data, a);
	}

	private void tryMovingArmyFurther(AIData4Country data, PArmy a) {
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
			List<Object> path = gameAlgorithms.findShortestPath(new PProvinceNodeWrapper(a.getLocation()),
					new PProvinceNodeWrapper(nearestProv));
			a.moveTo(path);
		}
	}

	private boolean tryMovingArmyToNeighbors(AIData4Country data, PArmy a) {
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

	private double calculateImportanceOfProvince(AIData4Country data, PProvince neighbor, PCountry pCountry) {
		PProvince capital = pCountry.getCapital();
		if (capital == null) {
			capital = pCountry.getFirstCapital();
		}
		return 1 / Math.max(data.getGame().relativeDistance(neighbor, capital), 1);
		//return calculateImportanceOfProvinceByCountOfNeighborsPops(data, neighbor, pCountry.getId());
	}

	private double calculateImportanceOfProvinceByCountOfNeighborsPops(AIData4Country data, PProvince p,
			Integer countryId) {
		return p.getNeighbors().stream().filter(pn -> countryId.equals(pn.getCountryId()))
				.mapToLong(pn -> pn.getPopulationAmount()).sum();
	}

}
