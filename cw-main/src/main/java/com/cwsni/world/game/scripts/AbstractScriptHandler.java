package com.cwsni.world.game.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.util.CwException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

abstract public class AbstractScriptHandler {

	protected Log logger = LogFactory.getLog(getClass());
	
	private Map<String, BlockingQueue<Script>> scriptsCache = new HashMap<>();

	abstract protected int getScriptsPoolSize();

	abstract protected String getScriptFolderPath();

	abstract protected int getScriptsMaxStackDeep();

	protected Object invokeScriptMethod(Map<String, Object> mapBinding, String scriptName, String methodName,
			Object args) {
		scriptsThreadLocalStack.set(null);
		scriptsThreadLocalBinding.set(null);
		return internalInvokeScriptMethod(mapBinding, scriptName, methodName, args);
	}

	private Object internalInvokeScriptMethod(Map<String, Object> mapBinding, String scriptName, String methodName,
			Object args) {
		BlockingQueue<Script> scriptPool = getScriptByName(scriptName);
		Script script = null;
		try {
			script = scriptPool.poll(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new CwException(e);
		}
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
			if (scriptsThreadLocalStack.get().size() > getScriptsMaxStackDeep()) {
				String msg = "scripts stack may not be more than MAX_ALLLOWED_STACK (" + getScriptsMaxStackDeep()
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
					int scriptsPoolSize = getScriptsPoolSize();
					scriptsQueue = new LinkedBlockingQueue<>(scriptsPoolSize);
					GroovyShell groovyShell = new GroovyShell();
					for (int i = 0; i < scriptsPoolSize; i++) {
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

	protected boolean hasScriptByName(String scriptName) {
		return getScriptByName(scriptName) != null;
	}

	private String loadScriptFromFile(String fileName) {
		fileName = getScriptFolderPath() + File.separator + fileName;
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

	// --------------------- Wrapper and chain invocations --------------

	private ThreadLocal<List<String>> scriptsThreadLocalStack = new ThreadLocal<>();
	private ThreadLocal<Binding> scriptsThreadLocalBinding = new ThreadLocal<>();

	public class ScriptHandlerWrapper {
		private AbstractScriptHandler scriptAIHandler;

		public ScriptHandlerWrapper(AbstractScriptHandler scriptAIHandler) {
			this.scriptAIHandler = scriptAIHandler;
		}

		public Object invoke(String scriptName, String methodName, Object args) {
			return scriptAIHandler.internalInvokeScriptMethod(null, scriptName, methodName, args);
		}

		public Object invoke(String scriptName, String methodName) {
			return invoke(scriptName, methodName, null);
		}
	}

	public ScriptHandlerWrapper getWrapper() {
		return new ScriptHandlerWrapper(this);
	}
}
