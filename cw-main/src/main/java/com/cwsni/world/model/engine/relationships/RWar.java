package com.cwsni.world.model.engine.relationships;

public class RWar extends RBaseAgreement {

	private boolean attackerRegularPeace;
	private boolean attackerWantToBeMaster;
	private boolean attackerWantToBeSlave;

	private boolean defenderRegularPeace;
	private boolean defenderWantToBeMaster;
	private boolean defenderWantToBeSlave;

	public void attackerOfferPeace(boolean isRegularPeace, boolean isWantToBeMaster, boolean isWantToBeSlave) {
		this.attackerRegularPeace = isRegularPeace;
		this.attackerWantToBeMaster = isWantToBeMaster;
		this.attackerWantToBeSlave = isWantToBeSlave;
	}

	public void defenderOfferPeace(boolean isRegularPeace, boolean isWantToBeMaster, boolean isWantToBeSlave) {
		this.defenderRegularPeace = isRegularPeace;
		this.defenderWantToBeMaster = isWantToBeMaster;
		this.defenderWantToBeSlave = isWantToBeSlave;
	}

	public boolean checkAttackerIsMasterInTribute() {
		return attackerWantToBeMaster && defenderWantToBeSlave;
	}

	public boolean checkDefenderIsMasterInTribute() {
		return defenderWantToBeMaster && attackerWantToBeSlave;
	}

	public boolean checkRegularTruce() {
		return attackerRegularPeace && defenderRegularPeace;
	}

	public void resetPeaceOffer() {
		attackerRegularPeace = false;
		attackerWantToBeMaster = false;
		attackerWantToBeSlave = false;

		defenderRegularPeace = false;
		defenderWantToBeMaster = false;
		defenderWantToBeSlave = false;
	}

}
