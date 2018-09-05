package com.cwsni.world.model.data;

import java.util.HashMap;
import java.util.Map;

public class DataEvent {

	private String type;
	private int id;
	private int startTurn;
	private Map<Object, Object> data = new HashMap<>();

	@Override
	public int hashCode() {
		return getType().hashCode() + getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DataEvent)) {
			return false;
		}
		DataEvent otherEvent = (DataEvent) obj;
		return getId() == otherEvent.getId() && getType().equals(otherEvent.getType());
	}

	@Override
	public String toString() {
		return "DataEvent: type = " + getType() + ", id = " + getId() + "";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map<Object, Object> getData() {
		return data;
	}

	public void setData(Map<Object, Object> data) {
		this.data = data;
	}

	public int getStartTurn() {
		return startTurn;
	}

	public void setStartTurn(int startTurn) {
		this.startTurn = startTurn;
	}

}
