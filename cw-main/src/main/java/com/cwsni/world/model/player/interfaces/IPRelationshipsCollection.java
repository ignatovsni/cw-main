package com.cwsni.world.model.player.interfaces;

import java.util.Map;

import com.cwsni.world.model.player.relationships.PRWar;

public interface IPRelationshipsCollection {

	Map<Integer, PRWar> getCountriesWithWar(Integer countryId);

	void makePeace(PRWar war);

	void declareWar(Integer countryId);

}
