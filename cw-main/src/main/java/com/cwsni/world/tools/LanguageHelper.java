package com.cwsni.world.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

	private Map<String, String> readLanguageFile(String language) throws FileNotFoundException, IOException {
		File languageFile = new File(getLanguageMessagesFullPath(language));

		Map<String, String> languageMessages = new HashMap<>();
		FileReader in = new FileReader(languageFile);
		BufferedReader br = new BufferedReader(in);
		br.lines().forEach(line -> {
			int idx = line.indexOf("=");
			if (idx > 0) {
				languageMessages.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
			}
		});
		br.close();
		in.close();
		return languageMessages;
	}

	private List<String> createResultRows(String language, Map<String, String> languageMessages)
			throws FileNotFoundException, IOException {
		String languageDirectory = getLanguageDirectoryFullPath();
		List<String> resultRows = new ArrayList<>();
		File baseFile = new File(languageDirectory + File.separator + "messages.properties");
		FileReader in = new FileReader(baseFile);
		BufferedReader br = new BufferedReader(in);
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
		br.close();
		in.close();
		System.out.println("Created new row lists for '" + language + "'; allRows = " + allRows + "; usedValues = "
				+ usedValues + "; needToUpdateValues = " + needToUpdateValues);
		return resultRows;
	}

	private void writeNewLanguageFile(String language, List<String> resultRows) throws IOException {
		File languageFile = new File(getLanguageMessagesFullPath(language) + ".new");
		FileWriter out = new FileWriter(languageFile);
		BufferedWriter bw = new BufferedWriter(out);
		resultRows.stream().forEach(row -> {
			try {
				bw.write(row);
				bw.write("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		bw.close();
		out.close();
	}

	private String getLanguageMessagesFullPath(String language) {
		return getLanguageDirectoryFullPath() + File.separator + "messages_" + language + ".properties";
	}

	private String getLanguageDirectoryFullPath() {
		return System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator
				+ "resources" + File.separator + "messages";
	}

}
