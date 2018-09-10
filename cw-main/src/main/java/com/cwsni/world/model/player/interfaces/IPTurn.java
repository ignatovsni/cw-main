package com.cwsni.world.model.player.interfaces;

import com.cwsni.world.model.engine.TimeMode;

public interface IPTurn {

	int getYearsAfter(int turn);

	int getDateTurn();
	
	TimeMode getCurrentTimeMode();

	double multiplyPerYear(double value);

	double addPerYear(double value);

	double probablilityPerYear(double value);

}
