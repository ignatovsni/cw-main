package com.cwsni.world.model.player.interfaces;

import java.util.Map;

import com.cwsni.world.game.ai.JavaAIHandler;
import com.cwsni.world.game.ai.ScriptAIHandler.ScriptAIHandlerWrapper;

public interface IData4Country {

	IPGame getGame();

	IPCountry getCountry();

	Integer getCountryId();

	JavaAIHandler getJavaAIHandler();
	
	ScriptAIHandlerWrapper getScriptHandler();
	
	Map<Object, Object> getAiRecords();

}