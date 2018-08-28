package com.cwsni.world.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.GsTimeMode;
import com.cwsni.world.game.ai.AIHandler;
import com.cwsni.world.game.commands.Command;
import com.cwsni.world.game.commands.CommandArmyCreate;
import com.cwsni.world.game.commands.CommandArmyMove;
import com.cwsni.world.game.commands.CommandArmySplit;
import com.cwsni.world.game.commands.CommandErrorHandler;
import com.cwsni.world.model.engine.Army;
import com.cwsni.world.model.engine.ComparisonTool;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.ProvinceBorder;
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
		try {
			preProcessCommands(game, pGames, errorHandler);
			processNewArmyCommands(game, pGames);
			checkArmyMovementCollisions(game, pGames, errorHandler);
			processCommands(game, pGames);
		} finally {
			if (!errorHandler.isEmpty()) {
				logger.info("command errors for " + game.toString());
				errorHandler.getErrors().forEach(e -> logger.info("    " + e));
			}
		}
		game.resetNewArmiesWithIdLessThanZero();
	}

	private void getCommandsFromAI(List<PGame> pGames) {
		aiHandler.processNewTurn(pGames);
	}

	private void preProcessCommands(Game game, List<PGame> pGames, CommandErrorHandler errorHandler) {
		pGames.forEach(pg -> {
			Country country = game.findCountryById(pg.getCountryId());
			pg.getCommands().forEach(c -> {
				c.setCountry(country);
				c.setErrorHandler(errorHandler);
			});
		});
	}

	private void processNewArmyCommands(Game game, List<PGame> pGames) {
		pGames.forEach(pg -> {
			List<Command> createArmyCommands = pg.getCommands().stream()
					.filter(c -> (c instanceof CommandArmyCreate) || (c instanceof CommandArmySplit))
					.collect(Collectors.toList());
			createArmyCommands.forEach(c -> c.apply());
			pg.removeCommands(createArmyCommands);
		});
	}

	private void processCommands(Game game, List<PGame> pGames) {
		pGames.forEach(pg -> pg.getCommands().forEach(c -> c.apply()));
	}

	private void checkArmyMovementCollisions(Game game, List<PGame> pGames, CommandErrorHandler errorHandler) {
		List<CommandArmyMove> commandsForCancellation = new ArrayList<>();
		Map<ProvinceBorder, List<CommandArmyMove>> movings = new HashMap<>();
		// fill movings
		for (PGame pg : pGames) {
			for (Command pgCmd : pg.getCommands()) {
				if (!(pgCmd instanceof CommandArmyMove)) {
					continue;
				}
				CommandArmyMove cmd = (CommandArmyMove) pgCmd;
				Army army = game.findArmyByIdForCommand(cmd.getCountryId(), cmd.getArmyId());
				if (army == null) {
					errorHandler.addError(cmd, "army not found in checkArmyMovementCollisions");
					continue;
				}
				ProvinceBorder pb = new ProvinceBorder(cmd.getDestinationProvId(), army.getLocation().getId());
				List<CommandArmyMove> movsFromToLocation = movings.get(pb);
				if (movsFromToLocation == null) {
					movsFromToLocation = new ArrayList<>();
					movings.put(pb, movsFromToLocation);
				}
				movsFromToLocation.add(cmd);
			}
		}
		// check collisions
		for (Entry<ProvinceBorder, List<CommandArmyMove>> entry : movings.entrySet()) {
			ProvinceBorder pb = entry.getKey();
			List<CommandArmyMove> commands = entry.getValue();
			List<CommandArmyMove> list1 = new ArrayList<>();
			List<CommandArmyMove> list2 = new ArrayList<>();
			commands.forEach(cmd -> {
				if (ComparisonTool.isEqual(cmd.getDestinationProvId(), pb.getFirst())) {
					list1.add(cmd);
				} else {
					list2.add(cmd);
				}
			});
			for (CommandArmyMove cmd1 : list1) {
				Army army1 = game.findArmyByIdForCommand(cmd1.getCountryId(), cmd1.getArmyId());
				for (CommandArmyMove cmd2 : list2) {
					Army army2 = game.findArmyByIdForCommand(cmd2.getCountryId(), cmd2.getArmyId());
					if (!ComparisonTool.isEqual(army1.getCountry().getId(), army2.getCountry().getId())) {
						if (army1.getStrength() > army2.getStrength()) {
							commandsForCancellation.add(cmd2);
						} else {
							commandsForCancellation.add(cmd1);
						}
					}
				}
			}
		}
		// cancel commands
		pGames.forEach(pg -> pg.removeCommands(commandsForCancellation));
	}

	private void logError(Exception e) {
		logger.error(e.getMessage(), e);
	}

}
