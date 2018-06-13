package com.cwsni.world.client.desktop.locale;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.GlobalCongiguration;

@Component
public class DefaultLocaleMessageSource implements LocaleMessageSource {

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private GlobalCongiguration globalConfig; 

	@Override
	public String getMessage(String code) {
		return messageSource.getMessage(code, null, getLocale());
	}

	private Locale getLocale() {
		return new Locale(globalConfig.getLocale());
	}

}
