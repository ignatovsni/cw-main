package com.cwsni.world.model.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * O : object K : key T : type
 */
public class ObjectStorage<O, K, T> {

	private Set<K> keys = new HashSet<>();

	@JsonIgnore
	private List<O> objects = new ArrayList<>();
	@JsonIgnore
	private Map<T, Map<K, O>> objectsByType = new HashMap<>();
	@JsonIgnore
	private Map<K, O> objectsByKey = new HashMap<>();

	protected void add(O o, K key, T type) {
		keys.add(key);
		objects.add(o);
		objectsByKey.put(key, o);
		Map<K, O> mapForTypeById = objectsByType.get(type);
		if (mapForTypeById == null) {
			mapForTypeById = new HashMap<>();
			objectsByType.put(type, mapForTypeById);
		}
		mapForTypeById.put(key, o);
	}

	protected void remove(O o, K key, T type) {
		if (keys.remove(key)) {
			objects.remove(o);
			objectsByKey.remove(key);
			objectsByType.get(type).remove(key);
		}
	}

	protected Set<K> getKeys() {
		return keys;
	}

	protected void setKeys(Set<K> keys) {
		this.keys = keys;
	}

	protected List<O> getObjects() {
		return objects;
	}

	protected Map<T, Map<K, O>> getObjectsByType() {
		return objectsByType;
	}

	protected O getObjectByKey(K key) {
		return objectsByKey.get(key);
	}
	
}
