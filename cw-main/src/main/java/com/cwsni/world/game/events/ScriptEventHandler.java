package com.cwsni.world.game.events;

import java.io.File;
import java.util.Map;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.game.scripts.AbstractScriptHandler;

public class ScriptEventHandler extends AbstractScriptHandler {

	protected static final String MAIN_SCRIPT_NAME_PART = ".main";

	private ApplicationSettings applicationSettings;
	protected String scriptId;
	private String eventsFolder;

	public ScriptEventHandler(ApplicationSettings applicationSettings, String scriptId, String eventsFolder) {
		this.applicationSettings = applicationSettings;
		this.scriptId = scriptId;
		this.eventsFolder = eventsFolder;
	}

	@Override
	protected int getScriptsPoolSize() {
		return applicationSettings.getEventsScriptsPoolSize();
	}

	@Override
	protected String getScriptFolderPath() {
		return eventsFolder + File.separator + scriptId;
	}

	@Override
	protected int getScriptsMaxStackDeep() {
		return applicationSettings.getScriptsMaxStackDeep();
	}

	public Object processEvent(Map<String, Object> mapBinding, String methodName, Object args) {
		return invokeScriptMethod(mapBinding, scriptId + MAIN_SCRIPT_NAME_PART, methodName, args);
	}

	public Object processEvent(Map<String, Object> mapBinding, String scriptName, String methodName, Object args) {
		return invokeScriptMethod(mapBinding, scriptName, methodName, args);
	}

}
