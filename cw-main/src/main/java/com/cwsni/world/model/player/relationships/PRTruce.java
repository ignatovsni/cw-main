package com.cwsni.world.model.player.relationships;

import com.cwsni.world.model.engine.relationships.RBaseAgreement;
import com.cwsni.world.model.engine.relationships.RTruce;
import com.cwsni.world.model.player.interfaces.IPRTruce;

public class PRTruce extends PRBaseAgreement implements IPRTruce {

	public PRTruce(RBaseAgreement truce) {
		super(truce);
	}

	@Override
	public int getEndTurn() {
		return ((RTruce) agreement).getEndTurn();
	}

}
