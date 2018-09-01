package com.cwsni.world.model.player.interfaces;

import com.cwsni.world.game.ai.JavaAIHandler;

public interface IData4Country {

	IPGame getGame();

	IPCountry getCountry();

	Integer getCountryId();

	JavaAIHandler getJavaAIHandler();

}