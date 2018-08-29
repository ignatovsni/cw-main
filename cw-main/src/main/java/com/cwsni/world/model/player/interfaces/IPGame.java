package com.cwsni.world.model.player.interfaces;

import java.util.List;

import com.cwsni.world.game.ai.AIData4Country;

public interface IPGame {

	IPCountry getCountry();

	Integer getCountryId();

	IPGameParams getGameParams();

	IPProvince findProvById(Integer id);

	double findDistance(IPProvince from, IPProvince to);

	double findDistance(Integer fromId, Integer toId);

	List<Object> findShortestPath(int fromId, int toId, IPArmy a);

	AIData4Country getAIData();

	IPRelationshipsCollection getRelationships();

	IPCountry findCountryById(Integer countryId);

}