package com.cwsni.world.model.engine.relationships;

import com.cwsni.world.model.data.relationships.DataRBaseAgreement;
import com.cwsni.world.model.engine.Game;

public class RBaseAgreement {

	protected DataRBaseAgreement data;

	protected DataRBaseAgreement getData() {
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
		if (!(obj instanceof RBaseAgreement)) {
			return false;
		}
		return data.equals(((RBaseAgreement) obj).data);
	}

	public void buildFrom(Game game, DataRBaseAgreement data) {
		this.data = data;
	}

	public int getMasterId() {
		return data.getMasterId();
	}

	public int getSlaveId() {
		return data.getSlaveId();
	}

	public int getStartTurn() {
		return data.getStartTurn();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(". attacker=");
		sb.append(data.getMasterId());
		sb.append(", defender=");
		sb.append(data.getSlaveId());
		sb.append(". started=");
		sb.append(data.getStartTurn());
		return sb.toString();
	}

}
