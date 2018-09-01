package com.cwsni.world.game.ai;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.model.player.PGame;
import com.cwsni.world.model.player.interfaces.IData4Country;
import com.cwsni.world.model.player.interfaces.IPGame;

@Component
public class AIHandler {

	private static final Log logger = LogFactory.getLog(AIHandler.class);

	@Autowired
	private JavaAIHandler javaAIHandler;

	@Autowired
	private ScriptAIHandler scriptAIHandler;

	public void processNewTurn(List<PGame> pGames) {
		for (IPGame pg : pGames) {
			try {
				processCountry(pg);
			} catch (Exception e) {
				logger.error("Failed to process AI for country with id=" + pg.getAIData().getCountryId(), e);
			}
		}
	}

	private void processCountry(IPGame game) {
		IData4Country data = game.getAIData();
		((AIData4Country) data).initNewTurn(javaAIHandler);
		if (scriptAIHandler.hasScript(data)) {
			try {
				scriptAIHandler.processCountry(data);
			} catch (Exception e) {
				logger.warn("failed to execute script", e);
				javaAIHandler.processCountry(data);
			}
		} else {
			javaAIHandler.processCountry(data);
		}
	}

}
