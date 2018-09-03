package com.cwsni.world.model.engine.modifiers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModifierCollection<T> {
	/**
	 * Можно кешировать расчёты. Если начальный параметр и коллекции не изменились,
	 * то ответ тот же. Если есть только ADD модификатор, то кеш не зависит от
	 * входных данных. Так же стоит кешировать модификаторы от разных источников. (все ADD_1, все MULTIPLY, ADD_2)
	 */

	private Map<T, Map<ModifierType, Set<Modifier<T>>>> modfiersByFeature;
	private Map<ModifierSource, Set<Modifier<T>>> modifiersBySource;
	
	private long timestampLastChange;

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

	public boolean addModifier(Modifier<T> modifier) {
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
		timestampLastChange = System.nanoTime();
	}

	public boolean removeModifier(Modifier<T> modifier) {
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

	public void removeAllModifiersBySource(ModifierSource source) {
		Set<Modifier<T>> modifiersForSource = modifiersBySource.remove(source);
		if (modifiersForSource == null) {
			return;
		}
		modifiersForSource.forEach(m -> removeModifier(m));
	}

}
