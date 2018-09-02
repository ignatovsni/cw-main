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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.model.player.interfaces.IData4Country;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Add scripts for: - fights - states creating - country creating ???
 * 
 */

@Component
@Qualifier("scriptAIHandler")
public class ScriptAIHandler {

	@Autowired
	private ApplicationSettings applicationSettings;

	public static final String DEFAULT_SCRIPT = "default";
	// public static final String DEFAULT_SCRIPT = "[java]";
	private static final String JAVA_INTERNAL_AI = "[java]";
	private static final String AI_SCRIPTS_FOLDER = "data" + File.separator + "ai-scripts";

	private static final Log logger = LogFactory.getLog(ScriptAIHandler.class);

	private Map<String, BlockingQueue<Script>> scriptsCache;

	public ScriptAIHandler() {
		scriptsCache = new HashMap<>();
	}

	public boolean hasScript(IData4Country data) {
		String aiScriptName = data.getCountry().getAiScriptName();
		if (aiScriptName == null || aiScriptName.isEmpty() || JAVA_INTERNAL_AI.equals(aiScriptName)) {
			return false;
		}
		return getScript(data) != null;
	}

	private BlockingQueue<Script> getScript(IData4Country data) {
		String scriptName = data.getCountry().getAiScriptName();
		if (scriptName == null || scriptName.isEmpty()) {
			return null;
		}
		scriptName += ".country";
		return getScriptByName(scriptName);
	}

	private BlockingQueue<Script> getScriptByName(String scriptName) {
		BlockingQueue<Script> scriptsQueue = scriptsCache.get(scriptName);
		if (scriptsQueue == null) {
			try {
				String fileName = scriptName + ".groovy";
				String scriptText = loadScriptFromFile(fileName);
				String commonSection = loadScriptFromFile("common-section.groovy");
				scriptText = commonSection + scriptText;
				scriptsQueue = new LinkedBlockingQueue<>();
				GroovyShell groovyShell = new GroovyShell();
				for (int i = 0; i < applicationSettings.getAIScriptsPoolSize(); i++) {
					Script script = groovyShell.parse(scriptText);
					scriptsQueue.add(script);
				}
				scriptsCache.put(scriptName, scriptsQueue);
			} catch (Exception e) {
				logger.warn("failed to parse/load script '" + scriptName + "'", e);
				scriptsCache.put(scriptName, null);
			}
		}
		return scriptsQueue;
	}

	private String loadScriptFromFile(String fileName) {
		fileName = AI_SCRIPTS_FOLDER + File.separator + fileName;
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

	private void invokeMethod(String methodName, IData4Country data) {
		BlockingQueue<Script> scriptPool = getScript(data);
		if (scriptPool != null) {
			Script script = scriptPool.poll();
			if (script != null) {
				try {
					script.invokeMethod(methodName, data);
				} finally {
					// clean bindings to avoid memory leaks
					script.setBinding(new Binding());
					scriptPool.add(script);
				}
			} else {
				logger.warn("scriptPool.poll() returned null");
			}
		}
	}

	public void processCountry(IData4Country data) {
		invokeMethod("processCountry", data);
	}

	public List<String> getListOfAvailableScripts() {
		Set<String> setOfScriptsName = new HashSet<>();
		File folder = new File(AI_SCRIPTS_FOLDER);
		if (folder.exists() && folder.isDirectory() && folder.canRead()) {
			for (File file : folder.listFiles()) {
				String fileName = file.getName();
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
		return names;
	}
}
