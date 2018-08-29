package com.cwsni.world.model.engine.relationships;

public class RWar extends RBaseAgreement {

	private boolean attackerOfferPeace;
	private boolean defenderOfferPeace;

	public void attackerOfferPeace() {
		this.attackerOfferPeace = true;
	}

	public void defenderOfferPeace() {
		this.defenderOfferPeace = true;
	}

	public boolean checkPeace() {
		return attackerOfferPeace && defenderOfferPeace;
	}

	public void resetPeaceOffer() {
		this.attackerOfferPeace = false;
		this.defenderOfferPeace = false;
	}

}
