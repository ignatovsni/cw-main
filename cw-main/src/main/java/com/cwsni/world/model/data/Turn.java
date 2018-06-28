package com.cwsni.world.model.data;

public class Turn {
	/*
	 * maybe make coefficient for lesser period of time (population growth & so
	 * one). or just increase once per year
	 * 
	 * month or week(?) - minimum step
	 * 
	 * https://www.vox.com/world/2018/6/19/17469176/roman-empire-maps-history-
	 * explained
	 * 
	 * The Mediterranean was a big help in getting around — most coastal locations
	 * in the western Mediterranean could be reached in under a week, and even
	 * far-flung coastal cities like Alexandria and Jerusalem could be reached in
	 * two weeks. But traveling to the interior was more difficult. Reaching the
	 * most distant points in the empire, such as Britain, could take close to a
	 * month. And of course, going from one end of the empire to the other could
	 * take even longer. The researchers estimate that it took seven weeks to travel
	 * from Constantinople (at the eastern end of the empire) to London (in the far
	 * west
	 */
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
