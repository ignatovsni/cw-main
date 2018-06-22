package com.cwsni.world.model.events;

import java.util.List;
import java.util.stream.Collectors;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Population;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.data.GameParams;

public class EventEpidemic extends Event {

	public static void processNewEvent(Game game, LocaleMessageSource messageSource) {
		GameParams gParams = game.getGameParams();
		if (gParams.getRandom().nextDouble() > gParams.getEventEpidemicProbability()) {
			return;
		}
		Event e = createEvent(game, gParams);
		Province core = null;
		do {
			core = game.getMap().findProvById(gParams.getRandom().nextInt(game.getMap().getProvinces().size()));
		} while (core == null || core.getPopulationAmount() == 0);
		core.addEvent(e);
		e.setEffectDouble1(DataFormatter
				.doubleWith2points(gParams.getEventEpidemicContagiousness() * gParams.getRandom().nextNormalDouble()));
		e.setEffectDouble2(DataFormatter
				.doubleWith2points(gParams.getEventEpidemicDeathRate() * gParams.getRandom().nextNormalDouble()));
		e.setTitle(messageSource.getMessage(EVENT_EPIDEMIC + ".title"));
		e.setDescription(messageSource.getMessage("event.epidemic.influence") + " " + e.getEffectDouble1() + "/"
				+ e.getEffectDouble2());
	}

	private static Event createEvent(Game game, GameParams gParams) {
		Event e = new Event();
		e.setId(game.nextEventId());
		e.setType(EVENT_EPIDEMIC);
		e.setVisibleForUser(true);
		e.setStartTurn(game.getTurn().getTurn());
		e.setDuration(gParams.getRandom().nextInt(gParams.getEventEpidemicDuration()));
		game.addEvent(e);
		return e;
	}

	public static void processEpidemicEvent(Game game, Province p, Event oldE) {
		if (oldE.getStartTurn() >= game.getTurn().getTurn()) {
			return;
		}
		if (oldE.isFinished(game)) {
			finishEvent(game, p, oldE);
			return;
		}
		Population.dieFromDisease(game, p, oldE.getEffectDouble2());
		GameParams gParams = game.getGameParams();
		List<Province> neighbors = p.getNeighbors().stream()
				.filter(n -> (n.getPopulationAmount() > 0) && !n.getEvents().hasEventWithType(EVENT_EPIDEMIC)
						&& !n.getEvents().hasEventWithType(EVENT_EPIDEMIC_PROTECTED))
				.collect(Collectors.toList());
		if (neighbors.isEmpty()) {
			return;
		}
		neighbors.stream().filter(n -> gParams.getRandom().nextDouble() < oldE.getEffectDouble1()).forEach(n -> {
			Event e = createEvent(game, gParams);
			e.setEffectDouble1(oldE.getEffectDouble1());
			e.setEffectDouble2(oldE.getEffectDouble2());
			e.setTitle(oldE.getTitle());
			e.setDescription(oldE.getDescription());
			n.addEvent(e);
		});

	}

	public static void finishEvent(Game game, Province p, Event oldE) {
		if (!oldE.getType().equals(EVENT_EPIDEMIC)) {
			return;
		}
		GameParams gParams = game.getGameParams();
		Event e = new Event();
		e.setId(game.nextEventId());
		e.setType(EVENT_EPIDEMIC_PROTECTED);
		e.setVisibleForUser(true);
		e.setStartTurn(game.getTurn().getTurn());
		e.setDuration(gParams.getEventEpidemicProtectionDuration());
		// TODO LocaleMessageSource messageSource
		e.setTitle("event.epidemic.protected.title");
		e.setDescription("event.epidemic.protected.title");
		game.addEvent(e);
		p.getEvents().add(e);
	}

}
