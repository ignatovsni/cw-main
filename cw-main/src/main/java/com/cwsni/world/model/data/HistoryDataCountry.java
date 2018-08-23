package com.cwsni.world.model.data;

public class HistoryDataCountry {

	private int turnOfRecord;
	private int id;
	private String name;
	private Color color;
	private Integer firstCapital;
	private String aiScriptName;

	public HistoryDataCountry() {
	}

	public HistoryDataCountry(DataCountry dc, int turnOfRecord) {
		this.turnOfRecord = turnOfRecord;
		this.id = dc.getId();
		this.name = dc.getName();
		this.color = new Color(dc.getColor());
		this.firstCapital = dc.getCapital();
		this.aiScriptName = dc.getAiScriptName();
	}

	public void copyTo(DataCountry dc) {
		dc.setId(getId());
		dc.setName(getName());
		dc.setColor(new Color(getColor()));
		dc.setFirstCapital(getFirstCapital());
		dc.setAiScriptName(getAiScriptName());
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

}
