package com.cwsni.world.model.player.interfaces;

import com.cwsni.world.model.engine.TimeMode;

public interface IPTurn {

	int getYearsAfter(int turn);

	int getCurrentTurn();
	
	TimeMode getCurrentTimeMode();

}
