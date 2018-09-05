package com.cwsni.world.model.engine;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.model.data.DataArmy;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.util.DataNormalizer;
import com.cwsni.world.util.ComparisonTool;

public class Army {

	private static final Log logger = LogFactory.getLog(Army.class);

	private DataArmy data;
	private Population population;
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
		return population.getAmount();
	}

	void setSoldiers(int soldiers) {
		population.setAmount(soldiers);
	}

	public double getTraining() {
		return data.getTraining();
	}

	public void setTraining(double training) {
		data.setTraining(training);
	}

	public double getOrganisation() {
		return data.getOrganisation();
	}

	public void setOrganisation(double organisation) {
		data.setOrganisation(organisation);
	}

	public double getEquipment() {
		return data.getEquipment();
	}

	public void setEquipment(double equipment) {
		data.setEquipment(equipment);
	}

	public Province getLocation() {
		return country.getGame().getMap().findProvById(getLocationId());
	}

	public Integer getLocationId() {
		return data.getProvince();
	}

	public double getScienceAgriculture() {
		return population.getScience().getAgriculture().getAmount();
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
		this.population = new Population(country.getGame());
		population.buildFrom(getLocation(), da.getPopulation());
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

	private void changeOrganization(double delta) {
		data.setOrganisation(DataNormalizer.minMax(data.getOrganisation() + delta, 0, 1));
	}

	private void changeTraining(double delta) {
		data.setTraining(DataNormalizer.minMax(data.getTraining() + delta, 0.5, 2));
	}

	private void changeSoldiers(int delta) {
		setSoldiers(Math.max(0, getSoldiers() + delta));
	}

	private void diedInBattle(int delta) {
		changeSoldiers(-delta);
		population.addCasualties(delta, country);
		country.getGame().getGameStats().addDiedInBattles(delta);
	}

	public double getEffectiveness() {
		return data.getOrganisation() * data.getTraining() * country.getFocus().getArmyStrengthInfluence();
	}

	public double getStrength() {
		return getEffectiveness() * getSoldiers();
	}

	public void dismiss() {
		getLocation().addPopulation(population);
		setProvince(null);
	}

	public void dismissSoldiers(int howManySoldiersNeedToDismiss) {
		getLocation().addPopulation(population.createNewPopFromThis(howManySoldiersNeedToDismiss));
	}

	public void moveTo(Province destination) {
		if (isCanMoveThisTurn) {
			if (!getLocation().getNeighbors().contains(destination)) {
				// destination is not neighbor
				// TODO find nearest province to move
				// right now command has only neighbors target
				return;
			}
			if (!new ProvincePassabilityCriteria(getCountry()).isPassable(destination)) {
				return;
			}
			moveFrom = getLocation();
			changeOrganization(getTurn().addPerWeek(-0.01));
			setProvince(destination);
			isCanMoveThisTurn = false;
		} else {
			logger.warn("someone is trying to move army a few times per turn");
		}
	}

	private void processEffectivenessInNewTurn() {
		changeOrganization(getTurn().addPerWeek(0.05));
		if (getTraining() < 1) {
			changeTraining(getTurn().addPerWeek(0.01));
			if (getTraining() > 1) {
				setTraining(1);
			}
		} else if (getTraining() > 1) {
			changeTraining(getTurn().addPerWeek(-0.02));
			if (getTraining() < 1) {
				setTraining(1);
			}
		}
	}

	protected Turn getTurn() {
		return country.getGame().getTurn();
	}

	public void processNewTurn() {
		isCanMoveThisTurn = true;
		isCanFightThisTurn = true;
		moveFrom = null;
		processEffectivenessInNewTurn();
		population.processNewTurnAsArmy();
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

	public void addPopulation(Population pop) {
		if (pop.getAmount() == 0) {
			population = pop;
		} else {
			population.addPop(pop);
		}
	}

	Army split(int soldiers) {
		if (soldiers >= getSoldiers()) {
			logger.warn("soldiers >= army.getSoldiers() ; " + soldiers + " >= " + getSoldiers());
			return null;
		}
		GameParams gParams = country.getGame().getGameParams();
		if (soldiers < gParams.getArmyMinAllowedSoldiers()) {
			logger.warn("soldiers <= gParams.getArmyMinAllowedSoldiers() ; " + soldiers + " < "
					+ gParams.getArmyMinAllowedSoldiers());
			return null;
		}
		if ((getSoldiers() - soldiers) < gParams.getArmyMinAllowedSoldiers()) {
			logger.warn("(army.getSoldiers()-soldiers) <= gParams.getArmyMinAllowedSoldiers() ; "
					+ (getSoldiers() - soldiers) + " < " + gParams.getArmyMinAllowedSoldiers());
			return null;
		}
		Army a = createArmy(country);
		a.addPopulation(population.createNewPopFromThis(soldiers));
		return a;
	}

	void mergeFrom(Army armyFrom, int soldiers) {
		if (soldiers >= armyFrom.getSoldiers()) {
			addPopulation(armyFrom.population);
			armyFrom.population = new Population(getCountry().getGame());
		} else {
			addPopulation(armyFrom.population.createNewPopFromThis(soldiers));
		}
	}

	public void subjugateProvince() {
		Province currentProv = getLocation();
		if (currentProv == null || !currentProv.getTerrainType().isPopulationPossible()) {
			return;
		}
		Country locationCountry = currentProv.getCountry();
		if (getCountry().equals(locationCountry) || locationCountry != null && !getGame().getRelationships()
				.getCountriesWithWar(country.getId()).containsKey(locationCountry.getId())) {
			return;
		}
		double totalSoldiers = currentProv.getArmies().stream()
				.filter(a -> ComparisonTool.isEqual(a.getCountry().getId(), getCountry().getId()))
				.mapToLong(a -> a.getSoldiers()).sum();
		double populationAmount = currentProv.getPopulationAmount();
		double successfulInvasion = totalSoldiers
				/ (populationAmount * getCountry().getArmySoldiersToPopulationForSubjugation());
		int soldiers = getSoldiers();
		currentProv.sufferFromInvasion(soldiers, successfulInvasion);
		if (successfulInvasion >= 1) {
			int needSoldiersToSubjugate = (int) (populationAmount
					* getGame().getGameParams().getArmySoldiersToPopulationForSubjugationLeaveInProvince());
			if (soldiers < needSoldiersToSubjugate) {
				return;
			}
			if (locationCountry != null) {
				locationCountry.removeProvince(currentProv);
			}
			getCountry().addProvince(currentProv);
			dismissSoldiers(needSoldiersToSubjugate);
		}
	}

	protected Game getGame() {
		return country.getGame();
	}

	// ---------------------------- static -----------------------------

	public static Army createArmy(Country country) {
		Army a = new Army();
		a.buildFrom(country, new DataArmy(country.getGame().nextArmyId()));
		a.setEquipment(1);
		a.setOrganisation(1);
		a.setTraining(0.5);
		return a;
	}

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
			a.changeOrganization((int) Math.round(-0.1 / result));
			a.changeTraining(0.05);
			int delta = (int) (game.getGameParams().getArmyFightBasePercentOfLoss() * a.getSoldiers() / result);
			a.diedInBattle(delta);
		});
		loser.forEach(a -> {
			a.changeOrganization((int) Math.round(-0.1 * result));
			a.changeTraining(0.05);
			int delta = (int) (game.getGameParams().getArmyFightBasePercentOfLoss() * a.getSoldiers() * result);
			a.diedInBattle(delta);
		});
		loser.forEach(a -> a.retreat());
	}

}
