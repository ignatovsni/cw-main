package com.cwsni.world.model.data.relationships;

import java.util.HashSet;
import java.util.Set;

public class DataRWar extends DataRBaseAgreement {

	private Set<Integer> attackerProvincesGoal = new HashSet<>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		return sb.toString();
	}

	public Set<Integer> getAttackerProvincesGoal() {
		return attackerProvincesGoal;
	}

	public void setAttackerProvincesGoal(Set<Integer> attackerProvincesGoal) {
		this.attackerProvincesGoal = attackerProvincesGoal;
	}

}
