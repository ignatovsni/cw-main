package com.cwsni.world.client.desktop.repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.cwsni.world.model.Game;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Repository
public class GameRepository {

	private static final Log logger = LogFactory.getLog(GameRepository.class);

	final static String QUICK_SAVE_FILE_NAME = "quick-save.cw";

	public void quickSaveGame(Game game) {
		logger.info("quick save : " + game.logDescription());
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			// printToConsole(objectMapper);
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.writeValue(new File(QUICK_SAVE_FILE_NAME), game);
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
			game = objectMapper.readValue(new File(QUICK_SAVE_FILE_NAME), Game.class);
			logger.info("quick load is successful : " + game.logDescription());
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("quick load is failed ", e);
		}
		return game;
	}

}
