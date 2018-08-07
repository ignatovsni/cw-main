package com.cwsni.world.game.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

@Component
@Qualifier("scriptAIHandler")
public class ScriptAIHandler implements IAIHandler {

	public static final String DEFAULT_SCRIPT = "default";
	private static final String JAVA_INTERNAL_AI = "[java]";
	private static final String AI_SCRIPTS_FOLDER_WITH_SLASH = "ai-scripts/";
	private static final String AI_SCRIPTS_FOLDER = "ai-scripts";

	private static final Log logger = LogFactory.getLog(ScriptAIHandler.class);

	private Map<String, Script> scriptsCache;

	public ScriptAIHandler() {
		scriptsCache = new HashMap<>();
	}

	public boolean hasScript(AIData4Country data) {
		String aiScriptName = data.getCountry().getAiScriptName();
		if (aiScriptName == null || aiScriptName.isEmpty() || JAVA_INTERNAL_AI.equals(aiScriptName)) {
			return false;
		}
		return getScript(data) != null;
	}

	private Script getScript(AIData4Country data) {
		String scriptName = data.getCountry().getAiScriptName();
		if (scriptName == null || scriptName.isEmpty()) {
			return null;
		}
		scriptName += ".country";
		return getScriptByName(scriptName);
	}

	private Script getScriptByName(String scriptName) {
		Script script = scriptsCache.get(scriptName);
		if (!scriptsCache.containsKey(scriptName)) {
			try {
				String fileName = scriptName + ".groovy";
				String scriptText = loadScriptFromFile(fileName);
				if (scriptText == null) {
					scriptText = loadScriptFromResources(fileName);
				}
				String commonSection = loadScriptFromResources("common-section.groovy");
				scriptText = commonSection + scriptText;
				script = new GroovyShell().parse(scriptText);
				scriptsCache.put(scriptName, script);
			} catch (Exception e) {
				logger.warn("failed to parse/load script '" + scriptName + "'", e);
				scriptsCache.put(scriptName, null);
			}
		}
		return script;
	}

	private String loadScriptFromResources(String fileName) {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream in = classloader.getResourceAsStream(AI_SCRIPTS_FOLDER_WITH_SLASH + fileName);
		String scriptText = loadScript(in);
		return scriptText;
	}

	private String loadScriptFromFile(String fileName) {
		fileName = AI_SCRIPTS_FOLDER_WITH_SLASH + fileName;
		final File file = new File(fileName);
		if (!file.exists() || file.isDirectory() || !file.canRead()) {
			return null;
		}
		try {
			FileInputStream in = new FileInputStream(file);
			String scriptText = loadScript(in);
			in.close();
			return scriptText;
		} catch (Exception e) {
			logger.warn("failed to parse loadScriptFromFile '" + fileName + "'", e);
		}
		return null;
	}

	private String loadScript(InputStream in) {
		return new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
	}

	public void clearCache() {
		scriptsCache.clear();
	}

	private void invokeMethod(String methodName, AIData4Country data) {
		Script script = getScript(data);
		if (script != null) {
			script.invokeMethod(methodName, data);
		}
	}

	@Override
	public void processCountry(AIData4Country data) {
		invokeMethod("processCountry", data);
	}

	public List<String> getListOfAvailableScripts() {
		Set<String> setOfScriptsName = new HashSet<>();
		final File file = new File(AI_SCRIPTS_FOLDER);
		if (file.exists() && file.isDirectory() && file.canRead()) {
			for (File app : file.listFiles()) {
				String fileName = app.getName();
				int idx = fileName.lastIndexOf(".country.groovy");
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
		System.out.println(names);
		return names;
	}
}
