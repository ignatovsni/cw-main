package com.cwsni.world.services;

import org.springframework.stereotype.Component;

/**
 * The purpose: to avoid ConcurrentModificationException when game engine is
 * changing game data model and UI is refreshing visual components.
 */
@Component
public class GameDataModelLocker {

	private Object lockObj = new Object();

	public void runLocked(Runnable r) {
		synchronized (lockObj) {
			r.run();
		}
	}

}
