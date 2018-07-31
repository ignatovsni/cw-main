package com.cwsni.world.game.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
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

	private static final Log logger = LogFactory.getLog(ScriptAIHandler.class);

	private Map<String, Script> scriptsCache;

	public ScriptAIHandler() {
		scriptsCache = new HashMap<>();
	}

	public boolean hasScript(AIData4Country data) {
		return getScript(data) != null;
	}

	private Script getScript(AIData4Country data) {
		String scriptName = data.getCountry().getAiScriptName();
		if (scriptName == null || scriptName.isEmpty()) {
			return null;
		}
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
		InputStream in = classloader.getResourceAsStream("ai-scripts/" + fileName);
		String scriptText = loadScript(in);
		return scriptText;
	}

	private String loadScriptFromFile(String fileName) {
		fileName = "ai-scripts/" + fileName;
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

	private void invokeMethod(String methodName, AIData4Country data) {
		Script script = getScript(data);
		if (script != null) {
			script.invokeMethod(methodName, data);
		}
	}

	@Override
	public void processArmyBudget(AIData4Country data) {
		invokeMethod("processArmyBudget", data);
	}

	@Override
	public void processArmies(AIData4Country data) {
		invokeMethod("processArmies", data);
	}

	public void clearCache() {
		scriptsCache.clear();
	}

}
