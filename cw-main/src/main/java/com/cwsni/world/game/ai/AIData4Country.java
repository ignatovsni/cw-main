package com.cwsni.world.game.ai;

import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.PGame;

public class AIData4Country {
	private PGame game;
	private PCountry country;
	private JavaAIHandler javaAIHandler;

	public void initNewTurn(PGame game, PCountry country, JavaAIHandler javaAIHandler) {
		this.game = game;
		this.country = country;
		this.javaAIHandler = javaAIHandler;
	}

	public PGame getGame() {
		return game;
	}

	public PCountry getCountry() {
		return country;
	}

	public JavaAIHandler getJavaAIHandler() {
		return javaAIHandler;
	}

}
