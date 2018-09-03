package com.cwsni.world.model.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.cwsni.world.model.data.DataEvent;
import com.cwsni.world.model.util.ObjectStorage;
import com.cwsni.world.util.CwException;

public class EventCollection extends ObjectStorage<DataEvent, Integer, String> {

	private Game game;

	public void addEvent(DataEvent e) {
		if (e.getId() <= 0) {
			throw new CwException("event id should be initiazed");
		}
		if (e.getType() == null) {
			throw new CwException("event type should be initiazed");
		}
		add(e, e.getId(), e.getType());
	}

	public void removeEvent(DataEvent e) {
		remove(e, e.getId(), e.getType());
	}

	public List<DataEvent> getEvents() {
		return Collections.unmodifiableList(getObjects());
	}

	public boolean hasEventWithType(String type) {
		Map<Integer, DataEvent> events = getObjectsByType().get(type);
		return !(events == null || events.isEmpty());
	}

	public Collection<DataEvent> findEventsByType(String type) {
		Map<Integer, DataEvent> map = getObjectsByType().get(type);
		return map != null ? map.values() : Collections.emptyList();
	}

	public DataEvent findEventById(Integer id) {
		return getObjectByKey(id);
	}

	protected void buildFrom(Game game, List<DataEvent> events) {
		this.game = game;
		events.forEach(e -> addEvent(e));
	}

	public DataEvent createNewEvent(String type) {
		DataEvent e = new DataEvent();
		e.setType(type);
		e.setId(game.nextEventId());
		return e;
	}

}
