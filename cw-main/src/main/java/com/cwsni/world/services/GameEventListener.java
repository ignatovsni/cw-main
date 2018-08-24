package com.cwsni.world.services;

import org.springframework.stereotype.Component;

import com.cwsni.world.model.Game;

@Component
public class GameEventListener {

	// private static final Log logger = LogFactory.getLog(GameEventListener.class);

	public void event(Game game, String txt) {
		// System.out.println("[game event | " + game.getTurn().getTurn() + " | " +
		// game.getTurn() + "] " + txt);
	}

}
