package com.cwsni.world.game.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.model.player.interfaces.IData4Country;
import com.cwsni.world.util.CwException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Add scripts for: - fights - states creating - country creating ???
 * 
 */

@Component
public class ScriptAIHandler {

	private static final Log logger = LogFactory.getLog(ScriptAIHandler.class);

	@Autowired
	private ApplicationSettings applicationSettings;

	public static final String DEFAULT_SCRIPT = "default";
	private static final String JAVA_INTERNAL_AI = "[java]";
	private static final String AI_SCRIPTS_FOLDER = "data" + File.separator + "ai-scripts";
	private static final int MAX_ALLLOWED_STACK = 10;

	private Map<String, BlockingQueue<Script>> scriptsCache;

	public ScriptAIHandler() {
		scriptsCache = new HashMap<>();
	}

	public boolean hasScriptForCountry(IData4Country data) {
		String aiScriptName = data.getCountry().getAiScriptName();
		if (aiScriptName == null || aiScriptName.isEmpty() || JAVA_INTERNAL_AI.equals(aiScriptName)) {
			return false;
		}
		return getScriptByName(getScriptNameForCountry(data)) != null;
	}

	private String getScriptNameForCountry(IData4Country data) {
		String scriptName = data.getCountry().getAiScriptName();
		if (scriptName == null || scriptName.isEmpty()) {
			return null;
		}
		scriptName += ".country";
		return scriptName;
	}

	public void processCountry(IData4Country data) {
		String scriptName = getScriptNameForCountry(data);
		BlockingQueue<Script> scriptPool = getScriptByName(scriptName);
		Map<String, Object> mapBinding = new HashMap<>();
		mapBinding.put("data", data);
		mapBinding.put("game", data.getGame());
		scriptsThreadLocalStack.set(null);
		scriptsThreadLocalBinding.set(null);
		invokeScriptMethod(mapBinding, scriptName, scriptPool, "processCountry", null);
	}

	private Object invokeScriptMethod(Map<String, Object> mapBinding, String scriptName,
			BlockingQueue<Script> scriptPool, String methodName, Object args) {
		Script script = scriptPool.poll();
		try {
			if (script == null) {
				logger.warn("scriptPool.poll() returned null");
				return null;
			}
			if (scriptsThreadLocalStack.get() == null) {
				scriptsThreadLocalStack.set(new ArrayList<>());
			}
			scriptsThreadLocalStack.get().add(scriptName);
			if (scriptsThreadLocalBinding.get() == null) {
				scriptsThreadLocalBinding.set(new Binding(mapBinding));
			}
			script.setBinding(scriptsThreadLocalBinding.get());
			if (scriptsThreadLocalStack.get().size() > MAX_ALLLOWED_STACK) {
				String msg = "scripts stack may not be more than MAX_ALLLOWED_STACK (" + MAX_ALLLOWED_STACK
						+ "). Current stack: " + scriptsThreadLocalStack.get();
				System.out.println(msg);
				throw new CwException(msg);
			}
			return script.invokeMethod(methodName, args);
		} finally {
			// clean bindings to avoid memory leaks
			script.setBinding(new Binding());
			scriptPool.add(script);
			scriptsThreadLocalStack.get().remove(scriptsThreadLocalStack.get().size() - 1);
			if (scriptsThreadLocalStack.get().isEmpty()) {
				scriptsThreadLocalStack.set(null);
				scriptsThreadLocalBinding.set(null);
			}
		}
	}

	private BlockingQueue<Script> getScriptByName(String scriptName) {
		if (scriptName == null) {
			return null;
		}
		BlockingQueue<Script> scriptsQueue = scriptsCache.get(scriptName);
		if (scriptsQueue == null) {
			try {
				String fileName = scriptName + ".groovy";
				String scriptText = loadScriptFromFile(fileName);
				if (scriptText != null) {
					String commonSection = loadScriptFromFile("common-section.groovy");
					scriptText = commonSection + "\n" + scriptText;
					int aiScriptsPoolSize = applicationSettings.getAIScriptsPoolSize();
					scriptsQueue = new LinkedBlockingQueue<>(aiScriptsPoolSize);
					GroovyShell groovyShell = new GroovyShell();
					for (int i = 0; i < aiScriptsPoolSize; i++) {
						Script script = groovyShell.parse(scriptText);
						scriptsQueue.add(script);
					}
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
		if (!file.exists()) {
			throw new CwException("script not found: " + fileName);
		}
		if (file.isDirectory() || !file.canRead()) {
			throw new CwException("script file is directory or can not be read: " + fileName);
		}
		try {
			return loadScript(file);
		} catch (Exception e) {
			logger.warn("failed to parse loadScriptFromFile '" + fileName + "'", e);
		}
		return null;
	}

	private String loadScript(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				BufferedReader br = new BufferedReader(isr)) {
			return br.lines().collect(Collectors.joining("\n"));
		}
	}

	public void clearCache() {
		scriptsCache.clear();
	}

	public List<String> getListOfAvailableCountryScripts() {
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

	// --------------------- Wrapper and chain invocations --------------

	private ThreadLocal<List<String>> scriptsThreadLocalStack = new ThreadLocal<>();
	private ThreadLocal<Binding> scriptsThreadLocalBinding = new ThreadLocal<>();

	public class ScriptAIHandlerWrapper {
		private ScriptAIHandler scriptAIHandler;

		public ScriptAIHandlerWrapper(ScriptAIHandler scriptAIHandler) {
			this.scriptAIHandler = scriptAIHandler;
		}

		public Object invoke(String scriptName, String methodName, Object args) {
			BlockingQueue<Script> scriptPool = scriptAIHandler.getScriptByName(scriptName);
			return invokeScriptMethod(null, scriptName, scriptPool, methodName, args);
		}

		public Object invoke(String scriptName, String methodName) {
			return invoke(scriptName, methodName, null);
		}
	}

	public ScriptAIHandlerWrapper getWrapper() {
		return new ScriptAIHandlerWrapper(this);
	}
}
