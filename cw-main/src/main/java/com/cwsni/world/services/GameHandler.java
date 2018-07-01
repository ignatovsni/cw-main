package com.cwsni.world.services;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.GsTimeMode;
import com.cwsni.world.game.ai.AIHandler;
import com.cwsni.world.game.commands.Command;
import com.cwsni.world.game.commands.CommandErrorHandler;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.player.PGame;

@Component
public class GameHandler {

	private static final Log logger = LogFactory.getLog(GameHandler.class);

	@Autowired
	private AIHandler aiHandler;

	public void processNewTurn(Game game, GsTimeMode timeMode, boolean autoTurn, boolean pauseBetweenTurn) {
		try {
			for (int i = 0; i < timeMode.getTurnPerTime(); i++) {
				processOneTurn(game);
			}
		} catch (Exception e) {
			logError(e);
		}
		if (autoTurn && pauseBetweenTurn) {
			try {
				Thread.sleep(50 * Math.max((10 - timeMode.getTurnPerTime()), 0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void processOneTurn(Game game) {
		List<PGame> pGames = game.getCountries().stream().map(c -> new PGame(c)).collect(Collectors.toList());
		getCommandsFromAI(pGames);
		executeCommands(game, pGames);
		game.processNewTurn();
	}

	private void executeCommands(Game game, List<PGame> pGames) {
		CommandErrorHandler errorHandler = new CommandErrorHandler();
		processCommands(game, pGames, errorHandler);
		if (!errorHandler.isEmpty()) {
			logger.info("command errors for " + game.toString());
			errorHandler.getErrors().forEach(e -> logger.info("    " + e));
		}
	}

	private void getCommandsFromAI(List<PGame> pGames) {
		aiHandler.processNewTurn(pGames);
	}

	private void processCommands(Game game, List<PGame> pGames, CommandErrorHandler errorHandler) {
		pGames.forEach(pg -> processCommands(game.findCountryById(pg.getCountryId()), pg.getCommands(), errorHandler));
	}

	private void processCommands(Country country, List<Command> commands, CommandErrorHandler errorHandler) {
		// TODO calculate collisions for armies that are moving towards each other
		commands.forEach(c -> c.apply(country, errorHandler));
	}

	private void logError(Exception e) {
		logger.error(e.getMessage(), e);
	}

}
