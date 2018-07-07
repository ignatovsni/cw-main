package com.cwsni.world.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.data.DataArmy;
import com.cwsni.world.model.data.DataCountry;
import com.cwsni.world.model.data.DataMoneyBudget;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.util.CwRandom;

public class Country {

	private static final Log logger = LogFactory.getLog(Country.class);

	private DataCountry data;
	private Game game;
	private Collection<Province> provinces;
	private Collection<Army> armies;
	private MoneyBudget budget;

	public void buildFrom(Game game, DataCountry dc) {
		this.game = game;
		this.data = dc;
		armies = new HashSet<>();
		provinces = new HashSet<>();
		budget = new MoneyBudget();
		budget.buildFrom(this, dc.getBudget());

		game.getMap().getProvinces().stream().filter(p -> p.getCountryId() != null && p.getCountryId() == data.getId())
				.forEach(p -> provinces.add(game.getMap().findProvById(p.getId())));

		dc.getArmies().forEach(da -> {
			Army a = new Army();
			a.buildFrom(this, da);
			armies.add(a);
			game.registerArmy(a);
		});
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

	public void setCapital(Province capital) {
		data.setCapital(capital != null ? capital.getId() : null);
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

	public double getFocus() {
		return data.getFocus();
	}

	public void setFocus(double focus) {
		data.setFocus(focus);
	}

	DataCountry getCountryData() {
		return data;
	}

	public MoneyBudget getBudget() {
		return budget;
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
		if (p.getTerrainType().isPopulationPossible()) {
			p.setCountry(this);
			provinces.add(p);
		}
	}

	public void removeProvince(Province p) {
		p.setCountry(null);
		provinces.remove(p);
		if (p.equals(getCapital())) {
			chooseNewCapital();
		}
	}

	private void chooseNewCapital() {
		int maxPop = -1;
		Province candidate = null;
		for (Province p : provinces) {
			int popAmount = p.getPopulationAmount();
			if (popAmount > maxPop) {
				maxPop = popAmount;
				candidate = p;
			}
		}
		setCapital(candidate);
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

	public Army createArmy(Province p, int soldiers) {
		if (!ComparisonTool.isEqual(p.getCountryId(), getId())) {
			logger.error("destination country id = " + p.getCountryId() + " but country.id = " + getId());
			return null;
		}
		GameParams gParams = game.getGameParams();
		double baseHiringCostPerSoldier = gParams.getBudgetBaseHiringCostPerSoldier();
		// TODO it is better to take money from military savings, not common savings...
		if (soldiers * baseHiringCostPerSoldier > budget.getMoney()) {
			soldiers = (int) (budget.getMoney() / baseHiringCostPerSoldier);
		}
		if (soldiers < gParams.getArmyMinAllowedSoldiers()) {
			logger.warn("soldiers <= gParams.getArmyMinAllowedSoldiers() ; " + soldiers + " < "
					+ gParams.getArmyMinAllowedSoldiers());
			return null;
		}
		budget.spendMoneyForArmy(soldiers * baseHiringCostPerSoldier);
		Army a = new Army();
		a.buildFrom(this, new DataArmy(game.nextArmyId()));
		// TODO hire people from population
		a.setSoldiers(soldiers);
		a.setEquipment(1);
		a.setOrganisation(100);
		a.setTraining(50);
		registerArmy(a);
		a.setProvince(p);
		return a;
	}

	public void processNewTurn() {
		budget.processNewTurn();
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
		Country c = new Country();
		c.buildFrom(game, dc);
		c.addProvince(p);
		c.setCapital(p);
		c.setFirstCapital(p);
		c.setFocus(100);
		game.registerCountry(c);
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
