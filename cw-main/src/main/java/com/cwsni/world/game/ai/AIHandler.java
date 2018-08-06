package com.cwsni.world.game.ai;

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.PGame;

@Component
public class AIHandler {

	private static final Log logger = LogFactory.getLog(AIHandler.class);

	@Autowired
	private JavaAIHandler javaAIHandler;

	@Autowired
	private ScriptAIHandler scriptAIHandler;

	public void processNewTurn(List<PGame> pGames) {
		pGames.forEach(pg -> processCountry(pg, pg.getCountry()));
	}

	private void processCountry(PGame game, PCountry c) {
		AIData4Country data = game.getAIData();
		data.initNewTurn(game, c, javaAIHandler);
		processMethod(data, (handler, d) -> handler.processCountry(d));
	}

	private void processMethod(AIData4Country data, BiConsumer<IAIHandler, AIData4Country> func) {
		if (scriptAIHandler.hasScript(data)) {
			try {
				func.accept(scriptAIHandler, data);
			} catch (Exception e) {
				logger.warn("failed to execute script", e);
				func.accept(javaAIHandler, data);
			}
		} else {
			func.accept(javaAIHandler, data);
		}
	}

}
