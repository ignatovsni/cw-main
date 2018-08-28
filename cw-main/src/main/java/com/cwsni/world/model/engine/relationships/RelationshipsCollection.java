package com.cwsni.world.model.engine.relationships;

import java.util.HashSet;
import java.util.Set;

import com.cwsni.world.model.data.relationships.DataRelationshipsCollection;
import com.cwsni.world.model.engine.Game;

public class RelationshipsCollection {

	private DataRelationshipsCollection data;
	private Set<RWar> wars;

	public void buildFrom(Game game, DataRelationshipsCollection relationships) {
		this.data = relationships;
		wars = new HashSet<>();
		data.getWars().forEach(dw -> {
			RWar war = new RWar();
			war.buildFrom(game, dw);
			wars.add(war);
		});
	}

}
