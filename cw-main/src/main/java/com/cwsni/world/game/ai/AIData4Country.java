package com.cwsni.world.game.ai;

import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPGame;

public class AIData4Country {
	private IPGame game;
	private IPCountry country;
	private JavaAIHandler javaAIHandler;

	public void initNewTurn(IPGame game, IPCountry country, JavaAIHandler javaAIHandler) {
		this.game = game;
		this.country = country;
		this.javaAIHandler = javaAIHandler;
	}

	public IPGame getGame() {
		return game;
	}

	public IPCountry getCountry() {
		return country;
	}

	public JavaAIHandler getJavaAIHandler() {
		return javaAIHandler;
	}

}
