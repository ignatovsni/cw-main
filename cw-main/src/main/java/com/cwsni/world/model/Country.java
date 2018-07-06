package com.cwsni.world.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.data.DataArmy;
import com.cwsni.world.model.data.DataCountry;
import com.cwsni.world.model.data.DataMoneyBudget;
import com.cwsni.world.util.CwRandom;
import com.cwsni.world.util.Heap;

public class Country {

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

	void dismissArmy(Army a) {
		a.dismiss();
		unregisterArmy(a);
	}

	private Army createArmy(Province p) {
		Army a = new Army();
		a.buildFrom(this, new DataArmy(game.nextArmyId()));
		a.setSoldiers(1000);
		a.setEquipment(1);
		a.setOrganisation(100);
		a.setTraining(50);
		registerArmy(a);
		a.setProvince(p);
		return a;
	}

	public void processNewTurn() {
		budget.processNewTurn();
		manageArmyWithBudget(budget.getAvailableMoneyForArmy());

	}

	private void manageArmyWithBudget(double availableMoneyForArmy) {
		// TODO it should be in AIHandler and must generate commands!!!
		if (availableMoneyForArmy < 0 && !getArmies().isEmpty()) {
			// we spend all money for existing armies
			// try to dismiss some
			// можно просто отсортировать массив, а не использовать heap
			Heap<Army> armiesByValues = new Heap<>((x, y) -> x.getSoldiers() - y.getSoldiers());
			getArmies().forEach(a -> armiesByValues.put(a));
			Army weakestArmy = armiesByValues.poll();
			while (availableMoneyForArmy < 0 && !getArmies().isEmpty()) {
				double costForSoldier = weakestArmy.getCostForSoldierPerYear();
				double howManySoldiersNeedToDismiss = -availableMoneyForArmy / costForSoldier;
				if (howManySoldiersNeedToDismiss >= weakestArmy.getSoldiers()) {
					availableMoneyForArmy += weakestArmy.getCostPerYear();
					dismissArmy(weakestArmy);
					weakestArmy = armiesByValues.poll();
				} else {
					weakestArmy.dismissSoldiers((int) howManySoldiersNeedToDismiss);
					availableMoneyForArmy = 0; // += howManySoldiersNeedToDismiss * costForSoldier;
				}
			}
		}

		double baseCostPerSoldier = game.getGameParams().getBudgetBaseCostPerSoldier();
		if (availableMoneyForArmy > baseCostPerSoldier * 100 && getCapital() != null) {
			Army a = createArmy(getCapital());
			a.setSoldiers((int) Math.max(1000, Math.min(100, availableMoneyForArmy / baseCostPerSoldier)));
			// TODO spend money for hiring
		}

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
