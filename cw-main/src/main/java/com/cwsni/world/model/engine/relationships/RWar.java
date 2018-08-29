package com.cwsni.world.model.engine.relationships;

import com.cwsni.world.model.data.relationships.DataRWar;
import com.cwsni.world.model.engine.Game;

public class RWar {

	private DataRWar data;
	private boolean attackerOfferPeace;
	private boolean defenderOfferPeace;

	DataRWar getData() {
		return data;
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof RWar)) {
			return false;
		}
		return data.equals(((RWar) obj).data);
	}

	public void buildFrom(Game game, DataRWar dw) {
		this.data = dw;
	}

	public int getAttackerId() {
		return data.getAttackerId();
	}

	public int getDefenderId() {
		return data.getDefenderId();
	}

	public void attackerOfferPeace() {
		this.attackerOfferPeace = true;
	}

	public void defenderOfferPeace() {
		this.defenderOfferPeace = true;
	}

	public boolean checkPeace() {
		return attackerOfferPeace && defenderOfferPeace;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(". attacker=");
		sb.append(data.getAttackerId());
		sb.append(", defender=");
		sb.append(data.getDefenderId());
		sb.append(". started=");
		sb.append(data.getStartTurn());
		return sb.toString();
	}

	public void resetPeaceOffer() {
		this.attackerOfferPeace = false;
		this.defenderOfferPeace = false;
	}

}
