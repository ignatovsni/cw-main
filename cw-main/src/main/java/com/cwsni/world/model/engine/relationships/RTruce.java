package com.cwsni.world.model.engine.relationships;

import com.cwsni.world.model.data.relationships.DataRTruce;

public class RTruce extends RBaseAgreement {

	protected DataRTruce getData() {
		return (DataRTruce) data;
	}

	public int getEndTurn() {
		return getData().getEndTurn();
	}

	public void setEndTurn(int endTurn) {
		getData().setEndTurn(endTurn);
	}

}
