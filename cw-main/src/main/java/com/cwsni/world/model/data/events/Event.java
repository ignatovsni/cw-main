package com.cwsni.world.model.data.events;

import java.util.List;
import java.util.stream.Collectors;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.Province;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Event {

	public static final String EVENT_GLOBAL_CLIMATE_CHANGE = "event.global.climate.change";
	public static final String EVENT_EPIDEMIC = "event.epidemic";
	public static final String EVENT_EPIDEMIC_PROTECTED = "event.epidemic.protected";

	private String type;
	private int id;
	private int startTurn;
	private int duration;
	private double effectDouble1;
	private double effectDouble2;
	private int effectInt1;
	private String title;
	private String description;
	private boolean isVisibleForUser;

	public double getEffectDouble1() {
		return effectDouble1;
	}

	public void setEffectDouble1(double effectDouble1) {
		this.effectDouble1 = effectDouble1;
	}

	public double getEffectDouble2() {
		return effectDouble2;
	}

	public int getEffectInt1() {
		return effectInt1;
	}

	public void setEffectInt1(int effectInt1) {
		this.effectInt1 = effectInt1;
	}

	public void setEffectDouble2(double effectDouble2) {
		this.effectDouble2 = effectDouble2;
	}

	public boolean isVisibleForUser() {
		return isVisibleForUser;
	}

	public void setVisibleForUser(boolean isVisibleForUser) {
		this.isVisibleForUser = isVisibleForUser;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStartTurn() {
		return startTurn;
	}

	public void setStartTurn(int startTurn) {
		this.startTurn = startTurn;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonIgnore
	public boolean isFinished(Game game) {
		return game.getTurn().getTurn() > getStartTurn() + getDuration();
	}

	// ---------------- static section -----------------------------

	public static void processEvents(Game game, LocaleMessageSource messageSource) {
		deactivateFinishedEvents(game, messageSource);
		EventGlobalClimateChange.processNewEvent(game, messageSource);
		EventEpidemic.processNewEvent(game, messageSource);
	}

	private static void deactivateFinishedEvents(Game game, LocaleMessageSource messageSource) {
		List<Event> finishedEvents = game.getEvents().stream().filter(e -> e.isFinished(game))
				.collect(Collectors.toList());
		finishedEvents.forEach(e -> {
			game.removeEvent(e);
			finish(game, e, messageSource);
		});
	}

	protected static void finish(Game game, Event e, LocaleMessageSource messageSource) {
		switch (e.getType()) {
		case EVENT_GLOBAL_CLIMATE_CHANGE:
			EventGlobalClimateChange.finishEvent(game, e, messageSource);
			break;
		case EVENT_EPIDEMIC:
			// made in processEvent
			break;
		}
	}

	public static void processEvent(Game game, Province p, Event e) {
		switch (e.getType()) {
		case EVENT_EPIDEMIC:
			EventEpidemic.processEpidemicEvent(game, p, e);
			break;
		}
	}

}
