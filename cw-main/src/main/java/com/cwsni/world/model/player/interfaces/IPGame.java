package com.cwsni.world.model.player.interfaces;

import java.util.List;

public interface IPGame {

	// IPCountry getCountry();

	// Integer getCountryId();

	IPGameParams getGameParams();

	IPProvince findProvById(Integer id);

	double findDistance(IPProvince from, IPProvince to);

	double findDistance(Integer fromId, Integer toId);

	double findDistanceApproximateCountOfProvinces(IPProvince from, IPProvince to);

	List<Object> findShortestPath(int fromId, int toId, IPArmy a);

	IData4Country getAIData();

	IPRelationshipsCollection getRelationships();

	IPCountry findCountryById(Integer countryId);

	IPTurn getTurn();

}