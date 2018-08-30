package com.cwsni.world.model.player.relationships;

import com.cwsni.world.model.engine.relationships.RBaseAgreement;
import com.cwsni.world.model.engine.relationships.RVassal;
import com.cwsni.world.model.player.interfaces.IPRVassal;

public class PRVassal extends PRBaseAgreement implements IPRVassal {

	public PRVassal(RBaseAgreement truce) {
		super(truce);
	}

	@Override
	public double getTax() {
		return ((RVassal) agreement).getTax();
	}

}
