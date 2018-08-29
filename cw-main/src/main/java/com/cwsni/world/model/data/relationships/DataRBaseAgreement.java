package com.cwsni.world.model.data.relationships;

public class DataRBaseAgreement {

	protected int masterId;
	protected int slaveId;
	protected int startTurn;

	public int getMasterId() {
		return masterId;
	}

	public void setMasterId(int attackerId) {
		this.masterId = attackerId;
	}

	public int getSlaveId() {
		return slaveId;
	}

	public void setSlaveId(int defenderId) {
		this.slaveId = defenderId;
	}

	public int getStartTurn() {
		return startTurn;
	}

	public void setStartTurn(int startTurn) {
		this.startTurn = startTurn;
	}

	@Override
	public int hashCode() {
		return masterId << 10 + slaveId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null) {
			return false;
		}
		if (getClass().equals(obj.getClass())) {
			return false;
		}
		DataRBaseAgreement obj2 = (DataRBaseAgreement) obj;
		return obj2.getMasterId() == getMasterId() && obj2.getSlaveId() == getSlaveId();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(" masterId:");
		sb.append(masterId);
		sb.append(" slaveId:");
		sb.append(slaveId);
		sb.append(" startTurn:");
		sb.append(startTurn);
		return sb.toString();
	}

}
