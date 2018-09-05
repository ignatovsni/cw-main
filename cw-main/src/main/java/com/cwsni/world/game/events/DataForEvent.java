package com.cwsni.world.game.events;

import com.cwsni.world.game.scripts.AbstractScriptHandler.ScriptHandlerWrapper;
import com.cwsni.world.model.engine.EventCollection;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.util.CwBaseRandom;

public class DataForEvent {

	private Game game;
	private CwBaseRandom rnd;
	private ScriptHandlerWrapper scriptEventsWrapper;

	public DataForEvent(Game game, CwBaseRandom randomForCurrentTurn, ScriptHandlerWrapper scriptEventsWrapper) {
		this.game = game;
		this.rnd = randomForCurrentTurn;
		this.scriptEventsWrapper = scriptEventsWrapper;
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
	
	public ScriptHandlerWrapper getScriptHandler() {
		return scriptEventsWrapper;
	}

}
