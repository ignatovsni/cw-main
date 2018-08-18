package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.cwsni.world.game.commands.CommandArmyCreate;
import com.cwsni.world.game.commands.CommandProvinceSetCapital;
import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPBudget;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPProvince;

public class PCountry implements IPCountry {

	private Country country;
	private IPBudget budget;
	private List<IPArmy> armies;
	private List<IPProvince> provinces;
	private PGame game;
	private List<IPProvince> neighborsProvs;
	private int nextArmyId = -1;
	private IPProvince capital;

	PCountry(PGame game, Country country) {
		this.game = game;
		this.country = country;
		this.budget = new PBudget(country.getBudget());
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
	public IPBudget getBudget() {
		return budget;
	}

	@Override
	public String getAiScriptName() {
		return country.getAiScriptName();
	}

	@Override
	public Collection<IPProvince> getNeighborsProvs() {
		if (neighborsProvs == null) {
			neighborsProvs = new ArrayList<>();
			getProvinces().forEach(p -> {
				p.getNeighbors().forEach(n -> {
					if (!ComparisonTool.isEqual(n.getCountryId(), p.getCountryId())
							&& n.getTerrainType().isPopulationPossible()) {
						neighborsProvs.add(n);
					}
				});
			});
		}
		return neighborsProvs;
	}

	@Override
	public IPArmy createArmy(int provinceId, int soldiers) {
		return (IPArmy) game.addCommand(new CommandArmyCreate(nextArmyId--, provinceId, soldiers));
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

	/**
	 * 'cmc' prefix means Client Model Changes
	 */
	public void cmcDismissArmy(PArmy army) {
		armies.remove(army);
	}

	public PArmy cmcCreateArmy(int armyId, Integer locationId, int soldiers) {
		PArmy army = new PArmy(game, armyId, locationId, soldiers);
		addArmy(army);
		return army;
	}

	public void cmcSetCapital(IPProvince capital) {
		this.capital = capital;
	}

}
