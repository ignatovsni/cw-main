package com.cwsni.world.model.events;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.GameParams;

public class EventGlobalClimateChange extends Event {

	public static void processNewEvent(Game game, LocaleMessageSource messageSource) {
		GameParams gParams = game.getGameParams();
		if (game.hasEventWithType(EVENT_GLOBAL_CLIMATE_CHANGE)
				|| gParams.getRandom().nextDouble() > gParams.getEventGlobalClimateChangeProbability()) {
			return;
		}
		Event e = createEvent(game, gParams);
		// direction
		e.setEffectDouble2(gParams.getRandom().nextDouble() > gParams.getEventGlobalClimateChangeBadProbability()
				? 1 + gParams.getEventGlobalClimateChangeMultiplicator()
				: 1 - gParams.getEventGlobalClimateChangeMultiplicator());
		// current effect
		e.setEffectDouble1(e.getEffectDouble2());
		// current steps
		e.setEffectInt1(1);
		e.setTitle(messageSource.getMessage(EVENT_GLOBAL_CLIMATE_CHANGE + ".title"));
		e.setDescription(messageSource.getMessage("event.global.climate.influence") + " " + e.getEffectDouble1());
	}

	private static Event createEvent(Game game, GameParams gParams) {
		Event e = new Event();
		e.setId(game.nextEventId());
		e.setType(EVENT_GLOBAL_CLIMATE_CHANGE);
		e.setVisibleForUser(true);
		e.setStartTurn(game.getTurn().getTurn());
		e.setDuration(gParams.getEventGlobalClimateChangeDuration());
		game.addEvent(e);
		game.getMap().getProvinces().stream().filter(p -> p.getTerrainType().isSoilPossible())
				.forEach(p -> p.addEvent(e));
		return e;
	}

	public static void finishEvent(Game game, Event oldE, LocaleMessageSource messageSource) {
		if (oldE.getEffectInt1() == 0) {
			return;
		}
		GameParams gParams = game.getGameParams();
		Event e = createEvent(game, gParams);
		e.setTitle(oldE.getTitle());
		if (oldE.getEffectInt1() > 0) {
			e.setEffectDouble1(oldE.getEffectDouble1() * oldE.getEffectDouble2());
		} else {
			e.setEffectDouble1(oldE.getEffectDouble1() / oldE.getEffectDouble2());
		}
		double climageChange = DataFormatter.doubleWith2points(e.getEffectDouble1());
		climageChange = Math.max(climageChange, 1 - gParams.getEventGlobalClimateMaxChange());
		climageChange = Math.min(climageChange, 1 + gParams.getEventGlobalClimateMaxChange());
		e.setEffectDouble1(climageChange);
		e.setEffectDouble2(oldE.getEffectDouble2());
		e.setEffectInt1(oldE.getEffectInt1() + 1);
		if (e.getEffectInt1() > 0
				&& gParams.getRandom().nextDouble() > gParams.getEventGlobalClimateChangeContinueProbability()) {
			e.setEffectInt1(-e.getEffectInt1());
		}
		e.setDescription(messageSource.getMessage("event.global.climate.influence") + " " + e.getEffectDouble1());
	}

}
