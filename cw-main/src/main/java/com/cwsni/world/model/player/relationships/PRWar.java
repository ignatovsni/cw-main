package com.cwsni.world.model.player.relationships;

import com.cwsni.world.model.engine.ComparisonTool;
import com.cwsni.world.model.engine.relationships.RWar;

public class PRWar {

	private RWar war;

	public PRWar(RWar war) {
		this.war = war;
	}

	public boolean hasParticipant(int countryId) {
		return ComparisonTool.isEqual(countryId, war.getAttackerId())
				|| ComparisonTool.isEqual(countryId, war.getDefenderId());
	}

	public int getAttackerId() {
		return war.getAttackerId();
	}

	public int getDefenderId() {
		return war.getDefenderId();
	}

	public int getStartTurn() {
		return war.getStartTurn();
	}

}
