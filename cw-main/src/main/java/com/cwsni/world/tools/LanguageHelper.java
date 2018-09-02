package com.cwsni.world.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageHelper {

	public static void main(String[] args) throws IOException {
		new LanguageHelper().updateLanguageFile("ru");
	}

	public void updateLanguageFile(String language) throws IOException {
		Map<String, String> languageMessages = readLanguageFile(language);
		List<String> resultRows = createResultRows(language, languageMessages);
		writeNewLanguageFile(language, resultRows);
		System.out.println("Created new file for " + language);
	}

	public Map<String, String> readLanguageFile(String language) throws FileNotFoundException, IOException {
		Map<String, String> languageMessages = new HashMap<>();
		File languageFile = new File(getLanguageMessagesFullPath(language));
		try (FileInputStream fis = new FileInputStream(languageFile);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				BufferedReader br = new BufferedReader(isr)) {
			br.lines().forEach(line -> {
				int idx = line.indexOf("=");
				if (idx > 0) {
					languageMessages.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
				}
			});
		}
		return languageMessages;
	}

	private List<String> createResultRows(String language, Map<String, String> languageMessages)
			throws FileNotFoundException, IOException {
		String languageDirectory = getLanguageDirectoryFullPath();
		List<String> resultRows = new ArrayList<>();
		File baseFile = new File(languageDirectory + File.separator + "messages.properties");
		try (FileInputStream fis = new FileInputStream(baseFile);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				BufferedReader br = new BufferedReader(isr)) {
			int allRows = 0;
			int usedValues = 0;
			int needToUpdateValues = 0;
			for (String line : br.lines().collect(Collectors.toList())) {
				int idx = line.indexOf("=");
				if (idx > 0) {
					String key = line.substring(0, idx).trim();
					String value = languageMessages.get(key);
					if (value == null) {
						value = line.substring(idx + 1).trim();
						needToUpdateValues++;
					} else {
						usedValues++;
					}
					resultRows.add(key + " = " + value);
				} else {
					resultRows.add(line);
				}
				allRows++;
			}
			System.out.println("Created new row lists for '" + language + "'; allRows = " + allRows + "; usedValues = "
					+ usedValues + "; needToUpdateValues = " + needToUpdateValues);
		}
		return resultRows;
	}

	private void writeNewLanguageFile(String language, List<String> resultRows) throws IOException {
		File languageFile = new File(getLanguageMessagesFullPath(language) + ".new");
		try (FileOutputStream fos = new FileOutputStream(languageFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				BufferedWriter bw = new BufferedWriter(osw)) {
			resultRows.stream().forEach(row -> {
				try {
					bw.write(row + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private String getLanguageMessagesFullPath(String language) {
		StringBuilder sb = new StringBuilder();
		sb.append(getLanguageDirectoryFullPath());
		sb.append(File.separator);
		sb.append("messages");
		if (language != null && !language.isEmpty()) {
			sb.append("_");
			sb.append(language);
		}
		sb.append(".properties");
		return sb.toString();
	}

	protected String getLanguageDirectoryFullPath() {
		return System.getProperty("user.dir") + File.separator + "data" + File.separator + "languages";
	}

}
