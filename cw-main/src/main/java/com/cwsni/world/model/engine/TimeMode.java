package com.cwsni.world.model.engine;

public enum TimeMode {
	PAUSE(0), WEEK(1), MONTH(4), YEAR(50), YEAR_10(500);
	
	private int dateTurnPerTime;
	TimeMode(int dateTurnPerTime) {
		this.dateTurnPerTime = dateTurnPerTime;
	}
	public int getDateTurnPerTime() {
		return dateTurnPerTime;
	}
}
