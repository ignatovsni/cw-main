package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cwsni.world.game.commands.CommandArmyCreate;
import com.cwsni.world.game.commands.CommandProvinceSetCapital;
import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPMoneyBudget;
import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.model.player.interfaces.IPScienceBudget;

public class PCountry implements IPCountry {

	private Country country;
	private IPMoneyBudget moneyBudget;
	private PScienceBudget scienceBudget;
	private List<IPArmy> armies;
	private List<IPProvince> provinces;
	private PGame game;
	private Set<IPProvince> neighborsProvs;
	private int nextArmyId = -1;
	private IPProvince capital;

	PCountry(PGame game, Country country) {
		this.game = game;
		this.country = country;
		this.moneyBudget = new PMoneyBudget(this, country.getMoneyBudget());
		this.scienceBudget = new PScienceBudget(this, country.getScienceBudget());
		this.capital = game.findProvById(country.getCapitalId());

		armies = new ArrayList<>(country.getArmies().size());
		country.getArmies().forEach(a -> addArmy(new PArmy(game, a)));

		provinces = new ArrayList<>(country.getProvinces().size());
		country.getProvinces().forEach(p -> provinces.add(game.getProvince(p)));
		provinces = Collections.unmodifiableList(provinces);

	}

	private void addArmy(PArmy army) {
		army.setCountry(this);
		armies.add(army);
	}

	public PGame getGame() {
		return game;
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PCountry)) {
			return false;
		}
		return ((IPCountry) obj).getId() == getId();
	}

	@Override
	public int getId() {
		return country.getId();
	}

	public boolean isAI() {
		return country.isAI();
	}

	@Override
	public Collection<IPArmy> getArmies() {
		return armies;
	}

	@Override
	public IPProvince getCapital() {
		return capital;
	}

	@Override
	public IPProvince getFirstCapital() {
		return game.findProvById(country.getFirstCapitalId());
	}

	@Override
	public Collection<IPProvince> getProvinces() {
		return provinces;
	}

	@Override
	public IPMoneyBudget getMoneyBudget() {
		return moneyBudget;
	}

	@Override
	public IPScienceBudget getScienceBudget() {
		return scienceBudget;
	}

	@Override
	public String getAiScriptName() {
		return country.getAiScriptName();
	}

	@Override
	public Collection<IPProvince> getNeighborsProvs() {
		if (neighborsProvs == null) {
			neighborsProvs = new HashSet<>();
			getProvinces().forEach(p -> {
				p.getNeighbors().forEach(n -> {
					if (n.getTerrainType().isWater()) {
						neighborsProvs.addAll(getNeighborsThroughWater(n));
					} else if (!ComparisonTool.isEqual(n.getCountryId(), getId())) {
						neighborsProvs.add(n);
					}
				});
			});
		}
		return neighborsProvs;
	}

	private Collection<IPProvince> getNeighborsThroughWater(IPProvince province) {
		Collection<IPProvince> neighbors = new HashSet<>();
		Set<Integer> visitedWaterProvsIds = new HashSet<>();
		List<IPProvince> waterProvs = new ArrayList<>();
		waterProvs.add(province);
		visitedWaterProvsIds.add(province.getId());
		int idx = 0;
		while (idx < waterProvs.size()) {
			IPProvince prov = waterProvs.get(idx);
			for (IPProvince n : prov.getNeighbors()) {
				if (n.getTerrainType().isWater()) {
					if (!visitedWaterProvsIds.contains(n.getId())) {
						visitedWaterProvsIds.add(n.getId());
						Province realProv = country.getGame().getMap().findProvById(n.getId());
						if (realProv.isPassable(country)) {
							waterProvs.add(n);
						}
					}
				} else if (!ComparisonTool.isEqual(n.getCountryId(), getId())) {
					neighbors.add(n);
				}
			}
			idx++;
		}
		return neighbors;
	}

	@Override
	public IPArmy createArmy(int provinceId, int soldiers) {
		return (IPArmy) game.addCommand(new CommandArmyCreate(getNewArmyId(), provinceId, soldiers));
	}

	int getNewArmyId() {
		return nextArmyId--;
	}

	@Override
	public IPArmy findArmyById(int armyId) {
		for (IPArmy a : armies) {
			if (a.getId() == armyId) {
				return a;
			}
		}
		return null;
	}

	@Override
	public Collection<IPArmy> findArmiesInProv(IPProvince prov) {
		return armies.stream().filter(a -> prov.equals(a.getLocation())).collect(Collectors.toList());
	}

	@Override
	public void setCapital(IPProvince capital) {
		game.addCommand(new CommandProvinceSetCapital(capital.getId()));
	}

	@Override
	public double getArmySoldiersToPopulationForSubjugation() {
		return country.getArmySoldiersToPopulationForSubjugation();
	}

	/**
	 * 'cmc' prefix means Client Model Changes
	 */
	public void cmcSetCapital(IPProvince capital) {
		this.capital = capital;
	}

	public void cmcDismissArmy(PArmy army) {
		armies.remove(army);
	}

	public PArmy cmcCreateArmy(int armyId, IPProvince destination, int soldiers) {
		soldiers = Math.min(soldiers, destination.getPopulationAmount());
		PArmy newArmy = new PArmy(game, armyId, destination.getId(), soldiers);
		addArmy(newArmy);
		((PProvince) destination).cmcAddPopulation(-soldiers);
		return newArmy;
	}

	public Object cmcSplitArmy(PArmy army, int newArmyId, int soldiers) {
		PArmy newArmy = new PArmy(game, newArmyId, army.getLocation().getId(), soldiers);
		addArmy(newArmy);
		army.cmcAddSoldiers(-soldiers);
		return newArmy;
	}

}
