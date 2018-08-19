package com.cwsni.world.model;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.model.data.DataArmy;
import com.cwsni.world.model.data.GameParams;

public class Army {

	private static final Log logger = LogFactory.getLog(Army.class);

	private DataArmy data;
	private Country country;
	private Province moveFrom;
	private boolean isCanMoveThisTurn = true;
	private boolean isCanFightThisTurn = true;

	public int getId() {
		return data.getId();
	}

	void setId(int armyId) {
		data.setId(armyId);
	}

	public Country getCountry() {
		return country;
	}

	public int getSoldiers() {
		return data.getSoldiers();
	}

	public void setSoldiers(int soldiers) {
		data.setSoldiers(soldiers);
	}

	public int getTraining() {
		return data.getTraining();
	}

	public void setTraining(int training) {
		data.setTraining(training);
	}

	public int getOrganisation() {
		return data.getOrganisation();
	}

	public void setOrganisation(int organisation) {
		data.setOrganisation(organisation);
	}

	public int getEquipment() {
		return data.getEquipment();
	}

	public void setEquipment(int equipment) {
		data.setEquipment(equipment);
	}

	public Province getLocation() {
		return country.getGame().getMap().findProvById(getLocationId());
	}

	public Integer getLocationId() {
		return data.getProvince();
	}

	public void setProvince(Province province) {
		Province p = getLocation();
		if (p != null) {
			p.removeArmy(this);
		}
		if (province != null) {
			data.setProvince(province.getId());
			province.addArmy(this);
		} else {
			data.setProvince(null);
		}
	}

	DataArmy getDataArmy() {
		return data;
	}

	public void buildFrom(Country country, DataArmy da) {
		this.country = country;
		this.data = da;
	}

	@Override
	public int hashCode() {
		return data.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Army)) {
			return false;
		}
		return ((Army) obj).getId() == getId();
	}

	boolean isCanFightThisTurn() {
		return isCanFightThisTurn;
	}

	private void changeOrganization(int delta) {
		data.setOrganisation(Math.min(100, data.getOrganisation() + delta));
	}

	private void changeTraining(int delta) {
		data.setTraining(Math.max(50, Math.min(200, data.getTraining() + delta)));
	}

	private void changeSoldiers(int delta) {
		data.setSoldiers(Math.max(0, data.getSoldiers() + delta));
	}

	private double getEffectiveness() {
		return 1.0 * data.getOrganisation() / 100 * data.getTraining() / 100;
	}

	public double getStrength() {
		return 1.0 * getEffectiveness() * data.getSoldiers();
	}

	public void dismiss() {
		setProvince(null);
		dismissToCapital(getSoldiers());
	}

	public void dismissSoldiers(int howManySoldiersNeedToDismiss) {
		setSoldiers(getSoldiers() - howManySoldiersNeedToDismiss);
		dismissToCapital(howManySoldiersNeedToDismiss);
	}

	private void dismissToCapital(int soldiers) {
		// TODO pops should return to home or stay in current province
		Province capital = getCountry().getCapital();
		if (capital != null) {
			capital.addPopulationFromArmy(soldiers);
		}
	}

	public void moveTo(Province destination) {
		if (isCanMoveThisTurn) {
			if (!getLocation().getNeighbors().contains(destination)) {
				// destination is not neighbor
				// TODO find nearest province to move
				// right now command has only neighbors target
				return;
			}
			moveFrom = getLocation();
			changeOrganization(-2);
			setProvince(destination);
			isCanMoveThisTurn = false;
		} else {
			logger.warn("someone is trying to move army a few times per turn");
		}
	}

	private void processEffectivenessInNewTurn() {
		changeOrganization(20);
		changeTraining(-1);
	}

	public void processNewTurn() {
		isCanMoveThisTurn = true;
		isCanFightThisTurn = true;
		moveFrom = null;
		processEffectivenessInNewTurn();
		Province currentProv = getLocation();
		Country locationCountry = currentProv.getCountry();
		if (getCountry().equals(locationCountry)) {
			// our land, doing nothing
			return;
		}
		// it is our land now! Probably... we need to check
		if (currentProv.getTerrainType().isPopulationPossible()) {
			long totalSoldiers = currentProv.getArmies().stream()
					.filter(a -> ComparisonTool.isEqual(a.getCountry().getId(), getCountry().getId()))
					.mapToLong(a -> a.getSoldiers()).sum();
			double successfulInvasion = totalSoldiers
					/ (currentProv.getPopulationAmount() * getCountry().getArmySoldiersToPopulationForSubjugation());
			currentProv.sufferFromInvasion(getSoldiers(), successfulInvasion);
			if (successfulInvasion >= 1) {
				if (locationCountry != null) {
					locationCountry.removeProvince(currentProv);
				}
				getCountry().addProvince(currentProv);
			}
		}
	}

	private void retreat() {
		Province retreatTo = moveFrom;
		if (retreatTo == null || !ComparisonTool.isEqual(retreatTo.getCountryId(), getCountry().getId())) {
			// TODO path to capital
		}
		if (retreatTo == null) {
			// nowhere to go... prepare to fight again or to die
			return;
		}
		setProvince(retreatTo);
		isCanFightThisTurn = false;
	}

	public double getCostPerYear() {
		return getSoldiers() * getCostForSoldierPerYear();
	}

	public double getCostForSoldierPerYear() {
		return country.getGame().getGameParams().getBudgetBaseCostPerSoldier();
	}

	/**
	 * Check if army is still able to work. If not, it will be dismissed.
	 * 
	 * @return
	 */
	public boolean isAbleToWork() {
		GameParams gParams = getCountry().getGame().getGameParams();
		return getSoldiers() >= gParams.getArmyMinAllowedSoldiers()
				&& getOrganisation() >= gParams.getArmyMinAllowedOrganization();
	}

	// ---------------------------- static -----------------------------

	public static double fight(List<Army> attacker, List<Army> defender) {
		Game game = attacker.get(0).getCountry().getGame();
		double attackerStrenth = attacker.stream().mapToDouble(a -> a.getStrength()).sum();
		double defenderStrenth = defender.stream().mapToDouble(a -> a.getStrength()).sum();
		double ras = attackerStrenth
				* (1 + game.getGameParams().getArmyFightRandomness() * game.getGameParams().getRandom().nextDouble());
		double das = defenderStrenth
				* (1 + game.getGameParams().getArmyFightRandomness() * game.getGameParams().getRandom().nextDouble());
		double result = ras / das;
		if (result > 1) {
			processFightResult(game, attacker, defender, result);
		} else {
			processFightResult(game, defender, attacker, 1 / result);
		}
		return result;
	}

	private static void processFightResult(Game game, List<Army> winner, List<Army> loser, double result) {
		winner.forEach(a -> {
			a.changeOrganization((int) Math.round(-10 / result));
			a.changeTraining(5);
			a.changeSoldiers((int) (-game.getGameParams().getArmyFightBasePercentOfLoss() * a.getSoldiers() / result));
		});
		loser.forEach(a -> {
			a.changeOrganization((int) Math.round(-10 * result));
			a.changeTraining(5);
			a.changeSoldiers((int) (-game.getGameParams().getArmyFightBasePercentOfLoss() * a.getSoldiers() * result));
		});
		loser.forEach(a -> a.retreat());
	}

}
