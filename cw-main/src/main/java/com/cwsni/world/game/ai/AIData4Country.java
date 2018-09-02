package com.cwsni.world.game.ai;

import java.util.Map;

import com.cwsni.world.game.scripts.AbstractScriptHandler.ScriptHandlerWrapper;
import com.cwsni.world.model.player.PGame;
import com.cwsni.world.model.player.interfaces.IData4Country;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPGame;
import com.cwsni.world.model.player.interfaces.IPRandom;

public class AIData4Country implements IData4Country {
	private PGame game;
	private JavaAIHandler javaAIHandler;
	private ScriptHandlerWrapper scriptAIHandlerWrapper;

	public AIData4Country(PGame game) {
		this.game = game;
	}

	protected void initNewTurn(JavaAIHandler javaAIHandler, ScriptHandlerWrapper scriptAIHandlerWrapper) {
		this.javaAIHandler = javaAIHandler;
		this.scriptAIHandlerWrapper = scriptAIHandlerWrapper;
	}

	@Override
	public IPGame getGame() {
		return game;
	}

	@Override
	public IPCountry getCountry() {
		return game.getCountry();
	}

	@Override
	public Integer getCountryId() {
		return getCountry().getId();
	}

	@Override
	public JavaAIHandler getJavaAIHandler() {
		return javaAIHandler;
	}

	@Override
	public ScriptHandlerWrapper getScriptHandler() {
		return scriptAIHandlerWrapper;
	}

	@Override
	public Map<Object, Object> getAiRecords() {
		return game.getAiRecords();
	}

	@Override
	public IPRandom getRandom() {
		return game.getRandom();
	}

}
