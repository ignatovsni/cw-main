package com.cwsni.world.model.engine;

import com.cwsni.world.model.data.DataTurn;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

	private DataTurn data;
	private TimeMode timeMode = TimeMode.WEEK;

	public int getDateTurn() {
		return data.getDateTurn();
	}

	public String getDateTexToDisplay() {
		return data.getDateTexToDisplay();
	}

	public int getProcessedTurn() {
		return data.getProcessedTurn();
	}

	public void increment() {
		data.setLastDateStep(timeMode.getDateTurnPerTime());
		data.setDateTurn(data.getDateTurn() + timeMode.getDateTurnPerTime());
		data.setProcessedTurn(data.getProcessedTurn() + 1);
	}

	@Override
	public String toString() {
		return getDateTexToDisplay();
	}

	/**
	 * Size of last step
	 */
	public int getLastStep() {
		return data.getLastDateStep();
	}

	/**
	 * It can be useful if I decide to use turns less than year.
	 */
	public int howManyYearsHavePassedSinceTurn(int pastTurn) {
		return (getDateTurn() - pastTurn) / TimeMode.YEAR.getDateTurnPerTime();
	}

	public int calculateFutureTurnAfterYears(int years) {
		return getDateTurn() + years * TimeMode.YEAR.getDateTurnPerTime();
	}

	@JsonIgnore
	public void setTimeMode(TimeMode timeMode) {
		this.timeMode = timeMode;
	}

	public TimeMode getTimeMode() {
		return timeMode;
	}

	public void buildFrom(Game game, DataTurn turn) {
		this.data = turn;
	}

	public double addPerWeek(double value) {
		if (TimeMode.WEEK.equals(timeMode)) {
			return value;
		}
		return value * 1.0 * timeMode.getDateTurnPerTime() / TimeMode.WEEK.getDateTurnPerTime();
	}

	public double addPerYear(double value) {
		if (TimeMode.YEAR.equals(timeMode)) {
			return value;
		}
		return value * 1.0 * timeMode.getDateTurnPerTime() / TimeMode.YEAR.getDateTurnPerTime();
	}

	public double multiplyPerWeek(double value) {
		if (TimeMode.WEEK.equals(timeMode)) {
			return value;
		}
		return Math.pow(value, 1.0 * timeMode.getDateTurnPerTime() / TimeMode.WEEK.getDateTurnPerTime());
	}

	public double multiplyPerYear(double value) {
		if (TimeMode.YEAR.equals(timeMode)) {
			return value;
		}
		return Math.pow(value, 1.0 * timeMode.getDateTurnPerTime() / TimeMode.YEAR.getDateTurnPerTime());
	}

	public double probablilityPerWeek(double value) {
		if (TimeMode.WEEK.equals(timeMode)) {
			return value;
		}
		return 1 - Math.pow(1 - value, 1.0 * timeMode.getDateTurnPerTime() / TimeMode.WEEK.getDateTurnPerTime());
	}
	
	public double probablilityPerYear(double value) {
		if (TimeMode.YEAR.equals(timeMode)) {
			return value;
		}
		return 1 - Math.pow(1 - value, 1.0 * timeMode.getDateTurnPerTime() / TimeMode.YEAR.getDateTurnPerTime());
	}

	public int getYear() {
		return data.getDateTurn() / TimeMode.YEAR.getDateTurnPerTime();
	}

}
