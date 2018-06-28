package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cwsni.world.model.Country;

public class PCountry {

	private Country country;
	private List<PArmy> armies;
	private List<PProvince> provinces;
	private PGame game;
	private List<PProvince> neighborsProvs;

	PCountry(PGame game, Country country) {
		this.game = game;
		this.country = country;

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

	public List<PProvince> getNeighborsProvs() {
		if (neighborsProvs == null) {
			neighborsProvs = new ArrayList<>();
			getProvinces().forEach(p -> {
				p.getNeighbors().forEach(n -> {
					if (n.getCountryId() != p.getCountryId() && n.getTerrainType().isPopulationPossible()) {
						neighborsProvs.add(n);
					}
				});
			});
		}
		return neighborsProvs;
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		return ((PCountry) obj).getId() == getId();
	}

}
