package com.cwsni.world.game.ai;

import com.cwsni.world.game.ai.ScriptAIHandler.ScriptAIHandlerWrapper;
import com.cwsni.world.model.player.PGame;
import com.cwsni.world.model.player.interfaces.IData4Country;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPGame;

public class AIData4Country implements IData4Country {
	private PGame game;
	private JavaAIHandler javaAIHandler;
	private ScriptAIHandlerWrapper scriptAIHandlerWrapper;

	public AIData4Country(PGame game) {
		this.game = game;
	}

	protected void initNewTurn(JavaAIHandler javaAIHandler, ScriptAIHandlerWrapper scriptAIHandlerWrapper) {
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
	public ScriptAIHandlerWrapper getScriptHandler() {
		return scriptAIHandlerWrapper;
	}

}
