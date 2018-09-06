package com.cwsni.world.game.events;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.game.scripts.AbstractScriptHandler;

@Component
public class ScriptEventHandler extends AbstractScriptHandler {

	@Autowired
	private ApplicationSettings applicationSettings;

	protected static final String EVENTS_SCRIPTS_FOLDER = "data" + File.separator + "events";
	protected static final String MAIN_SCRIPT_NAME_PART = ".main";

	protected ThreadLocal<String> subFolder = new ThreadLocal<>();

	@Override
	protected int getScriptsPoolSize() {
		return applicationSettings.getEventsScriptsPoolSize();
	}

	@Override
	protected String getScriptFolderPath() {
		if (subFolder.get() == null) {
			return EVENTS_SCRIPTS_FOLDER;
		} else {
			return EVENTS_SCRIPTS_FOLDER + File.separator + subFolder.get();
		}
	}

	@Override
	protected int getScriptsMaxStackDeep() {
		return applicationSettings.getScriptsMaxStackDeep();
	}

	public List<String> getListOfAvailableEventsScripts() {
		Set<String> setOfScriptsName = new HashSet<>();
		File folder = new File(EVENTS_SCRIPTS_FOLDER);
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

	public Object processEvent(Map<String, Object> mapBinding, String scriptName, String methodName, Object args) {
		subFolder.set(scriptName);
		Object result = invokeScriptMethod(mapBinding, scriptName + MAIN_SCRIPT_NAME_PART, methodName, args);
		subFolder.set(null);
		return result;
	}
}
