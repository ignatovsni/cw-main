package com.cwsni.world.client.desktop.locale;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.tools.LanguageHelper;

public class SimpleLocaleMessageSource implements LocaleMessageSource {

	protected final Log logger = LogFactory.getLog(getClass());

	protected static final String DEFAULT_LANGUAGE_CODE = "";

	protected String currentLanguage;
	protected Map<String, String> messages;
	protected Map<String, String> defaultLanguageMessages;

	protected ApplicationSettings applicationSettings;

	protected void init() {
		clearCache();
	}

	public void clearCache() {
		currentLanguage = DEFAULT_LANGUAGE_CODE;
		defaultLanguageMessages = readMessagesForLanguage(currentLanguage);
		messages = null;
	}

	@Override
	public String getMessage(String code) {
		String appLanguage = applicationSettings.getLanguage();
		if (messages == null || !appLanguage.equals(currentLanguage)) {
			messages = readMessagesForLanguage(appLanguage);
			currentLanguage = appLanguage;
		}
		String value = messages.get(code);
		if (value == null) {
			value = defaultLanguageMessages.get(code);
			if (value == null) {
				value = code;
			}
		}
		return value;
	}

	private Map<String, String> readMessagesForLanguage(String code) {
		try {
			LanguageHelper lh = new LanguageHelper() {
				@Override
				protected String getLanguageDirectoryFullPath() {
					return getLanguagesFolder();
				}
			};
			return lh.readLanguageFile(code);
		} catch (Exception e) {
			logger.error("failed to read language file for language code=" + code, e);
			return Collections.emptyMap();
		}
	}

	protected String getLanguagesFolder() {
		// TODO Auto-generated method stub
		return null;
	}

}
