package com.cwsni.world.model.data;

import com.cwsni.world.model.engine.TimeMode;

public class DataTurn {

	/**
	 * Represents date.
	 */
	private int dateTurn;
	/**
	 * How many real turns were processed, how many game.processNewTurn() was
	 * invoked.
	 */
	private int processedTurn;

	private int lastDateStep;

	public int getDateTurn() {
		return dateTurn;
	}

	public void setDateTurn(int dateTurn) {
		this.dateTurn = dateTurn;
	}

	public int getProcessedTurn() {
		return processedTurn;
	}

	public void setProcessedTurn(int processedTurn) {
		this.processedTurn = processedTurn;
	}

	public String getDateTexToDisplay() {
		return getDateTexToDisplay(dateTurn);
	}

	public String getDateTexToDisplay(int turn) {
		int year = turn / TimeMode.YEAR.getDateTurnPerTime() - 4000;
		int week = turn % TimeMode.YEAR.getDateTurnPerTime();
		StringBuilder sb = new StringBuilder();
		sb.append(Math.abs(year));
		if (year < 0) {
			sb.append(" BC");
		} else {
			sb.append(" AC");
		}
		sb.append(" - ");
		sb.append(week);
		return sb.toString();
	}

	public void setDateTexToDisplay(String v) {
		// ignore JSON loading
	}

	public int getLastDateStep() {
		return lastDateStep;
	}

	public void setLastDateStep(int lastDateStep) {
		this.lastDateStep = lastDateStep;
	}

}
