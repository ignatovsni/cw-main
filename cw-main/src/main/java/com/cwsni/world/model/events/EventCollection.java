package com.cwsni.world.model.events;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.model.Game;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class EventCollection {

	private int[] eventIds = new int[0];
	@JsonIgnore
	private List<Event> events = new ArrayList<>();
	private Game game;

	public void add(Event e) {
		events.add(e);
	}

	public void removeEvent(Event e) {
		events.remove(e);
	}

	public int[] getEventIds() {
		eventIds = new int[events.size()];
		for (int i = 0; i < events.size(); i++) {
			eventIds[i] = events.get(i).getId();
		}
		return eventIds;
	}
	
	public List<Event> getEvents() {
		return events;
	}

	public void setEventIds(int[] eventIds) {
		this.eventIds = eventIds;
	}

	public void postLoad(Game game) {
		this.game = game;
		getEvents().clear();
		for (int id : eventIds) {
			getEvents().add(game.findEventById(id));
		}
	}

	public void postGenerate(Game game) {
		this.game = game;
	}

}
