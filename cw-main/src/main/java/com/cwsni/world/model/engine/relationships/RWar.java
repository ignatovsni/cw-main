package com.cwsni.world.model.engine.relationships;

public class RWar extends RBaseAgreement {

	private boolean attackerRegularPeace;
	private boolean attackerWantToBeMaster;
	private boolean attackerWantToBeVassal;

	private boolean defenderRegularPeace;
	private boolean defenderWantToBeMaster;
	private boolean defenderWantToBeVassal;

	public void attackerOfferPeace(boolean isRegularPeace, boolean isWantToBeMaster, boolean isWantToBeVassal) {
		this.attackerRegularPeace = isRegularPeace;
		this.attackerWantToBeMaster = isWantToBeMaster;
		this.attackerWantToBeVassal = isWantToBeVassal;
	}

	public void defenderOfferPeace(boolean isRegularPeace, boolean isWantToBeMaster, boolean isWantToBeVassal) {
		this.defenderRegularPeace = isRegularPeace;
		this.defenderWantToBeMaster = isWantToBeMaster;
		this.defenderWantToBeVassal = isWantToBeVassal;
	}

	public boolean checkAttackerIsMasterInVassal() {
		return attackerWantToBeMaster && defenderWantToBeVassal;
	}

	public boolean checkDefenderIsMasterInVassal() {
		return defenderWantToBeMaster && attackerWantToBeVassal;
	}

	public boolean checkRegularTruce() {
		return attackerRegularPeace && defenderRegularPeace;
	}

	public void resetPeaceOffer() {
		attackerRegularPeace = false;
		attackerWantToBeMaster = false;
		attackerWantToBeVassal = false;

		defenderRegularPeace = false;
		defenderWantToBeMaster = false;
		defenderWantToBeVassal = false;
	}

}
