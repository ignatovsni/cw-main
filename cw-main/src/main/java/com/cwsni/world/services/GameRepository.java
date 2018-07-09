package com.cwsni.world.services;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.data.DataGame;
import com.cwsni.world.services.algorithms.GameAlgorithms;
import com.fasterxml.jackson.core.JsonProcessingException;
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

	public void quickSaveGame(Game game) {
		logger.info("quick save : " + game.logDescription());
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			// printToConsole(objectMapper);
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.writeValue(new File(QUICK_SAVE_FILE_NAME), game.getSaveData());
			logger.info("quick save is successful : " + game.logDescription());
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("quick save is failed : " + game.logDescription(), e);
		}
	}

	private void printToConsole(ObjectMapper objectMapper) throws JsonProcessingException {
		String jsonInString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		System.out.println("JSON is\n" + jsonInString);
	}

	public Game quickLoadGame() {
		logger.info("quick load");
		ObjectMapper objectMapper = new ObjectMapper();
		Game game = null;
		try {
			DataGame dataGame = objectMapper.readValue(new File(QUICK_SAVE_FILE_NAME), DataGame.class);
			game = new Game();
			game.buildFrom(dataGame, messageSource, gameAlgorithms);
			logger.info("quick load is successful : " + game.logDescription());
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("quick load is failed ", e);
			game = null;
		}
		return game;
	}

}
