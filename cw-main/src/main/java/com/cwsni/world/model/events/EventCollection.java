package com.cwsni.world.model.events;

import java.util.List;
import java.util.Map;

import com.cwsni.world.model.Game;
import com.cwsni.world.model.util.ObjectStorage;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class EventCollection extends ObjectStorage<Event, Integer, String> {

	private Game game;

	public void add(Event e) {
		add(e, e.getId(), e.getType());
	}

	public void removeEvent(Event e) {
		remove(e, e.getId(), e.getType());
	}

	@JsonIgnore
	public List<Event> getEvents() {
		return getObjects();
	}

	public void postLoad(Game game) {
		this.game = game;
		getEvents().clear();
		for (int id : getKeys()) {
			Event e = game.findEventById(id);
			add(e, e.getId(), e.getType());
		}
	}

	public void postGenerate(Game game) {
		this.game = game;
	}

	public boolean hasEventWithType(String type) {
		Map<Integer, Event> events = getObjectsByType().get(type);
		return !(events == null || events.isEmpty());
	}

}
