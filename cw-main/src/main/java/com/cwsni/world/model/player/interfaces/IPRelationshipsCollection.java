package com.cwsni.world.model.player.interfaces;

import java.util.Map;

public interface IPRelationshipsCollection {

	Map<Integer, IPRWar> getCountriesWithWar(Integer countryId);

	Map<Integer, IPRTruce> getCountriesWithTruce(Integer countryId);

	Map<Integer, IPRTribute> getCountriesWithTribute(Integer countryId);

	void makePeace(IPRWar war, boolean isRegularPeace, boolean isWantToBeMaster, boolean isWantToBeVassal);

	void declareWar(Integer countryId);
	
	public void cancelTribute(IPRTribute tribute);


}
