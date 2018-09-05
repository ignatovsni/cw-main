package com.cwsni.world.game.events;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.locale.SimpleLocaleMessageSource;
import com.cwsni.world.model.engine.Event;
import com.cwsni.world.model.engine.Game;

@Component
public class GameEventHandler {

	private static final Log logger = LogFactory.getLog(GameEventHandler.class);

	private static final String METHOD_PROCESS_NEW_TURN = "processNewTurn";
	private static final String METHOD_PREPARE_GAME_AFTER_LOADING = "prepareGameAfterLoading";
	private static final String METHOD_GET_TITLE_AND_SHORT_DESCRIPTION = "getTitleAndShortDescription";

	private static final String LANGUAGES_FILE_NAME_MID_WITH = ".messages";
	private static final String LANGUAGES_FILE_NAME_END_WITH = ".txt";

	private Map<String, EventProcessorInfo> eventsInfo;

	@Autowired
	private ScriptEventHandler scriptEventHandler;

	@Autowired
	private ApplicationSettings applicationSettings;

	@Autowired
	private LocaleMessageSource messageSource;

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
			localeMessageSource.setLanguagesFileNameStartWith(scriptId + LANGUAGES_FILE_NAME_MID_WITH);
			localeMessageSource.setLanguagesFileNameEndWith(LANGUAGES_FILE_NAME_END_WITH);
			localeMessageSource.init();
			EventProcessorInfo epi = new EventProcessorInfo(scriptId, localeMessageSource);
			eventsInfo.put(scriptId, epi);
		}
	}

	private EventProcessorInfo findEventProcessorInfoByScriptId(String scriptId) {
		if (eventsInfo != null) {
			return eventsInfo.get(scriptId);
		} else {
			return null;
		}
	}

	public void processNewTurn(Game game) {
		checkInitialization();
		for (EventProcessorInfo epi : eventsInfo.values()) {
			try {
				scriptEventHandler.processEvent(createBinding(game, epi), epi.getScriptId(), METHOD_PROCESS_NEW_TURN,
						null);
			} catch (Exception e) {
				logger.error("failed to process event type=" + epi.getScriptId(), e);
			}
		}
	}

	public void activateEventsAfterLoading(Game game) {
		checkInitialization();
		for (EventProcessorInfo epi : eventsInfo.values()) {
			try {
				scriptEventHandler.processEvent(createBinding(game, epi), epi.getScriptId(),
						METHOD_PREPARE_GAME_AFTER_LOADING, null);
			} catch (Exception e) {
				logger.error("failed to process event type=" + epi.getScriptId(), e);
			}
		}
		game.getEventsCollection()
				.checkOldUntouchedEvents(applicationSettings.getEventsRemoveWhileLoadingIfDidNotTouchMoreThanTurns());
	}

	private Map<String, Object> createBinding(Game game, EventProcessorInfo epi) {
		Map<String, Object> mapBinding = new HashMap<>();
		DataForEvent data = new DataForEvent(game, epi,
				game.getRandomForCurrentTurn(game.getEventsCollection().getEvents().size()),
				scriptEventHandler.getWrapper());
		mapBinding.put("data", data);
		return mapBinding;
	}

	public Map<Event, List<String>> getTitleAndShortDescriptionsForEvents(Game game, Set<Object> eventsIds) {
		checkInitialization();
		Map<Event, List<String>> result = new HashMap<>(eventsInfo.size());
		for (Object id : eventsIds) {
			Event event = game.getEventsCollection().findEventById((Integer) id);
			if (event != null) {
				List<String> description = getTitleAndShortDescriptionForEvent(game, event);
				if (description != null) {
					result.put(event, description);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<String> getTitleAndShortDescriptionForEvent(Game game, Event event) {
		EventProcessorInfo epi = findEventProcessorInfoByScriptId(event.getType());
		if (epi == null) {
			return Arrays.asList(event.getType(), getMessage("error.msg.event.script.not-found"));
		}
		try {
			return (List<String>) scriptEventHandler.processEvent(createBinding(game, epi), epi.getScriptId(),
					METHOD_GET_TITLE_AND_SHORT_DESCRIPTION, new Object[] {event, applicationSettings.getLanguage()});
		} catch (Exception e) {
			logger.error("failed to process event type=" + epi.getScriptId(), e);
			return Arrays.asList(event.getType(), getMessage("error.msg.event.script.execution-error"));
		}
	}

	protected String getMessage(String code) {
		return messageSource.getMessage(code);
	}

}
