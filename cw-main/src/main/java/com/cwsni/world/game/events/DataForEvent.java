package com.cwsni.world.game.events;

import java.util.Collection;

import com.cwsni.world.game.scripts.AbstractScriptHandler.ScriptHandlerWrapper;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Event;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.Province;
import com.cwsni.world.model.engine.modifiers.CountryModifier;
import com.cwsni.world.model.engine.modifiers.Modifier;
import com.cwsni.world.model.engine.modifiers.ModifierType;
import com.cwsni.world.model.engine.modifiers.ProvinceModifier;
import com.cwsni.world.util.CwBaseRandom;

public class DataForEvent {

	public class EventCollectionWrapper {

		public Collection<Event> findEventsByThisType() {
			return game.getEventsCollection().findEventsByType(epi.getScriptId());
		}

		public Event createAndAddNewEvent() {
			return game.getEventsCollection().createAndAddNewEvent(epi.getScriptId());
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Modifier addModifier(Province target, Object feature, ModifierType type, Double value, Event event) {
			Modifier<ProvinceModifier> modifier = Modifier
					.createModifierByEvent(target.getClass().getSimpleName() + ":" + target.getId(), feature, type, value, event);
			target.getModifiers().add(modifier);
			event.addProvinceModifier(target, modifier);
			return modifier;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Modifier addModifier(Country target, Object feature, ModifierType type, Double value, Event event) {
			Modifier<CountryModifier> modifier = Modifier
					.createModifierByEvent(target.getClass().getSimpleName() + ":" + target.getId(), feature, type, value, event);
			target.getModifiers().add(modifier);
			event.addCountryModifier(target, modifier);
			return modifier;
		}

	}

	private Game game;
	private CwBaseRandom rnd;
	private ScriptHandlerWrapper scriptEventsWrapper;
	private EventProcessorInfo epi;

	public DataForEvent(Game game, EventProcessorInfo epi, CwBaseRandom randomForCurrentTurn,
			ScriptHandlerWrapper scriptEventsWrapper) {
		this.game = game;
		this.epi = epi;
		this.rnd = randomForCurrentTurn;
		this.scriptEventsWrapper = scriptEventsWrapper;
	}

	public Game getGame() {
		return game;
	}

	public CwBaseRandom getRnd() {
		return rnd;
	}

	public EventCollectionWrapper getEvents() {
		return new EventCollectionWrapper();
	}

	public ScriptHandlerWrapper getScriptHandler() {
		return scriptEventsWrapper;
	}

	public String getMessage(String code) {
		return epi.getMessageSource().getMessage(code);
	}

}
