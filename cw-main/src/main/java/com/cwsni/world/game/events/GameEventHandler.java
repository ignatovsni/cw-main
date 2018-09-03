package com.cwsni.world.game.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.client.desktop.locale.SimpleLocaleMessageSource;
import com.cwsni.world.model.engine.Game;

@Component
public class GameEventHandler {

	private static final Log logger = LogFactory.getLog(GameEventHandler.class);

	private Map<String, EventProcessorInfo> eventsInfo;

	@Autowired
	private ScriptEventHandler scriptEventHandler;

	@Autowired
	private ApplicationSettings applicationSettings;

	public void clearCache() {
		this.eventsInfo = null;
	}

	private void checkInitialization() {
		if (eventsInfo != null) {
			return;
		}
		eventsInfo = new HashMap<>();
		List<String> scripts = scriptEventHandler.getListOfAvailableEventsScripts();
		for (String scriptId : scripts) {
			SimpleLocaleMessageSource localeMessageSource = new SimpleLocaleMessageSource();
			localeMessageSource.setApplicationSettings(applicationSettings);
			localeMessageSource.setLanguagesFolder(ScriptEventHandler.EVENTS_SCRIPTS_FOLDER);
			EventProcessorInfo epi = new EventProcessorInfo(scriptId, localeMessageSource);
			eventsInfo.put(scriptId, epi);
		}
	}

	public void processNewTurn(Game game) {
		checkInitialization();
		for (EventProcessorInfo epi : eventsInfo.values()) {
			try {
				processNewTurn(game, epi);
			} catch (Exception e) {
				logger.error("failed to process event type=" + epi.getId(), e);
			}
		}
	}

	private void processNewTurn(Game game, EventProcessorInfo epi) {
		Map<String, Object> mapBinding = new HashMap<>();
		DataForEvent data = new DataForEvent(game,
				game.getRandomForCurrentTurn(game.getEventsCollection().getEvents().size()));
		mapBinding.put("data", data);
		scriptEventHandler.processEvent(mapBinding, epi.getId(), "processEvent", null);
	}

}
