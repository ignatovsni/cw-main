package com.cwsni.world.client.desktop.game;

public enum GsTimeMode {
	PAUSE(0), RUN(1), RUN_2(2), RUN_5(5), RUN_10(10);
	
	private int turnPerTime;
	GsTimeMode(int turnPerTime) {
		this.turnPerTime = turnPerTime;
	}
	public int getTurnPerTime() {
		return turnPerTime;
	}
}
