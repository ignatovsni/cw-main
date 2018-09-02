package com.cwsni.world.game.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {

	@Autowired
	private ScriptEventHandler scriptEventHandler;

	public void clearCache() {
		// TODO Auto-generated method stub
		
	}

}
