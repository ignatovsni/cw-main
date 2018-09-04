package com.cwsni.world.game.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.model.player.PGame;
import com.cwsni.world.model.player.interfaces.IData4Country;
import com.cwsni.world.model.player.interfaces.IPGame;
import com.cwsni.world.services.GameExecutorService;

@Component
public class AIHandler {

	private static final Log logger = LogFactory.getLog(AIHandler.class);

	@Autowired
	private JavaAIHandler javaAIHandler;

	@Autowired
	private ScriptAIHandler scriptAIHandler;

	@Autowired
	private GameExecutorService taskExecutor;
	
	@Autowired
	private ApplicationSettings applicationSettings;

	public void processNewTurn(List<PGame> pGames) {
		List<Future<?>> tasks = new ArrayList<>(pGames.size());
		CountDownLatch latch = new CountDownLatch(pGames.size());
		for (IPGame pg : pGames) {
			try {
				Future<?> task = taskExecutor.processOneAI(() -> processCountry(pg, latch));
				tasks.add(task);
			} catch (Exception e) {
				logger.error("Failed to process AI for country with id=" + pg.getAIData().getCountryId(), e);
			}
		}
		try {
			int secondsToWait = applicationSettings.getMultithreadingMaxWaitAllTasksPerTurnSeconds();
			boolean isSuccessful = latch.await(secondsToWait, TimeUnit.SECONDS);
			if (!isSuccessful) {
				logger.error("AIHandler::processNewTurn has waited too long (seconds=" + secondsToWait
						+ "). Not all tasks were completed");
				for (Future<?> task : tasks) {
					if (!task.isDone() && !task.isCancelled()) {
						task.cancel(false);
					}
				}
			}
		} catch (InterruptedException e) {
			logger.error(e);
		}
	}

	private void processCountry(IPGame game, CountDownLatch latch) {
		try {
			IData4Country data = game.getAIData();
			((AIData4Country) data).initNewTurn(javaAIHandler, scriptAIHandler.getWrapper());
			if (scriptAIHandler.hasScriptForCountry(data)) {
				try {
					scriptAIHandler.processCountry(data);
				} catch (Exception e) {
					logger.warn("failed to execute script for country.id = " + game.getAIData().getCountryId(), e);
					javaAIHandler.processCountry(data);
				}
			} else {
				javaAIHandler.processCountry(data);
			}
		} catch (Exception e) {
			logger.warn("failed to process AI for country.id = " + game.getAIData().getCountryId(), e);
		} finally {
			latch.countDown();
		}
	}

}
