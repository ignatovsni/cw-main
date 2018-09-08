package com.cwsni.world.game.events;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.client.desktop.game.map.MapMode;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.locale.SimpleLocaleMessageSource;
import com.cwsni.world.model.engine.Event;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.Province;
import com.cwsni.world.util.CwBaseRandom;

@Component
public class GameEventHandler {

	private static final Log logger = LogFactory.getLog(GameEventHandler.class);

	protected static final String EVENTS_FOLDER = "data" + File.separator + "events";
	protected static final String MAIN_SCRIPT_NAME_PART = ".main";

	private static final String METHOD_PROCESS_NEW_TURN = "processNewTurn";
	private static final String METHOD_PREPARE_GAME_AFTER_LOADING = "prepareGameAfterLoading";

	private static final String UI_SCRIPT_NAME = "ui";
	private static final String METHOD_GET_TITLE_AND_SHORT_DESCRIPTION = "getTitleAndShortDescription";
	private static final String METHOD_GET_MAP_MODES = "getMapModes";
	private static final String METHOD_GET_PROVINCE_COLOR = "getProvinceColor";

	private Map<String, EventProcessorInfo> eventsInfo;

	@Autowired
	private ApplicationSettings applicationSettings;

	@Autowired
	private LocaleMessageSource messageSource;

	public void clearCache() {
		if (eventsInfo != null) {
			eventsInfo.values().forEach(epi -> epi.clearCache());
		}
		eventsInfo.clear();
		eventsInfo = null;
	}

	private void checkInitialization() {
		if (eventsInfo != null) {
			return;
		}
		eventsInfo = new HashMap<>();
		List<String> scripts = getListOfAvailableEventsScripts();
		for (String scriptId : scripts) {
			SimpleLocaleMessageSource localeMessageSource = new SimpleLocaleMessageSource();
			localeMessageSource.setApplicationSettings(applicationSettings);
			localeMessageSource.setLanguagesFolder(EVENTS_FOLDER + File.separator + scriptId);
			localeMessageSource.init();
			EventProcessorInfo epi = new EventProcessorInfo(scriptId, localeMessageSource,
					new ScriptEventHandler(applicationSettings, scriptId, EVENTS_FOLDER));
			eventsInfo.put(scriptId, epi);
		}
	}

