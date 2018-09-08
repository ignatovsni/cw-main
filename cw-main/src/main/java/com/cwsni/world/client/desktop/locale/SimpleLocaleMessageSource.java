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
	protected String languagesFolder;
	private String languagesFileNameStartWith = "messages";
	private String languagesFileNameEndWith = ".properties";

	protected ApplicationSettings applicationSettings;

	public void init() {
		clearCache();
	}

	public void clearCache() {
		currentLanguage = DEFAULT_LANGUAGE_CODE;
		defaultLanguageMessages = null;
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
			if (defaultLanguageMessages == null) {
				defaultLanguageMessages = readMessagesForLanguage(currentLanguage);
			}
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

				protected String getFileNameStartWith() {
					return getLanguagesFileNameStartWith();
				}

				protected String getFileNameEndWith() {
					return getLanguagesFileNameEndWith();
				}
			};
			Map<String, String> messages = lh.readLanguageFile(code);
			messages.entrySet().forEach(e -> {
				e.setValue(e.getValue().replace(System.getProperty("line.separator"), ""));
				e.setValue(e.getValue().replace("\n", ""));
				e.setValue(e.getValue().replace("\\n", "\n"));
				e.setValue(e.getValue().replace("\\t", "\t"));
				e.setValue(e.getValue().replace("\\", ""));
			});
			return messages;
		} catch (Exception e) {
			if (logger.isTraceEnabled()) {
				logger.trace("failed to read language file for language code=" + code, e);
			} else {
				logger.warn("failed to read language file for language code=" + code);
			}
			return Collections.emptyMap();
		}
	}

	protected String getLanguagesFolder() {
		return languagesFolder;
	}

	public void setLanguagesFolder(String languagesFolder) {
		this.languagesFolder = languagesFolder;
	}

	public void setApplicationSettings(ApplicationSettings applicationSettings) {
		this.applicationSettings = applicationSettings;
	}

	public String getLanguagesFileNameStartWith() {
		return languagesFileNameStartWith;
	}

	public void setLanguagesFileNameStartWith(String languagesFileNameStartWith) {
		this.languagesFileNameStartWith = languagesFileNameStartWith;
	}

	public String getLanguagesFileNameEndWith() {
		return languagesFileNameEndWith;
	}

	public void setLanguagesFileNameEndWith(String languagesFileNameEndWith) {
		this.languagesFileNameEndWith = languagesFileNameEndWith;
	}

}
