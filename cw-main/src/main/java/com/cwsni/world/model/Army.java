package com.cwsni.world.model;

import java.util.List;

import com.cwsni.world.model.data.DataArmy;
import com.cwsni.world.model.data.GameParams;

public class Army {

	private DataArmy data;
	private Country country;
	private Province moveFrom;
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
		// TODO pops should return to home or stay in current province
		setProvince(null);
	}

	public void dismissSoldiers(int howManySoldiersNeedToDismiss) {
		setSoldiers(getSoldiers() - howManySoldiersNeedToDismiss);
	}

	public void moveTo(Province destination) {
		moveFrom = getLocation();
		changeOrganization(-2);
		setProvince(destination);
	}

	private void processEffectivenessInNewTurn() {
		changeOrganization(20);
		changeTraining(-1);
	}

	public void processNewTurn() {
		isCanFightThisTurn = true;
		moveFrom = null;
		processEffectivenessInNewTurn();
		Province currentProv = getLocation();
		Country locationCountry = currentProv.getCountry();
		if (getCountry().equals(locationCountry)) {
			// our land, doing nothing
			return;
		}
		// it is our land now!
		if (locationCountry != null) {
			currentProv.sufferFromInvading();
			locationCountry.removeProvince(currentProv);
		}
		getCountry().addProvince(currentProv);
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
