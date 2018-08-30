package com.cwsni.world.model.player.interfaces;

import java.util.Map;

public interface IPRelationshipsCollection {

	Map<Integer, IPRWar> getCountriesWithWar(Integer countryId);

	Map<Integer, IPRTruce> getCountriesWithTruce(Integer countryId);

	void makePeace(IPRWar war, boolean isRegularPeace, boolean isWantToBeMaster, boolean isWantToBeVassal);

	void declareWar(Integer countryId);

	Map<Integer, IPRVassal> getCountriesWithVassal(Integer countryId);

}
