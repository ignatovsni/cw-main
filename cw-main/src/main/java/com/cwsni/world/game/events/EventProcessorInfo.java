package com.cwsni.world.game.events;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

public class EventProcessorInfo {

	private String id;
	private LocaleMessageSource messageSource;

	public EventProcessorInfo(String scriptId, LocaleMessageSource localeMessageSource) {
		this.id = scriptId;
		this.messageSource = localeMessageSource;
	}
	
	public String getId() {
		return id;
	}
	
	public LocaleMessageSource getMessageSource() {
		return messageSource;
	}

}
