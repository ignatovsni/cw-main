package com.cwsni.world.game.events;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.locale.SimpleLocaleMessageSource;

public class EventProcessorInfo {

	private String scriptId;
	private SimpleLocaleMessageSource messageSource;
	private ScriptEventHandler scriptHandler;

	public EventProcessorInfo(String scriptId, SimpleLocaleMessageSource localeMessageSource,
			ScriptEventHandler scriptHandler) {
		this.scriptId = scriptId;
		this.messageSource = localeMessageSource;
		this.scriptHandler = scriptHandler;
	}

	public String getScriptId() {
		return scriptId;
	}

	public LocaleMessageSource getMessageSource() {
		return messageSource;
	}

	public ScriptEventHandler getScriptHandler() {
		return scriptHandler;
	}

	public void clearCache() {
		messageSource.clearCache();
		scriptHandler.clearCache();
	}

}
