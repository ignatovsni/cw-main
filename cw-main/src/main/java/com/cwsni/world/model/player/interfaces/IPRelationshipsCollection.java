package com.cwsni.world.model.player.interfaces;

import java.util.Map;

public interface IPRelationshipsCollection {

	Map<Integer, IPRWar> getCountriesWithWar(Integer countryId);

	Map<Integer, IPRTruce> getCountriesWithTruce(Integer countryId);

	void makePeace(IPRWar war);

	void declareWar(Integer countryId);

}
