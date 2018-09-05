package com.cwsni.world.client.desktop.locale;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;

@Component
public class GlobalLocaleMessageSource extends SimpleLocaleMessageSource {

	private static final String LANGUAGES_FOLDER = "data" + File.separator + "languages";
	private static final String LANGUAGE_LABEL = "language.label";

	@Autowired
	public void setApplicationSettings(ApplicationSettings applicationSettings) {
		super.setApplicationSettings(applicationSettings);
	}

	@PostConstruct
	public void init() {
		setLanguagesFolder(LANGUAGES_FOLDER);
		super.init();
	}

	public Map<String, String> getAvailableLanguages() {
		Map<String, String> languages = new HashMap<>();
		File folder = new File(getLanguagesFolder());
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
