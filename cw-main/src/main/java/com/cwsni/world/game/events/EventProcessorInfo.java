package com.cwsni.world.game.events;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

public class EventProcessorInfo {

	private String scriptId;
	private LocaleMessageSource messageSource;

	public EventProcessorInfo(String scriptId, LocaleMessageSource localeMessageSource) {
		this.scriptId = scriptId;
		this.messageSource = localeMessageSource;
	}
	
	public String getScriptId() {
		return scriptId;
	}
	
	public LocaleMessageSource getMessageSource() {
		return messageSource;
	}

}
