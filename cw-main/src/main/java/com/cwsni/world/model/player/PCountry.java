package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cwsni.world.game.commands.CommandArmyCreate;
import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.Country;

public class PCountry {

	private Country country;
	private PBudget budget;
	private List<PArmy> armies;
	private List<PProvince> provinces;
	private PGame game;
	private List<PProvince> neighborsProvs;

	PCountry(PGame game, Country country) {
		this.game = game;
		this.country = country;
		this.budget = new PBudget(country.getBudget());

		armies = new ArrayList<>(country.getArmies().size());
		country.getArmies().forEach(a -> {
			PArmy army = game.getArmy(a);
			army.setCountry(this);
			armies.add(army);
		});
		armies = Collections.unmodifiableList(armies);

		provinces = new ArrayList<>(country.getProvinces().size());
		country.getProvinces().forEach(p -> provinces.add(game.getProvince(p)));
		provinces = Collections.unmodifiableList(provinces);
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PCountry)) {
			return false;
		}
		return ((PCountry) obj).getId() == getId();
	}

	public int getId() {
		return country.getId();
	}

	public boolean isAI() {
		return country.isAI();
	}

	public List<PArmy> getArmies() {
		return armies;
	}

	public Integer getCapitalId() {
		return country.getCapitalId();
	}

	public PProvince getCapital() {
		return game.getProvince(getCapitalId());
	}

	public PProvince getFirstCapital() {
		return game.getProvince(country.getFirstCapitalId());
	}

	public List<PProvince> getProvinces() {
		return provinces;
	}

	public PBudget getBudget() {
		return budget;
	}

	public List<PProvince> getNeighborsProvs() {
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

	public void createArmy(int provinceId, int soldiers) {
		game.addCommand(new CommandArmyCreate(provinceId, soldiers));
	}

}
