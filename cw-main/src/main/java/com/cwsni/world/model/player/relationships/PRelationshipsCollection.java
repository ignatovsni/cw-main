package com.cwsni.world.model.player.relationships;

import java.util.HashMap;
import java.util.Map;

import com.cwsni.world.game.commands.CommandDiplomacyPeace;
import com.cwsni.world.game.commands.CommandDiplomacyWar;
import com.cwsni.world.model.engine.ComparisonTool;
import com.cwsni.world.model.engine.relationships.RTruce;
import com.cwsni.world.model.engine.relationships.RWar;
import com.cwsni.world.model.engine.relationships.RelationshipsCollection;
import com.cwsni.world.model.player.PGame;
import com.cwsni.world.model.player.interfaces.IPRTruce;
import com.cwsni.world.model.player.interfaces.IPRWar;
import com.cwsni.world.model.player.interfaces.IPRelationshipsCollection;

public class PRelationshipsCollection implements IPRelationshipsCollection {

	private PGame game;
	private RelationshipsCollection relationships;

	public PRelationshipsCollection(PGame game, RelationshipsCollection relationships) {
		this.game = game;
		this.relationships = relationships;
	}

	@Override
	public Map<Integer, IPRWar> getCountriesWithWar(Integer countryId) {
		Map<Integer, RWar> warsWith = relationships.getCountriesWithWar(countryId);
		Map<Integer, IPRWar> result = new HashMap<>();
		warsWith.entrySet().forEach(e -> result.put(e.getKey(), new PRWar(e.getValue())));
		return result;
	}

	@Override
	public Map<Integer, IPRTruce> getCountriesWithTruce(Integer countryId) {
		Map<Integer, RTruce> warsWith = relationships.getCountriesWithTruces(countryId);
		Map<Integer, IPRTruce> result = new HashMap<>();
		warsWith.entrySet().forEach(e -> result.put(e.getKey(), new PRTruce(e.getValue())));
		return result;
	}

	@Override
	public void makePeace(IPRWar war) {
		if (war == null) {
			return;
		}
		if (!war.hasParticipant(game.getCountryId())) {
			return;
		}
		int targetId = ComparisonTool.isEqual(game.getCountryId(), war.getMasterId()) ? war.getSlaveId()
				: war.getMasterId();
		game.addCommand(new CommandDiplomacyPeace(targetId));
	}

	@Override
	public void declareWar(Integer countryId) {
		if (getCountriesWithWar(game.getCountryId()).containsKey(countryId)) {
			// war is already active
			return;
		}
		game.addCommand(new CommandDiplomacyWar(countryId));
	}

}