	public List<String> getListOfAvailableEventsScripts() {
		Set<String> setOfScriptsName = new HashSet<>();
		File folder = new File(EVENTS_FOLDER);
		if (folder.exists() && folder.isDirectory() && folder.canRead()) {
			for (File subFolder : folder.listFiles()) {
				if (subFolder.isDirectory() && folder.canRead()) {
					for (File file : subFolder.listFiles()) {
						if (!file.isDirectory()) {
							String fileName = file.getName();
							int idx = fileName.lastIndexOf(MAIN_SCRIPT_NAME_PART + ".groovy");
							if (idx >= 0) {
								String eventScriptId = fileName.substring(0, idx);
								if (subFolder.getName().equals(eventScriptId)) {
									setOfScriptsName.add(eventScriptId);
								}
							}
						}
					}
				}
			}
		}
		List<String> names = new ArrayList<>(setOfScriptsName);
		Collections.sort(names);
		return names;
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
				epi.getScriptHandler().processEvent(createBinding(game, epi), METHOD_PROCESS_NEW_TURN, null);
			} catch (Exception e) {
				logger.error("failed to process event type=" + epi.getScriptId(), e);
			}
		}
	}

	public void activateEventsAfterLoading(Game game) {
		checkInitialization();
		for (EventProcessorInfo epi : eventsInfo.values()) {
			try {
				epi.getScriptHandler().processEvent(createBinding(game, epi), METHOD_PREPARE_GAME_AFTER_LOADING, null);
			} catch (Exception e) {
				logger.error("failed to process event type=" + epi.getScriptId(), e);
			}
		}
		game.getEventsCollection()
				.checkOldUntouchedEvents(applicationSettings.getEventsRemoveWhileLoadingIfDidNotTouchMoreThanTurns());
	}

	private Map<String, Object> createBinding(Game game, EventProcessorInfo epi) {
		Map<String, Object> mapBinding = new HashMap<>();
		CwBaseRandom randomForCurrentTurn = game == null ? null
				: game.getRandomForCurrentTurn(game.getEventsCollection().getEvents().size());
		DataForEvent data = new DataForEvent(game, epi, randomForCurrentTurn, epi.getScriptHandler().getWrapper());
		mapBinding.put("data", data);
		return mapBinding;
	}

	public Map<Event, List<String>> getTitleAndShortDescriptionsForEvents(Game game, Set<Object> eventsIds,
			Object target) {
		checkInitialization();
		Map<Event, List<String>> result = new HashMap<>(eventsInfo.size());
		for (Object id : eventsIds) {
			Event event = game.getEventsCollection().findEventById((Integer) id);
			if (event != null) {
				List<String> description = getTitleAndShortDescriptionForEvent(game, event, target);
				if (description != null) {
					result.put(event, description);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<String> getTitleAndShortDescriptionForEvent(Game game, Event event, Object target) {
		EventProcessorInfo epi = findEventProcessorInfoByScriptId(event.getType());
		if (epi == null) {
			return Arrays.asList(event.getType(), getMessage("error.msg.event.script.not-found"));
		}
		try {
			return (List<String>) epi.getScriptHandler().processEvent(createBinding(game, epi), UI_SCRIPT_NAME,
					METHOD_GET_TITLE_AND_SHORT_DESCRIPTION,
					new Object[] { event, applicationSettings.getLanguage(), target });
		} catch (Exception e) {
			logger.error("failed to process event type=" + epi.getScriptId(), e);
			return Arrays.asList(event.getType(), getMessage("error.msg.event.script.execution-error"));
		}
	}

	protected String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	/**
	 * Must return Map.key = the mode code, Map.value = List<String> with 2
	 * elements: [title, tooltip description].
	 */
	@SuppressWarnings("unchecked")
	public Map<String, List<String>> getAvailableEventsMapModes(Game game) {
		checkInitialization();
		Map<String, List<String>> modes = new HashMap<>();
		for (EventProcessorInfo epi : eventsInfo.values()) {
			try {
				Map<String, List<String>> list = (Map<String, List<String>>) epi.getScriptHandler()
						.processEvent(createBinding(game, epi), UI_SCRIPT_NAME, METHOD_GET_MAP_MODES, null);
				list.entrySet().forEach(entry -> {
					List<String> description = entry.getValue();
					if (description != null && description.size() == 2) {
						modes.put(MapMode.createEventMapCode(epi.getScriptId() + "." + entry.getKey()), description);
					} else {
						logger.error(
								"event script " + epi.getScriptId() + " created wrong map modes description: " + entry);
					}
				});
			} catch (Exception e) {
				logger.error("failed to process event type=" + epi.getScriptId(), e);
			}
		}
		return modes;
	}

	@SuppressWarnings("unchecked")
	public List<Double> getColorForProvince(Game game, Province province, MapMode mapMode) {
		checkInitialization();
		try {
			String scriptCode = mapMode.getEventScriptCode();
			String scriptId = scriptCode.substring(0, scriptCode.indexOf("."));
			String mapModeId = scriptCode.substring(scriptCode.indexOf(".") + 1);
			EventProcessorInfo epi = eventsInfo.get(scriptId);
			if (epi != null) {
				List<Double> colorList = (List<Double>) epi.getScriptHandler().processEvent(createBinding(game, epi),
						UI_SCRIPT_NAME, METHOD_GET_PROVINCE_COLOR, new Object[] { province, mapModeId });
				return colorList;
			} else {
				logger.trace("there is not event handler for: " + mapMode);
			}
		} catch (Exception e) {
			logger.error("failed to get map color for mode: " + mapMode, e);
		}
		return null;
	}

}
