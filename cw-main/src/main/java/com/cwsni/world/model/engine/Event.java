package com.cwsni.world.model.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.model.data.DataEvent;
import com.cwsni.world.model.engine.modifiers.Modifier;
import com.cwsni.world.model.engine.modifiers.ModifierSource;
import com.cwsni.world.model.engine.modifiers.ModifierSourceType;
import com.cwsni.world.model.engine.modifiers.ProvinceModifier;

public class Event {

	private DataEvent data;
	private Map<Province, List<Modifier<ProvinceModifier>>> provinceModifiers;

	public void buildFrom(Game game, DataEvent data) {
		this.data = data;
		provinceModifiers = new HashMap<>();
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

	public void addProvinceModifier(Province p, Modifier<ProvinceModifier> modifier) {
		List<Modifier<ProvinceModifier>> modifiers = provinceModifiers.get(p);
		if (modifiers == null) {
			modifiers = new ArrayList<>();
			provinceModifiers.put(p, modifiers);
		}
		modifiers.add(modifier);
	}

	public Map<Province, List<Modifier<ProvinceModifier>>> getProvinceModifiers() {
		return provinceModifiers;
	}

	public void removeModifiers() {
		ModifierSource source = new ModifierSource(ModifierSourceType.EVENT, getId());
		provinceModifiers.keySet().forEach(p -> p.getModifiers().removeBySource(source));
		provinceModifiers.clear();
	}

}
