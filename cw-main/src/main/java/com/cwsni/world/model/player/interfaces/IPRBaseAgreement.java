package com.cwsni.world.model.player.interfaces;

public interface IPRBaseAgreement {

	int getStartTurn();

	int getSlaveId();

	int getMasterId();

	boolean hasParticipant(int countryId);

}
