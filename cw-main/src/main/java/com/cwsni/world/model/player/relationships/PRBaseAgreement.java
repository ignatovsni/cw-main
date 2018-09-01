package com.cwsni.world.model.player.relationships;

import com.cwsni.world.model.engine.relationships.RBaseAgreement;
import com.cwsni.world.model.player.interfaces.IPRBaseAgreement;
import com.cwsni.world.util.ComparisonTool;

public class PRBaseAgreement implements IPRBaseAgreement {

	protected RBaseAgreement agreement;

	public PRBaseAgreement(RBaseAgreement war) {
		this.agreement = war;
	}

	@Override
	public boolean hasParticipant(int countryId) {
		return ComparisonTool.isEqual(countryId, agreement.getMasterId())
				|| ComparisonTool.isEqual(countryId, agreement.getSlaveId());
	}

	@Override
	public int getMasterId() {
		return agreement.getMasterId();
	}

	@Override
	public int getSlaveId() {
		return agreement.getSlaveId();
	}

	@Override
	public int getStartTurn() {
		return agreement.getStartTurn();
	}

}
