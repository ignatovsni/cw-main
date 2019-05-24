package com.cwsni.world.game.ai;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.game.scripts.AbstractScriptHandler;
import com.cwsni.world.model.player.interfaces.IData4Country;
import com.cwsni.world.settings.ApplicationSettings;

@Component
public class ScriptAIHandler extends AbstractScriptHandler {

	@Autowired
	private ApplicationSettings applicationSettings;

	public static final String DEFAULT_SCRIPT = "[java]";
	//public static final String DEFAULT_SCRIPT = "default";
	private static final String JAVA_INTERNAL_AI = "[java]";
	private static final String AI_SCRIPTS_FOLDER = "data" + File.separator + "ai-scripts";
	protected static final String MAIN_SCRIPT_NAME_PART = ".main";

	public boolean hasScriptForCountry(IData4Country data) {
		String aiScriptName = data.getCountry().getAiScriptName();
		if (aiScriptName == null || aiScriptName.isEmpty() || JAVA_INTERNAL_AI.equals(aiScriptName)) {
			return false;
		}
		return hasScriptByName(getScriptNameForCountry(data));
	}

	private String getScriptNameForCountry(IData4Country data) {
		String scriptName = data.getCountry().getAiScriptName();
		if (scriptName == null || scriptName.isEmpty()) {
			return null;
		}
		scriptName += MAIN_SCRIPT_NAME_PART;
		return scriptName;
	}

	public void processCountry(IData4Country data) {
		String scriptName = getScriptNameForCountry(data);
		Map<String, Object> mapBinding = new HashMap<>();
		mapBinding.put("data", data);
		mapBinding.put("game", data.getGame());
		invokeScriptMethod(mapBinding, scriptName, "processCountry", null);
	}

	public List<String> getListOfAvailableCountryScripts() {
		Set<String> setOfScriptsName = new HashSet<>();
		File folder = new File(AI_SCRIPTS_FOLDER);
		if (folder.exists() && folder.isDirectory() && folder.canRead()) {
			for (File file : folder.listFiles()) {
				String fileName = file.getName();
				int idx = fileName.lastIndexOf(MAIN_SCRIPT_NAME_PART + ".groovy");
				if (idx < 0) {
					continue;
				}
				setOfScriptsName.add(fileName.substring(0, idx));
			}
		}
		setOfScriptsName.remove(DEFAULT_SCRIPT);
		List<String> names = new ArrayList<>(setOfScriptsName);
		Collections.sort(names);
		names.add(0, DEFAULT_SCRIPT);
		names.add(1, JAVA_INTERNAL_AI);
		return names;
	}

	@Override
	protected int getScriptsPoolSize() {
		return applicationSettings.getAiScriptsPoolSize();
	}

	@Override
	protected String getScriptFolderPath() {
		return AI_SCRIPTS_FOLDER;
	}

	@Override
	protected int getScriptsMaxStackDeep() {
		return applicationSettings.getScriptsMaxStackDeep();
	}
}
