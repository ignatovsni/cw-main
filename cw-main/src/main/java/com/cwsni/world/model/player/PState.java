package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.cwsni.world.model.engine.State;
import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.model.player.interfaces.IPState;

public class PState implements IPState {

	private State state;
	private List<IPProvince> provinces;
	private IPProvince capital;

	PState(PGame game, State state) {
		this.state = state;
		this.capital = game.findProvById(state.getCapitalId());

		provinces = new ArrayList<>(state.getProvinces().size());
		state.getProvinces().forEach(p -> provinces.add(game.getProvince(p)));
		provinces = Collections.unmodifiableList(provinces);
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof IPState)) {
			return false;
		}
		return ((IPState) obj).getId() == getId();
	}

	@Override
	public int getId() {
		return state.getId();
	}

	@Override
	public IPProvince getCapital() {
		return capital;
	}

	@Override
	public Collection<IPProvince> getProvinces() {
		return provinces;
	}
	
	@Override
	public long getPopulationAmount() {
		return state.getPopulationAmount();
	}
	
	@Override
	public double getLoayltyToState() {
		return state.getLoayltyToState();
	}
	
	@Override
	public double getLoayltyToCountry(int countryId) {
		return state.getLoayltyToCountry(countryId);
	}

}
