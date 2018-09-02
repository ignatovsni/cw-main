package com.cwsni.world.client.desktop.locale;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.tools.LanguageHelper;

@Component
public class DefaultLocaleMessageSource implements LocaleMessageSource {

	private static final Log logger = LogFactory.getLog(DefaultLocaleMessageSource.class);

	private static final String LANGUAGES_FOLDER = "data" + File.separator + "languages";
	private static final String LANGUAGE_LABEL = "language.label";
	private static final String DEFAULT_LANGUAGE_CODE = "";

	private String currentLanguage;
	private Map<String, String> messages;
	private Map<String, String> defaultLanguageMessages;

	@Autowired
	private ApplicationSettings applicationSettings;

	@PostConstruct
	private void init() {
		currentLanguage = DEFAULT_LANGUAGE_CODE;
		defaultLanguageMessages = readMessagesForLanguage(currentLanguage);
	}

	@Override
	public String getMessage(String code) {
		String appLanguage = applicationSettings.getLanguage();
		if (messages == null || !appLanguage.equals(currentLanguage)) {
			messages = readMessagesForLanguage(appLanguage);
			if (messages != null) {
				currentLanguage = appLanguage;
			} else {
				currentLanguage = DEFAULT_LANGUAGE_CODE;
			}
		}
		if (messages != null) {
			String value = messages.get(code);
			if (value == null) {
				value = defaultLanguageMessages.get(code);
				if (value == null) {
					value = code;
				}
			}
			return value;
		} else {
			return null;
		}
	}

	private Map<String, String> readMessagesForLanguage(String code) {
		try {
			LanguageHelper lh = new LanguageHelper() {
				@Override
				protected String getLanguageDirectoryFullPath() {
					return LANGUAGES_FOLDER;
				}
			};
			return lh.readLanguageFile(code);
		} catch (Exception e) {
			logger.error("failed to read language file for language code=" + code, e);
			return null;
		}
	}

	public Map<String, String> getAvailableLanguages() {
		Map<String, String> languages = new HashMap<>();
		File folder = new File(LANGUAGES_FOLDER);
		if (folder.exists() && folder.isDirectory() && folder.canRead()) {
			for (File file : folder.listFiles()) {
				String fileName = file.getName();
				if (fileName.equals("messages.properties")) {
					String label = getLanguageLabel(file);
					if (label != null) {
						languages.put("", label);
					}
					continue;
				}
				if (!fileName.startsWith("messages")) {
					continue;
				}
				int idxEndOfCode = fileName.lastIndexOf(".properties");
				if (idxEndOfCode < 0) {
					continue;
				}
				int idxStartOfCode = fileName.lastIndexOf("_");
				if (idxStartOfCode < 0) {
					continue;
				}
				String label = getLanguageLabel(file);
				if (label != null) {
					languages.put(fileName.substring(idxStartOfCode + 1, idxEndOfCode), label);
				}
			}
		}
		return languages;
	}

	private String getLanguageLabel(File file) {
		String languageLabel = null;
		try {
			try (FileInputStream fis = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
					BufferedReader br = new BufferedReader(isr)) {
				Optional<String> lineResult = br.lines().filter(line -> line.contains("=")).findFirst();
				if (lineResult.isPresent()) {
					String str = lineResult.get();
					int idx = str.indexOf("=");
					if (LANGUAGE_LABEL.equals(str.substring(0, idx).trim())) {
						languageLabel = str.substring(idx + 1).trim();
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
		if (languageLabel == null) {
			logger.warn("file " + file.getAbsolutePath() + " does not have " + LANGUAGE_LABEL + " in the first line.");
		}
		return languageLabel;
	}

}
