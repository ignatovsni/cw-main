package com.cwsni.world.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.GlobalLocaleMessageSource;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.game.ai.ScriptAIHandler;
import com.cwsni.world.game.events.GameEventHandler;
import com.cwsni.world.game.events.ScriptEventHandler;

@Component
public class GameGeneralController {

	private static final Log logger = LogFactory.getLog(GameGeneralController.class);

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private ScriptAIHandler scriptAIHandler;
	
	@Autowired
	private ScriptEventHandler scriptEventHandler;
	
	@Autowired
	private GameEventHandler eventHandler;

	public void resetAppCaches() {
		logger.info("resetAppCaches");
		scriptAIHandler.clearCache();
		scriptEventHandler.clearCache();
		eventHandler.clearCache();
		((GlobalLocaleMessageSource) messageSource).clearCache();
	}

}
