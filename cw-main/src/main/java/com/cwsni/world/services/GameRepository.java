package com.cwsni.world.services;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.util.CwException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Repository
public class GameRepository {

	private static final Log logger = LogFactory.getLog(GameRepository.class);

	final static String QUICK_SAVE_FILE_NAME = "quick-save.cw";
	final static String SAVE_DIRECTORY = "saves";

	@Autowired
	private ApplicationSettings applicationSettings;

	@Autowired
	private PlayerEventListener gameEventListener;

	@PostConstruct
	public void init() {
		File saveDirectory = new File(getSaveDirectoryFullPath());
		if (!saveDirectory.exists()) {
			saveDirectory.mkdirs();
			logger.info("created save directory: " + saveDirectory.getAbsolutePath());
		} else if (!saveDirectory.isDirectory()) {
			logger.error(saveDirectory.getAbsolutePath() + " is not directory");
		}
	}

	public String getSaveDirectoryFullPath() {
		return System.getProperty("user.dir") + File.separator + SAVE_DIRECTORY;
	}

	private String getQuickSaveFullPath() {
		return getSaveDirectoryFullPath() + File.separator + QUICK_SAVE_FILE_NAME;
	}

	public void quickSaveGame(Game game) {
		if (game.getMap().getProvinces().isEmpty()) {
			logger.trace("do not quick save because the map is empty");
			return;
		}
		logger.trace("quick save : " + game.logDescription());
		File file = new File(getQuickSaveFullPath());
		saveGame(game, file);
	}

	public void saveGame(Game game, File file) {
		try {
			checkAndCleanAiRecords(game);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.writeValue(file, game.getSaveData());
			logger.trace("saving is successful : " + game.logDescription());
		} catch (IOException e) {
			logger.error("saving is failed : " + game.logDescription(), e);
			throw new CwException(e.getMessage(), e);
		}
	}

	private void checkAndCleanAiRecords(Game game) {
		game.getCountries().forEach(c -> c.checkAndCleanAiRecords());
	}

	public Game quickLoadGame() {
		File file = new File(getQuickSaveFullPath());
		logger.trace("quick load");
		Game game = loadGame(file);
		return game;
	}

	public Game loadGame(File file) {
		ObjectMapper objectMapper = new ObjectMapper();
		Game game = null;
		try {
			DataGame dataGame = objectMapper.readValue(file, DataGame.class);
			game = new Game();
			game.buildFrom(dataGame, gameEventListener);
			logger.trace("loading is successful : " + game.logDescription());
		} catch (IOException e) {
			logger.error("loading is failed ", e);
			throw new CwException(e.getMessage(), e);
		}
		return game;
	}

	public void autoSave(Game game) {
		if (!applicationSettings.isUseAutoSave()) {
			return;
		}
		int processedTurn = game.getTurn().getProcessedTurn();
		if (game.getLastAutoSaveTurn() > processedTurn - applicationSettings.getAutoSaveTurnStep()) {
			return;
		}
		// save
		saveGame(game, new File(
				getSaveDirectoryFullPath() + File.separator + "autosave-" + game.getTurn().getDateTexToDisplay() + ".cw"));
		game.setLastAutoSaveTurn(processedTurn);

		// delete old autosaves
		File saveDirectory = new File(getSaveDirectoryFullPath());
		File[] files = saveDirectory.listFiles((dir, name) -> name.startsWith("autosave-") && name.endsWith(".cw"));
		Arrays.sort(files, (x, y) -> x.lastModified() < y.lastModified() ? 1 : -1);
		for (int i = applicationSettings.getAutoSaveMaxFiles(); i < files.length; i++) {
			files[i].delete();
		}
	}

}
