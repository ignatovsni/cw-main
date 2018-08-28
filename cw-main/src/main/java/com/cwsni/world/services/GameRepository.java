package com.cwsni.world.services;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cwsni.world.CwException;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.services.algorithms.GameAlgorithms;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Repository
public class GameRepository {

	private static final Log logger = LogFactory.getLog(GameRepository.class);

	final static String QUICK_SAVE_FILE_NAME = "quick-save.cw";

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private GameAlgorithms gameAlgorithms;
	
	@Autowired
	private GameEventListener gameEventListener;

	public void quickSaveGame(Game game) {
		logger.info("quick save : " + game.logDescription());
		File file = new File(QUICK_SAVE_FILE_NAME);
		saveGame(game, file);
	}

	public void saveGame(Game game, File file) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			// printToConsole(objectMapper);
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.writeValue(file, game.getSaveData());
			logger.info("saving is successful : " + game.logDescription());
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("saving is failed : " + game.logDescription(), e);
			throw new CwException(e.getMessage(), e);
		}
	}

	/*
	 * private void printToConsole(ObjectMapper objectMapper) throws
	 * JsonProcessingException { String jsonInString =
	 * objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
	 * System.out.println("JSON is\n" + jsonInString); }
	 */

	public Game quickLoadGame() {
		File file = new File(QUICK_SAVE_FILE_NAME);
		logger.info("quick load");
		Game game = loadGame(file);
		return game;
	}

	public Game loadGame(File file) {
		ObjectMapper objectMapper = new ObjectMapper();
		Game game = null;
		try {
			DataGame dataGame = objectMapper.readValue(file, DataGame.class);
			game = new Game();
			game.buildFrom(dataGame, messageSource, gameAlgorithms, gameEventListener);
			logger.info("loading is successful : " + game.logDescription());
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("loading is failed ", e);
			throw new CwException(e.getMessage(), e);
		}
		return game;
	}

}
