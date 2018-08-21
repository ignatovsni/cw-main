package com.cwsni.world.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.CwException;
import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.data.DataArmy;
import com.cwsni.world.model.data.DataCountry;
import com.cwsni.world.model.data.DataMoneyBudget;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataScienceBudget;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.util.CwRandom;

public class Country {

	private static final Log logger = LogFactory.getLog(Country.class);

	private DataCountry data;
	private Game game;
	private Collection<Province> provinces;
	private Collection<Army> armies;
	private MoneyBudget budget;
	private ScienceBudget scienceBudget;

	public void buildFrom(Game game, DataCountry dc) {
		this.game = game;
		this.data = dc;
		armies = new HashSet<>();
		provinces = new HashSet<>();
		budget = new MoneyBudget();
		scienceBudget = new ScienceBudget();

		data.getProvinces().forEach(pId -> {
			Province province = game.getMap().findProvById(pId);
			province.setCountry(this);
			provinces.add(province);
		});

		dc.getArmies().forEach(da -> {
			Army a = new Army();
			a.buildFrom(this, da);
			armies.add(a);
			game.registerArmy(a);
		});

		// budget must be initialized last to calculate actual numbers
		budget.buildFrom(this, dc.getBudget());
		scienceBudget.buildFrom(this, dc.getScienceBudget());
	}

	public int getId() {
		return data.getId();
	}

	public String getName() {
		return data.getName();
	}

	public Color getColor() {
		return data.getColor();
	}

	public Game getGame() {
		return game;
	}

	public boolean isAI() {
		return data.isAI();
	}

	public void setAI(boolean isAI) {
		data.setAI(isAI);
	}

	public Province getCapital() {
		return game.getMap().findProvById(data.getCapital());
	}

	public void setCapital(Province province) {
		if (province == null) {
			data.setCapital(null);
		} else {
			if (this.equals(province.getCountry())) {
				data.setCapital(province.getId());
			} else {
				throw new CwException("Trying to set up capital in alien province: province country id = "
						+ province.getCountryId() + " but country.id = " + getId());
			}
		}
	}

	public Integer getCapitalId() {
		return data.getCapital();
	}

	public Integer getFirstCapitalId() {
		return data.getFirstCapital();
	}

	public void setFirstCapital(Province capital) {
		data.setFirstCapital(capital != null ? capital.getId() : null);
	}

	public double getMoney() {
		return budget.getMoney();
	}

	public double getIncome() {
		return budget.getIncome();
	}

	public double getFocus() {
		return data.getFocus();
	}

	public void setFocus(double focus) {
		data.setFocus(focus);
	}

	DataCountry getCountryData() {
		return data;
	}

	public MoneyBudget getMoneyBudget() {
		return budget;
	}

	public ScienceBudget getScienceBudget() {
		return scienceBudget;
	}

	public Collection<Army> getArmies() {
		return Collections.unmodifiableCollection(armies);
	}

	private void registerArmy(Army a) {
		data.getArmies().add(a.getDataArmy());
		armies.add(a);
		game.registerArmy(a);
	}

	private void unregisterArmy(Army a) {
		data.getArmies().remove(a.getDataArmy());
		armies.remove(a);
		game.unregisterArmy(a);
	}

	public void addProvince(Province p) {
		if (p.getCountry() != null) {
			p.getCountry().removeProvince(p);
		}
		if (p.getTerrainType().isPopulationPossible()) {
			p.setCountry(this);
			provinces.add(p);
			data.getProvinces().add(p.getId());
		}
	}

	public void removeProvince(Province p) {
		p.setCountry(null);
		provinces.remove(p);
		data.getProvinces().remove(p.getId());
		if (ComparisonTool.isEqual(getCapitalId(), p.getId())) {
			setCapital(null);
		}
	}

	public Collection<Province> getProvinces() {
		return Collections.unmodifiableCollection(provinces);
	}

