package com.cwsni.world.model.data;

public class Turn {

	private int turn;

	public Turn() {		
	}
	
	public Turn(int startTurn) {
		this.turn = startTurn;
	}

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public String getTurnTexToDisplay() {
		int year = turn - 4000;
		if (year < 0) {
			return String.valueOf(Math.abs(year)) + " BC";
		} else {
			return String.valueOf(year) + " AC";
		}
	}

	public void setTurnTexToDisplay(String v) {
		// ignore JSON loading
	}

	public void increment() {
		turn++;
	}

}
