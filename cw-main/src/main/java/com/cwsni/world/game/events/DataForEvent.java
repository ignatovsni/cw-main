package com.cwsni.world.game.events;

import com.cwsni.world.model.engine.EventCollection;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.util.CwBaseRandom;

public class DataForEvent {

	private Game game;
	private CwBaseRandom rnd;

	public DataForEvent(Game game, CwBaseRandom randomForCurrentTurn) {
		this.game = game;
		this.rnd = randomForCurrentTurn;
	}

	public Game getGame() {
		return game;
	}

	public CwBaseRandom getRnd() {
		return rnd;
	}

	public EventCollection getEventCollection() {
		return game.getEventsCollection();
	}

}
