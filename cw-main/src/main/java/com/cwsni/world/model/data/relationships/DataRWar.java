package com.cwsni.world.model.data.relationships;

import java.util.HashSet;
import java.util.Set;

public class DataRWar {

	private int attackerId;
	private int defenderId;
	private int startTurn;
	private Set<Integer> attackerProvincesGoal = new HashSet<>();

	public int getAttackerId() {
		return attackerId;
	}

	public void setAttackerId(int attackerId) {
		this.attackerId = attackerId;
	}

	public int getDefenderId() {
		return defenderId;
	}

	public void setDefenderId(int defenderId) {
		this.defenderId = defenderId;
	}

	public int getStartTurn() {
		return startTurn;
	}

	public void setStartTurn(int startTurn) {
		this.startTurn = startTurn;
	}

	public Set<Integer> getAttackerProvincesGoal() {
		return attackerProvincesGoal;
	}

	public void setAttackerProvincesGoal(Set<Integer> attackerProvincesGoal) {
		this.attackerProvincesGoal = attackerProvincesGoal;
	}

	@Override
	public int hashCode() {
		return attackerId + defenderId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DataRWar)) {
			return false;
		}
		DataRWar obj2 = (DataRWar) obj;
		return obj2.getAttackerId() == getAttackerId() && obj2.getDefenderId() == getDefenderId();
	}

}
