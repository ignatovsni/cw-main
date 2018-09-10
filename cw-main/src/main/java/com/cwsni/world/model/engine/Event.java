package com.cwsni.world.model.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.model.data.DataEvent;
import com.cwsni.world.model.engine.modifiers.CountryModifier;
import com.cwsni.world.model.engine.modifiers.Modifier;
import com.cwsni.world.model.engine.modifiers.ModifierSource;
import com.cwsni.world.model.engine.modifiers.ModifierSourceType;
import com.cwsni.world.model.engine.modifiers.ProvinceModifier;

public class Event {

	private DataEvent data;
	private Map<Integer, List<Modifier<ProvinceModifier>>> provinceModifiers;
	private Map<Integer, List<Modifier<CountryModifier>>> countryModifiers;
	private Game game;

	public void buildFrom(Game game, DataEvent data) {
		this.game = game;
		this.data = data;
		provinceModifiers = new HashMap<>();
		countryModifiers = new HashMap<>();
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Event)) {
			return false;
		}
		Event otherEvent = (Event) obj;
		return this.data.equals(otherEvent.data);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": type = " + getType() + ", id = " + getId() + "";
	}

	protected DataEvent getData() {
		return data;
	}

	public Map<Object, Object> getInfo() {
		return data.getInfo();
	}

	public int getId() {
		return data.getId();
	}

	public String getType() {
		return data.getType();
	}

	public void markAsProcessed() {
		data.setLastProcessedTurn(game.getTurn().getProcessedTurn());
	}

	public int getLastProcessedTurn() {
		return data.getLastProcessedTurn();
	}

	public void removeFromGame() {
		removeModifiers();
		game.getEventsCollection().removeEvent(this);
	}

	public int getCreatedTurn() {
		return data.getCreatedTurn();
	}

	public void removeModifiers() {
		ModifierSource source = new ModifierSource(ModifierSourceType.EVENT, getId());

		provinceModifiers.keySet().stream().map(id -> game.getMap().findProvinceById(id))
				.forEach(o -> o.getModifiers().removeBySource(source));
		provinceModifiers.clear();

		countryModifiers.keySet().stream().map(id -> game.findCountryById(id)).filter(o -> o != null)
				.forEach(o -> o.getModifiers().removeBySource(source));
		countryModifiers.clear();
	}

	public void addProvinceModifier(Province target, Modifier<ProvinceModifier> modifier) {
		List<Modifier<ProvinceModifier>> modifiers = provinceModifiers.get(target.getId());
		if (modifiers == null) {
			modifiers = new ArrayList<>();
			provinceModifiers.put(target.getId(), modifiers);
		}
		modifiers.add(modifier);
	}

	public Map<Integer, List<Modifier<ProvinceModifier>>> getProvinceModifiers() {
		return Collections.unmodifiableMap(provinceModifiers);
	}

	public Collection<Integer> getProvincesIds() {
		return Collections.unmodifiableSet(provinceModifiers.keySet());
	}

	public void addCountryModifier(Country target, Modifier<CountryModifier> modifier) {
		List<Modifier<CountryModifier>> modifiers = countryModifiers.get(target.getId());
		if (modifiers == null) {
			modifiers = new ArrayList<>();
			countryModifiers.put(target.getId(), modifiers);
		}
		modifiers.add(modifier);
	}

	public Map<Integer, List<Modifier<CountryModifier>>> getCountryModifiers() {
		return Collections.unmodifiableMap(countryModifiers);
	}

	public Collection<Integer> getCountriesIds() {
		return Collections.unmodifiableSet(countryModifiers.keySet());
	}

}
