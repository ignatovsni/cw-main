package com.cwsni.world.model.engine.modifiers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cwsni.world.model.data.DataEvent;
import com.cwsni.world.util.ComparisonTool;

public class ModifierCollection<T> {
	/**
	 * Можно кешировать расчёты. Если начальный параметр и коллекции не изменились,
	 * то ответ тот же. Если есть только ADD модификатор, то кеш не зависит от
	 * входных данных. Так же стоит кешировать модификаторы от разных источников.
	 * (все ADD_1, все MULTIPLY, ADD_2)
	 */

	private Map<T, Map<ModifierType, Set<Modifier<T>>>> modfiersByFeature = new HashMap<>();
	private Map<ModifierSource, Set<Modifier<T>>> modifiersBySource = new HashMap<>();

	// private double timestampLastChange;

	public Map<ModifierType, Set<Modifier<T>>> findByFeature(T feature) {
		Map<ModifierType, Set<Modifier<T>>> modifiersByType = modfiersByFeature.get(feature);
		if (modifiersByType != null) {
			return modifiersByType;
		}
		return Collections.emptyMap();
	}

	public Set<Modifier<T>> findByFeatureAndType(String feature, ModifierType type) {
		Map<ModifierType, Set<Modifier<T>>> modifiersByType = modfiersByFeature.get(feature);
		if (modifiersByType != null) {
			Set<Modifier<T>> modifiers = modifiersByType.get(type);
			if (modifiers != null) {
				return modifiers;
			}
		}
		return Collections.emptySet();
	}

	public Set<Modifier<T>> findBySource(ModifierSource source) {
		Set<Modifier<T>> modifiersForSource = modifiersBySource.get(source);
		if (modifiersForSource != null) {
			return modifiersForSource;
		}
		return Collections.emptySet();
	}

	public Set<Modifier<T>> findByEvent(DataEvent event) {
		return findBySource(new ModifierSource(ModifierSourceType.EVENT, event.getId()));
	}

	public boolean add(Modifier<T> modifier) {
		// modfiersByFeature
		Map<ModifierType, Set<Modifier<T>>> modifiersByType = modfiersByFeature.get(modifier.getFeature());
		if (modifiersByType == null) {
			modifiersByType = new HashMap<>();
			modfiersByFeature.put(modifier.getFeature(), modifiersByType);
		}
		Set<Modifier<T>> modifiersForFeatureAndType = modifiersByType.get(modifier.getType());
		if (modifiersForFeatureAndType == null) {
			modifiersForFeatureAndType = new HashSet<>();
			modifiersByType.put(modifier.getType(), modifiersForFeatureAndType);
		}

		refreshLastTimeStamp();
		modifiersForFeatureAndType.add(modifier);

		// modifiersBySource
		Set<Modifier<T>> modifiersForSource = modifiersBySource.get(modifier.getSource());
		if (modifiersForSource == null) {
			modifiersForSource = new HashSet<>();
			modifiersBySource.put(modifier.getSource(), modifiersForSource);
		}

		return modifiersForSource.add(modifier);
	}

	private void refreshLastTimeStamp() {
		// TODO ???
		// timestampLastChange = System.nanoTime();
	}

	public void update(Modifier<T> modifier, Double value) {
		if (!ComparisonTool.isEqual(modifier.getValue(), value)) {
			modifier.setValue(value);
			refreshLastTimeStamp();
		}
	}

	public boolean remove(Modifier<T> modifier) {
		// modfiersByFeature
		Map<ModifierType, Set<Modifier<T>>> modifiersByType = modfiersByFeature.get(modifier.getFeature());
		if (modifiersByType == null) {
			return false;
		}
		Set<Modifier<T>> modifiersForFeatureAndType = modifiersByType.get(modifier.getType());
		if (modifiersForFeatureAndType == null) {
			return false;
		}

		refreshLastTimeStamp();

		boolean removed = modifiersForFeatureAndType.remove(modifier);
		if (modifiersForFeatureAndType.isEmpty()) {
			modifiersByType.remove(modifier.getType());
		}
		if (modifiersByType.isEmpty()) {
			modfiersByFeature.remove(modifier.getFeature());
		}

		// modifiersBySource
		Set<Modifier<T>> modifiersForSource = modifiersBySource.get(modifier.getSource());
		if (modifiersForSource == null) {
			return false;
		}
		modifiersForSource.remove(modifier);
		if (modifiersForSource.isEmpty()) {
			modifiersBySource.remove(modifier.getSource());
		}

		return removed;
	}

	public void removeBySource(ModifierSource source) {
		Set<Modifier<T>> modifiersForSource = modifiersBySource.remove(source);
		if (modifiersForSource == null) {
			return;
		}
		modifiersForSource.forEach(m -> remove(m));
	}

	public void removeByEvent(DataEvent event) {
		removeBySource(new ModifierSource(ModifierSourceType.EVENT, event.getId()));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ". modfiersByFeature.size=" + modfiersByFeature.size()
				+ ", modifiersBySource.size=" + modifiersBySource.size();
	}

	public double getModifiedValue(T feature, double value) {
		if (modifiersBySource.isEmpty()) {
			return value;
		}
		Map<ModifierType, Set<Modifier<T>>> modifiersByType = findByFeature(feature);
		if (modifiersByType.isEmpty()) {
			return value;
		}

		Set<Modifier<T>> mdfs = modifiersByType.get(ModifierType.ADD);
		if (mdfs != null && !mdfs.isEmpty()) {
			for (Modifier<T> modifier : mdfs) {
				value += modifier.getValue();
			}
		}

		mdfs = modifiersByType.get(ModifierType.MULTIPLY);
		if (mdfs != null && !mdfs.isEmpty()) {
			for (Modifier<T> modifier : mdfs) {
				value *= modifier.getValue();
			}
		}

		mdfs = modifiersByType.get(ModifierType.ADD_2);
		if (mdfs != null && !mdfs.isEmpty()) {
			for (Modifier<T> modifier : mdfs) {
				value += modifier.getValue();
			}
		}

		return value;
	}

	public Set<Object> getAllEvents() {
		return modifiersBySource.keySet().stream().filter(source -> ModifierSourceType.EVENT.equals(source.getType())).map(source -> source.getId())
				.collect(Collectors.toSet());
	}

}
