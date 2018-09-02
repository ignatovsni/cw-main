package com.cwsni.world.model.player.interfaces;

import java.util.Map;

import com.cwsni.world.game.ai.JavaAIHandler;
import com.cwsni.world.game.scripts.AbstractScriptHandler.ScriptHandlerWrapper;

public interface IData4Country {

	IPGame getGame();

	IPCountry getCountry();

	Integer getCountryId();

	IPRandom getRandom();

	JavaAIHandler getJavaAIHandler();

	ScriptHandlerWrapper getScriptHandler();

	Map<Object, Object> getAiRecords();

}