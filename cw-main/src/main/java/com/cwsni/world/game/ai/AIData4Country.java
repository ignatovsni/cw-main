package com.cwsni.world.game.ai;

import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.PGame;

public class AIData4Country {
	private PGame game;
	private PCountry country;

	public void initNewTurn(PGame game, PCountry country) {
		this.game = game;
		this.country = country;
	}

	public PGame getGame() {
		return game;
	}

	public PCountry getCountry() {
		return country;
	}

}
