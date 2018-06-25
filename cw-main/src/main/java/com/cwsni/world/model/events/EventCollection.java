package com.cwsni.world.model.events;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.cwsni.world.model.Game;
import com.cwsni.world.model.data.DataProvince;
import com.cwsni.world.model.util.ObjectStorage;

public class EventCollection extends ObjectStorage<Event, Integer, String> {

	private DataProvince dataProvince;

	public void addEvent(Event e) {
		add(e, e.getId(), e.getType());
		dataProvince.getEvents().add(e.getId());
	}

	public void removeEvent(Event e) {
		remove(e, e.getId(), e.getType());
		dataProvince.getEvents().remove(e.getId());
	}

	public List<Event> getEvents() {
		return Collections.unmodifiableList(getObjects());
	}

	public boolean hasEventWithType(String type) {
		Map<Integer, Event> events = getObjectsByType().get(type);
		return !(events == null || events.isEmpty());
	}

	public Collection<Event> getEventsWithType(String type) {
		Map<Integer, Event> map = getObjectsByType().get(type);
		return map != null ? map.values() : Collections.emptyList();
	}

	public Event getEventById(Integer id) {
		return getObjectByKey(id);
	}

	public void buildFrom(DataProvince dp, Game game) {
		this.dataProvince = dp;
		dataProvince.getEvents().forEach(id -> addEvent(game.findEventById(id)));
	}

	public void buildFrom(Game game, List<Event> events) {
		// fake province to be able to use EventCollection
		this.dataProvince = new DataProvince();
		events.forEach(e -> addEvent(e));
	}

}
