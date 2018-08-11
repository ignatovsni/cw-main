package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cwsni.world.game.commands.CommandArmyCreate;
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

	PCountry(PGame game, Country country) {
		this.game = game;
		this.country = country;
		this.budget = new PBudget(country.getBudget());

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
	public List<IPArmy> getArmies() {
		return armies;
	}

	@Override
	public Integer getCapitalId() {
		return country.getCapitalId();
	}

	@Override
	public IPProvince getCapital() {
		return game.getProvince(getCapitalId());
	}

	@Override
	public IPProvince getFirstCapital() {
		return game.getProvince(country.getFirstCapitalId());
	}

	@Override
	public List<IPProvince> getProvinces() {
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
	public List<IPProvince> getNeighborsProvs() {
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
	public void createArmy(int provinceId, int soldiers) {
		game.addCommand(new CommandArmyCreate(nextArmyId--, provinceId, soldiers));
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

	public void cpDismissArmy(PArmy army) {
		armies.remove(army);
	}

	public PArmy cpCreateArmy(int armyId, Integer locationId, int soldiers) {
		PArmy army = new PArmy(game, armyId, locationId, soldiers);
		addArmy(army);
		return army;
	}

}