	@Override
	public int hashCode() {
		return data.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Country)) {
			return false;
		}
		return ((Country) obj).getId() == getId();
	}

	public void dismiss() {
		getProvinces().forEach(p -> p.setCountry(null));
		List<Army> listArmies = new LinkedList<>(getArmies());
		listArmies.forEach(a -> {
			dismissArmy(a);
		});
	}

	public void dismissArmy(Army a) {
		a.dismiss();
		unregisterArmy(a);
	}

	private Army createArmy() {
		Army a = new Army();
		a.buildFrom(this, new DataArmy(game.nextArmyId()));
		a.setEquipment(1);
		a.setOrganisation(100);
		a.setTraining(50);
		return a;
	}

	public Army createArmy(Province p, int soldiers) {
		if (!this.equals(p.getCountry())) {
			logger.error("destination country id = " + p.getCountryId() + " but country.id = " + getId());
			return null;
		}
		GameParams gParams = game.getGameParams();
		double baseHiringCostPerSoldier = gParams.getBudgetBaseHiringCostPerSoldier();
		if (soldiers * baseHiringCostPerSoldier > budget.getMoney()) {
			soldiers = (int) (budget.getMoney() / baseHiringCostPerSoldier);
		}
		if (soldiers < gParams.getArmyMinAllowedSoldiers()) {
			logger.warn("soldiers <= gParams.getArmyMinAllowedSoldiers() ; " + soldiers + " < "
					+ gParams.getArmyMinAllowedSoldiers());
			return null;
		}
		Army a = createArmy();
		p.hirePeopleForArmy(a, soldiers);
		if (a.getSoldiers() > 0) {
			budget.spendMoneyForArmy(a.getSoldiers() * baseHiringCostPerSoldier);
			registerArmy(a);
			a.setProvince(p);
			return a;
		} else {
			return null;
		}
	}

	public Army splitArmy(Army army, int soldiers) {
		if (soldiers >= army.getSoldiers()) {
			logger.warn("soldiers >= army.getSoldiers() ; " + soldiers + " >= " + army.getSoldiers());
			return null;
		}
		GameParams gParams = game.getGameParams();
		if (soldiers < gParams.getArmyMinAllowedSoldiers()) {
			logger.warn("soldiers <= gParams.getArmyMinAllowedSoldiers() ; " + soldiers + " < "
					+ gParams.getArmyMinAllowedSoldiers());
			return null;
		}
		if ((army.getSoldiers() - soldiers) < gParams.getArmyMinAllowedSoldiers()) {
			logger.warn("(army.getSoldiers()-soldiers) <= gParams.getArmyMinAllowedSoldiers() ; "
					+ (army.getSoldiers() - soldiers) + " < " + gParams.getArmyMinAllowedSoldiers());
			return null;
		}
		Army a = createArmy();
		a.setSoldiers(soldiers);
		army.setSoldiers(army.getSoldiers() - soldiers);
		registerArmy(a);
		a.setProvince(army.getLocation());
		return a;
	}

	public void mergeArmy(Army army, Army armyFrom, int soldiers) {
		soldiers = Math.min(soldiers, armyFrom.getSoldiers());
		army.setSoldiers(army.getSoldiers() + soldiers);
		armyFrom.setSoldiers(armyFrom.getSoldiers() - soldiers);
		if (armyFrom.getSoldiers() <= 0) {
			dismissArmy(armyFrom);
		}
	}

	public void processNewTurn() {
		budget.processNewTurn();
		processScienceNewTurn();
	}

	private void processScienceNewTurn() {
		Province capital = getCapital();
		if (capital != null) {
			capital.spendMoneyForScience(budget.getAvailableMoneyForScience());
		}
	}

	public String getAiScriptName() {
		return data.getAiScriptName();
	}

	public void setAiScriptName(String aiScriptName) {
		data.setAiScriptName(aiScriptName);
	}

	public void setName(String name) {
		data.setName(name);
	}

	public double getArmySoldiersToPopulationForSubjugation() {
		// it can depend on country science development
		return game.getGameParams().getArmySoldiersToPopulationForSubjugation();
	}

	// --------------------- static -------------------------------

	public static void createNewCountry(Game game, Province p) {
		if (p.getCountry() != null) {
			return;
		}
		DataCountry dc = new DataCountry();
		dc.setId(game.nextCountryId());
		dc.setName("#" + String.valueOf(dc.getId()));
		dc.setColor(createNewColorForCountry(game));
		dc.setBudget(new DataMoneyBudget());
		dc.setScienceBudget(new DataScienceBudget());
		Country c = new Country();
		c.buildFrom(game, dc);
		c.addProvince(p);
		c.setCapital(p);
		c.setFirstCapital(p);
		c.setFocus(100);
		c.getCapital().addCountryLoyalty(c.getId(), DataPopulation.LOYALTY_MAX);
		game.registerCountry(c);

		// randomize parameters
		dc.getBudget().setProvinceTax(game.getGameParams().getRandom().nextDouble());
		dc.getBudget().setArmyWeight(game.getGameParams().getRandom().nextDouble());
		dc.getBudget().setScienceWeight(game.getGameParams().getRandom().nextDouble());
		dc.getBudget().setSavingWeight(game.getGameParams().getRandom().nextDouble());
		dc.getScienceBudget().setAdministrationWeight(game.getGameParams().getRandom().nextDouble());
		dc.getScienceBudget().setAgricultureWeight(game.getGameParams().getRandom().nextDouble());
		dc.getScienceBudget().setMedicineWeight(game.getGameParams().getRandom().nextDouble());
	}

	private static Color createNewColorForCountry(Game game) {
		CwRandom random = game.getGameParams().getRandom();
		int minValue = 50;
		int minDiff = 50;
		Color color = null;
		while (color == null) {
			int r = minValue + random.nextInt(255 - minValue);
			int g = minValue + random.nextInt(255 - minValue);
			int b = minValue + random.nextInt(255 - minValue);
			// TODO match with colors of others countries
			if (Math.abs(r - g) >= minDiff && Math.abs(r - b) >= minDiff && Math.abs(b - g) >= minDiff) {
				color = new Color(r, g, b);
			}
		}
		return color;
	}

}
