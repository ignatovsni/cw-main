package com.cwsni.world.model.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.cwsni.world.model.data.DataEvent;
import com.cwsni.world.model.util.ObjectStorage;
import com.cwsni.world.util.CwException;

public class EventCollection extends ObjectStorage<Event, Integer, String> {

	private Game game;

	protected void buildFrom(Game game, List<DataEvent> dataEvents) {
		this.game = game;
		dataEvents.forEach(de -> {
			Event e = new Event();
			e.buildFrom(game, de);
			registerEvent(e);
		});
	}

	public void addEvent(Event e) {
		game.getGameData().getEvents().add(e.getData());
		registerEvent(e);
	}

	private void registerEvent(Event e) {
		if (e.getId() <= 0) {
			throw new CwException("event id should be initiazed");
		}
		if (e.getType() == null) {
			throw new CwException("event type should be initiazed");
		}
		add(e, e.getId(), e.getType());
	}

	public void removeEvent(Event e) {
		remove(e, e.getId(), e.getType());
		game.getGameData().getEvents().remove(e.getData());
	}

	public List<Event> getEvents() {
		return Collections.unmodifiableList(getObjects());
	}

	public boolean hasEventWithType(String type) {
		Map<Integer, Event> events = getObjectsByType().get(type);
		return !(events == null || events.isEmpty());
	}

	public Collection<Event> findEventsByType(String type) {
		Map<Integer, Event> map = getObjectsByType().get(type);
		return map != null ? map.values() : Collections.emptyList();
	}

	public Event findEventById(Integer id) {
		return getObjectByKey(id);
	}

	public Event createNewEvent(String type) {
		DataEvent de = new DataEvent();
		de.setType(type);
		de.setId(game.nextEventId());
		de.setCreatedTurn(game.getTurn().getDateTurn());
		de.setLastProcessedTurn(game.getTurn().getDateTurn());
		Event e = new Event();
		e.buildFrom(game, de);
		return e;
	}

	public Event createAndAddNewEvent(String type) {
		Event e = createNewEvent(type);
		addEvent(e);
		return e;
	}

	public void checkOldUntouchedEvents() {
		int turnToDelete = game.getTurn().getProcessedTurn() - 100;
		List<Event> copy = new ArrayList<>(getEvents());
		copy.stream().filter(event -> event.getLastProcessedTurn() < turnToDelete)
				.forEach(event -> event.removeFromGame());
	}

}
