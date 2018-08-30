package com.cwsni.world.model.player.relationships;

import com.cwsni.world.model.engine.relationships.RBaseAgreement;
import com.cwsni.world.model.engine.relationships.RTribute;
import com.cwsni.world.model.player.interfaces.IPRTribute;

public class PRTribute extends PRBaseAgreement implements IPRTribute {

	public PRTribute(RBaseAgreement truce) {
		super(truce);
	}

	@Override
	public double getTax() {
		return ((RTribute) agreement).getTax();
	}

}
