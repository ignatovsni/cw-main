package com.cwsni.world.model.data.relationships;

public class DataRTruce extends DataRBaseAgreement {

	private int endTurn;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		return sb.toString();
	}

	public int getEndTurn() {
		return endTurn;
	}

	public void setEndTurn(int endTurn) {
		this.endTurn = endTurn;
	}

}
