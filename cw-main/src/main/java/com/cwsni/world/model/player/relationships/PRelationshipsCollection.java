package com.cwsni.world.model.player.relationships;

import java.util.HashMap;
import java.util.Map;

import com.cwsni.world.game.commands.CommandDiplomacyCancelTribute;
import com.cwsni.world.game.commands.CommandDiplomacyPeace;
import com.cwsni.world.game.commands.CommandDiplomacyWar;
import com.cwsni.world.model.engine.relationships.RTribute;
import com.cwsni.world.model.engine.relationships.RTruce;
import com.cwsni.world.model.engine.relationships.RWar;
import com.cwsni.world.model.engine.relationships.RelationshipsCollection;
import com.cwsni.world.model.player.PGame;
import com.cwsni.world.model.player.interfaces.IPRTribute;
import com.cwsni.world.model.player.interfaces.IPRTruce;
import com.cwsni.world.model.player.interfaces.IPRWar;
import com.cwsni.world.model.player.interfaces.IPRelationshipsCollection;
import com.cwsni.world.util.ComparisonTool;

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
		Map<Integer, RTruce> warsWith = relationships.getCountriesWithTruce(countryId);
		Map<Integer, IPRTruce> result = new HashMap<>();
		warsWith.entrySet().forEach(e -> result.put(e.getKey(), new PRTruce(e.getValue())));
		return result;
	}

	@Override
	public Map<Integer, IPRTribute> getCountriesWithTribute(Integer countryId) {
		Map<Integer, RTribute> warsWith = relationships.getCountriesWithTribute(countryId);
		Map<Integer, IPRTribute> result = new HashMap<>();
		warsWith.entrySet().forEach(e -> result.put(e.getKey(), new PRTribute(e.getValue())));
		return result;
	}

	@Override
	public void makePeace(IPRWar war, boolean isRegularPeace, boolean isWantToBeMaster, boolean isWantToBeVassal) {
		if (war == null) {
			return;
		}
		if (!war.hasParticipant(game.getAIData().getCountryId())) {
			return;
		}
		int targetId = ComparisonTool.isEqual(game.getAIData().getCountryId(), war.getMasterId()) ? war.getSlaveId()
				: war.getMasterId();
		game.addCommand(new CommandDiplomacyPeace(targetId, isRegularPeace, isWantToBeMaster, isWantToBeVassal));
	}

	@Override
	public void declareWar(Integer countryId) {
		if (getCountriesWithWar(game.getAIData().getCountryId()).containsKey(countryId)) {
			// war is already active
			return;
		}
		game.addCommand(new CommandDiplomacyWar(countryId));
	}

	@Override
	public void cancelTribute(IPRTribute tribute) {
		int targetId = ComparisonTool.isEqual(game.getAIData().getCountryId(), tribute.getMasterId())
				? tribute.getSlaveId()
				: tribute.getMasterId();
		game.addCommand(new CommandDiplomacyCancelTribute(targetId));
	}

}
