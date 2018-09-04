package com.cwsni.world.model.data;

import com.cwsni.world.model.engine.Turn;

public class HistoryDataCountry {

	private static final int MAX_AGE_OF_COUNTRY_RECORD = 2000;

	// ---------------- special history data
	private int turnOfRecord;
	private int firstTurnOfDisappearance;

	// ---------------- regular country data
	private int id;
	private String name;
	private Color color;
	private Integer firstCapital;
	private int turnOfCreation;
	private int turnsOfExistence;
	private String aiScriptName;

	public HistoryDataCountry() {
	}

	public void update(DataCountry dc, int turnOfRecord) {
		this.turnOfRecord = turnOfRecord;
		if (this.firstTurnOfDisappearance == 0) {
			this.firstTurnOfDisappearance = turnOfRecord;
		}
		this.id = dc.getId();
		this.name = dc.getName();
		this.color = new Color(dc.getColor());
		this.firstCapital = dc.getFirstCapital();
		this.aiScriptName = dc.getAiScriptName();
		this.turnOfCreation = dc.getTurnOfCreation();
		this.turnsOfExistence = dc.getTurnsOfExistence();
	}

	public void copyTo(DataCountry dc) {
		dc.setId(getId());
		dc.setName(getName());
		dc.setColor(new Color(getColor()));
		dc.setFirstCapital(getFirstCapital());
		dc.setAiScriptName(getAiScriptName());
		dc.setTurnOfCreation(getTurnOfCreation());
		dc.setTurnsOfExistence(getTurnsOfExistence());
	}

	public boolean isCanBeRemoved(Turn turn) {
		int turnNumber = turn.getDateTurn();
		return getTurnOfRecord() < turnNumber - MAX_AGE_OF_COUNTRY_RECORD
				|| getFirstTurnOfDisappearance() < (turnNumber - getTurnsOfExistence());
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof HistoryDataCountry)) {
			return false;
		}
		return ((HistoryDataCountry) obj).getId() == getId();
	}

	@Override
	public String toString() {
		return "HistoryDataCountry with id " + getId();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Integer getFirstCapital() {
		return firstCapital;
	}

	public void setFirstCapital(Integer firstCapital) {
		this.firstCapital = firstCapital;
	}

	public String getAiScriptName() {
		return aiScriptName;
	}

	public void setAiScriptName(String aiScriptName) {
		this.aiScriptName = aiScriptName;
	}

	public int getTurnOfRecord() {
		return turnOfRecord;
	}

	public void setTurnOfRecord(int turnOfRecord) {
		this.turnOfRecord = turnOfRecord;
	}

	public int getTurnOfCreation() {
		return turnOfCreation;
	}

	public void setTurnOfCreation(int turnOfCreation) {
		this.turnOfCreation = turnOfCreation;
	}

	public int getTurnsOfExistence() {
		return turnsOfExistence;
	}

	public void setTurnsOfExistence(int turnsOfExistence) {
		this.turnsOfExistence = turnsOfExistence;
	}

	public int getFirstTurnOfDisappearance() {
		return firstTurnOfDisappearance;
	}

	public void setFirstTurnOfDisappearance(int firstTurnOfDisappearance) {
		this.firstTurnOfDisappearance = firstTurnOfDisappearance;
	}

}
