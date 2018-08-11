package com.cwsni.world.model.player.interfaces;

import java.util.List;

import com.cwsni.world.game.ai.AIData4Country;

public interface IPGame {

	IPCountry getCountry();

	Integer getCountryId();

	IPGameParams getGameParams();

	IPProvince getProvince(Integer id);

	double relativeDistance(IPProvince from, IPProvince to);

	double relativeDistance(Integer fromId, Integer toId);

	List<Object> findShortestPath(int fromId, int toId);

	AIData4Country getAIData();

}