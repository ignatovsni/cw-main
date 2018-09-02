package com.cwsni.world.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.DefaultLocaleMessageSource;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.game.ai.ScriptAIHandler;

@Component
public class GameGeneralController {

	private static final Log logger = LogFactory.getLog(GameGeneralController.class);

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private ScriptAIHandler scriptAIHandler;

	public void resetAppCaches() {
		logger.info("resetAppCaches");
		scriptAIHandler.clearCache();
		((DefaultLocaleMessageSource) messageSource).clearCache();
	}

}
