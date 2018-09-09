package com.cwsni.world.model.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.data.DataCountry;
import com.cwsni.world.model.data.DataMoneyBudget;
import com.cwsni.world.model.data.DataPopulation;
import com.cwsni.world.model.data.DataScienceBudget;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.data.HistoryDataCountry;
import com.cwsni.world.model.engine.modifiers.CountryModifier;
import com.cwsni.world.model.engine.modifiers.ModifierCollection;
import com.cwsni.world.util.ComparisonTool;
import com.cwsni.world.util.CwException;
import com.cwsni.world.util.CwRandom;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Country {

	private static final Log logger = LogFactory.getLog(Country.class);

	private DataCountry data;
	private Game game;
	private Collection<Province> provinces;
	private Collection<Army> armies;
	private MoneyBudget budget;
	private ScienceBudget scienceBudget;
	private CountryFocus focus;
	private ModifierCollection<CountryModifier> modifiers;

	// ------------- cache -----------------
	private long populationAmount;
	private int waterMaxDistance;
	private boolean isNeedRefreshReachableLandBorderAlienProvs = true;
	private boolean isNeedRefreshReachableProvincesThroughWater = true;
	private Set<Province> coastProvinces;
	private Set<Province> reachableWaterProvinces;
	// ------------- end of cache -----------------

	// -------------- player section -----------
	// Probably some of below variables should be in PCountry. But PGame (and all
	// PCountry) is creating for each country, so they can require many
	// recalculations.
	private Set<Province> reachableLandBorderAlienProvs;
	private Set<Province> reachableLandProvincesThroughWater;
	// -------------- end of player section -----------

	public void buildFrom(Game game, DataCountry dc) {
		this.game = game;
		this.data = dc;
		armies = new HashSet<>();
		provinces = new HashSet<>();
		budget = new MoneyBudget();
		scienceBudget = new ScienceBudget();
		focus = new CountryFocus();
		reachableLandBorderAlienProvs = new HashSet<>();
		reachableLandProvincesThroughWater = new HashSet<>();
		reachableWaterProvinces = new HashSet<>();
		coastProvinces = new HashSet<>();
		modifiers = new ModifierCollection<>();

		focus.buildFrom(this, dc.getFocus());

		data.getProvinces().forEach(pId -> {
			Province province = game.getMap().findProvinceById(pId);
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
		refreshPopulation();
		refreshWaterMaxDistance();
		refreshLandReachableBorderAlienProvs();
		refreshListOfReachableProvincesThroughWater();
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
		return game.getMap().findProvinceById(data.getCapital());
	}

	public void setCapital(Province province) {
		if (province == null) {
			data.setCapital(null);
		} else {
			if (this.equals(province.getCountry())) {
				data.setCapital(province.getId());
				if (data.getFirstCapital() == null) {
					data.setFirstCapital(data.getCapital());
				}
			} else {
				throw new CwException("Trying to set up capital in alien province: province country id = "
						+ province.getCountryId() + " but country.id = " + getId());
			}
		}
	}

	public ModifierCollection<CountryModifier> getModifiers() {
		return modifiers;
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
		return budget.getIncomePerYear();
	}

	public CountryFocus getFocus() {
		return focus;
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

	public Map<Object, Object> getAiRecords() {
		return data.getAiRecords();
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
		if (provinces.contains(p)) {
			return;
		}
		if (p.getTerrainType().isPopulationPossible()) {
			if (p.getCountry() != null) {
				p.getCountry().removeProvince(p);
			}
			p.setCountry(this);
			provinces.add(p);
			data.getProvinces().add(p.getId());
			isNeedRefreshReachableLandBorderAlienProvs = true;
			if (p.hasWaterNeighbor()) {
				isNeedRefreshReachableProvincesThroughWater = true;
			}
		}
	}

	public void removeProvince(Province p) {
		if (!provinces.contains(p)) {
			return;
		}
		p.setCountry(null);
		provinces.remove(p);
		data.getProvinces().remove(p.getId());
		if (ComparisonTool.isEqual(getCapitalId(), p.getId())) {
			setCapital(null);
		}
		isNeedRefreshReachableLandBorderAlienProvs = true;
		if (p.hasWaterNeighbor()) {
			isNeedRefreshReachableProvincesThroughWater = true;
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

	@Override
	public String toString() {
		return "Country with id " + getId();
	}

	public void dismiss() {
		new ArrayList<>(getProvinces()).forEach(p -> removeProvince(p));
		new ArrayList<>(getArmies()).forEach(a -> dismissArmy(a));
	}

	public void dismissArmy(Army a) {
		a.dismiss();
		unregisterArmy(a);
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
		Army a = Army.createArmy(this);
		p.recruitPeopleForArmy(a, soldiers);
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
		Army a = army.split(soldiers);
		if (a != null) {
			registerArmy(a);
			a.setProvince(army.getLocation());
		}
		return a;
	}

	public void mergeArmy(Army army, Army armyFrom, int soldiers) {
		army.mergeFrom(armyFrom, soldiers);
		if (armyFrom.getSoldiers() <= 0) {
			dismissArmy(armyFrom);
		}
	}

	protected void calculateBaseBudget() {
		budget.calculateBaseBudget();
	}

	protected void calculateBudgetWithAgreements() {
		budget.calculateBudgetWithAgreements();
	}

	public void processNewTurn() {
		data.setTurnsOfExistence(data.getTurnsOfExistence() + game.getTurn().getLastStep());
		budget.processNewTurn();
		processScienceNewTurn();
		refreshWaterMaxDistance();
		if (isNeedRefreshReachableLandBorderAlienProvs) {
			refreshLandReachableBorderAlienProvs();
			isNeedRefreshReachableLandBorderAlienProvs = false;
		}
		if (isNeedRefreshReachableProvincesThroughWater) {
			refreshListOfReachableProvincesThroughWater();
			isNeedRefreshReachableProvincesThroughWater = false;
		}
		refreshPopulation();
		double multiplyPerYear = game.getTurn()
				.multiplyPerYear(game.getGameParams().getPopulationCasualtiesCoeffPerYear());
		data.setCasualties(data.getCasualties() * multiplyPerYear);
		data.setRebelAddChances(data.getRebelAddChances() * multiplyPerYear);
	}

	private void refreshLandReachableBorderAlienProvs() {
		reachableLandBorderAlienProvs.clear();
		coastProvinces.clear();
		getProvinces().forEach(p -> {
			if (p.hasWaterNeighbor()) {
				coastProvinces.add(p);
			}
			p.getNeighbors().stream()
					.filter(n -> !n.getTerrainType().isWater() && !ComparisonTool.isEqual(n.getCountryId(), getId()))
					.forEach(n -> reachableLandBorderAlienProvs.add(n));
		});

	}

	private void refreshWaterMaxDistance() {
		int newWaterMaxDistance = game.getScienceModificators().getMaxWaterDistance(this);
		if (newWaterMaxDistance != waterMaxDistance) {
			waterMaxDistance = newWaterMaxDistance;
			isNeedRefreshReachableProvincesThroughWater = true;
		}
	}

	private void refreshListOfReachableProvincesThroughWater() {
		reachableLandProvincesThroughWater.clear();
		reachableWaterProvinces.clear();
		if (waterMaxDistance == 0) {
			return;
		}
		List<Province> waterProvs = new ArrayList<>();
		coastProvinces.stream().forEach(p -> {
			p.getNeighbors().stream().filter(n -> n.getTerrainType().isWater()).forEach(n -> {
				waterProvs.add(n);
				reachableWaterProvinces.add(n);
			});
		});
		int step = 0;
		int idx = 0;
		while (step++ <= waterMaxDistance) {
			int final_idx = waterProvs.size();
			for (int i = idx; i < final_idx; i++) {
				Province prov = waterProvs.get(i);
				for (Province n : prov.getNeighbors()) {
					if (n.getTerrainType().isWater()) {
						if (step < waterMaxDistance && !reachableWaterProvinces.contains(n)) {
							waterProvs.add(n);
							reachableWaterProvinces.add(n);
						}
					} else {
						reachableLandProvincesThroughWater.add(n);
					}
				}
			}
			idx = final_idx;
		}
	}

	private void processScienceNewTurn() {
		Province capital = getCapital();
		if (capital != null) {
			double money = budget.getAvailableMoneyForScience();
			capital.spendMoneyForScience(money);
			budget.spendMoneyForScience(money);
		}
	}

	public String getAiScriptName() {
		return data.getAiScriptName();
	}

	public Set<Province> getReachableLandProvincesThroughWater() {
		return reachableLandProvincesThroughWater;
	}

	public Set<Province> getReachableWaterProvinces() {
		return reachableWaterProvinces;
	}

	public Set<Province> getReachableLandBorderAlienProvs() {
		return reachableLandBorderAlienProvs;
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

	public double getArmySoldiersToPopulationForSubjugationLeaveInProvince() {
		// it can depend on country science development
		return game.getGameParams().getArmySoldiersToPopulationForSubjugationLeaveInProvince();
	}

	private void refreshPopulation() {
		populationAmount = getProvinces().stream().mapToLong(p -> p.getPopulationAmount()).sum();
	}

	public long getPopulationAmount() {
		if (populationAmount == 0) {
			refreshPopulation();
		}
		return populationAmount;
	}

	public long getAvailablePeopleForRecruiting() {
		return getProvinces().stream().mapToLong(p -> p.getAvailablePeopleForRecruiting()).sum();
	}

	public long getArmiesSoldiers() {
		return getArmies().stream().mapToLong(a -> a.getSoldiers()).sum();
	}

	public double getCasualties() {
		return data.getCasualties();
	}

	protected void addCasualties(long delta) {
		if (delta < 0) {
			System.out.println("country addCasualties: delta < 0, delta=" + delta);
		}
		data.setCasualties(data.getCasualties() + delta);
	}

	public double getLoyaltyToCountryFromCountryCasualties() {
		long populationAmount = getPopulationAmount();
		if (populationAmount == 0) {
			return 0;
		}
		double casualties = getCasualties();
		return -Math.min(getGame().getGameParams().getPopulationCasualtiesGlobalLoyaltyMaxSuffer(),
				casualties / populationAmount);
	}

	public void checkAndCleanAiRecords() {
		boolean isNeedToClean = false;
		String msgAboutClean = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			// objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			String txt = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getAiRecords());
			if (txt != null && txt.length() > game.getGameParams().getAiRecordMaxTextSize()) {
				isNeedToClean = true;
				msgAboutClean = "Country " + getId() + " had text aiRecors.size=" + txt.length()
						+ ", it is more than allowed size (" + game.getGameParams().getAiRecordMaxTextSize()
						+ "), so aiRecors was deleted";
			} else {
				objectMapper.readValue(txt, Map.class);
			}
		} catch (Exception e) {
			isNeedToClean = true;
			msgAboutClean = "Country " + getId()
					+ " could not save aiRecords as a text, so aiRecors was deleted. Error message: " + e.getMessage();
			logger.trace(msgAboutClean, e);
		}
		if (isNeedToClean) {
			getAiRecords().clear();
			logger.warn(msgAboutClean);
		}
	}

	protected double getRebelAddChances() {
		return data.getRebelAddChances();
	}

	protected void setRebelAddChances(double rebelAddChances) {
		data.setRebelAddChances(rebelAddChances);
	}

	// --------------------- static -------------------------------

	public static Country createNewCountry(Game game, Province p) {
		if (p.getCountry() != null) {
			return null;
		}
		DataCountry dc = createDefaultDataCountry(game);
		dc.setId(game.nextCountryId());
		dc.setName("#" + String.valueOf(dc.getId()));

		Country c = new Country();
		c.buildFrom(game, dc);
		c.addProvince(p);
		c.setCapital(p);
		c.setFirstCapital(p);
		c.getCapital().addLoyaltyToCountry(c.getId(), DataPopulation.LOYALTY_MAX);
		game.registerCountry(c);

		return c;
	}

	public static Country restoreCountry(Game game, HistoryDataCountry hdc) {
		if (hdc == null) {
			return null;
		}
		if (game.findCountryById(hdc.getId()) != null) {
			// country is active
			return null;
		}
		DataCountry dc = createDefaultDataCountry(game);
		hdc.copyTo(dc);

		Country c = new Country();
		c.buildFrom(game, dc);
		game.registerCountry(c);
		return c;
	}

	public static Country createNewCountryForRebelState(Game game, Province stateCapital) {
		stateCapital.getCountry().removeProvince(stateCapital);
		return createNewCountry(game, stateCapital);
	}

	private static DataCountry createDefaultDataCountry(Game game) {
		DataCountry dc = new DataCountry();
		dc.setColor(createNewColorForCountry(game));
		dc.setTurnOfCreation(game.getTurn().getDateTurn());
		dc.setFocus(CountryFocus.createFocusForNewCountry(game));
		dc.setBudget(new DataMoneyBudget());
		dc.setScienceBudget(new DataScienceBudget());
		// randomize parameters
		dc.getBudget().setProvinceTax(game.getGameParams().getRandom().nextDouble());
		dc.getBudget().setArmyWeight(game.getGameParams().getRandom().nextDouble());
		dc.getBudget().setScienceWeight(game.getGameParams().getRandom().nextDouble());
		dc.getBudget().setSavingWeight(game.getGameParams().getRandom().nextDouble());
		dc.getScienceBudget().setAdministrationWeight(game.getGameParams().getRandom().nextDouble());
		dc.getScienceBudget().setAgricultureWeight(game.getGameParams().getRandom().nextDouble());
		dc.getScienceBudget().setMedicineWeight(game.getGameParams().getRandom().nextDouble());
		return dc;
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
