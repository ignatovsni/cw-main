package com.cwsni.world.model.player;

import com.cwsni.world.model.engine.TimeMode;
import com.cwsni.world.model.engine.Turn;
import com.cwsni.world.model.player.interfaces.IPTurn;

public class PTurn implements IPTurn {

	private Turn turn;

	public PTurn(Turn turn) {
		this.turn = turn;
	}
	
	@Override
	public int getDateTurn() {
		return turn.getDateTurn();
	}
	
	@Override
	public int getYearsAfter(int turn) {
		return this.turn.howManyYearsHavePassedSinceTurn(turn);
	}

	@Override
	public TimeMode getCurrentTimeMode() {
		return this.turn.getTimeMode();
	}
	
	@Override
	public double probablilityPerYear(double value) {
		return turn.probablilityPerYear(value);
	}
	
	@Override
	public double addPerYear(double value) {
		return turn.addPerYear(value);
	}

	@Override
	public double multiplyPerYear(double value) {
		return turn.multiplyPerYear(value);
	}

}
