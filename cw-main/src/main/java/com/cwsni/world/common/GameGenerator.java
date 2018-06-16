package com.cwsni.world.common;

import java.util.Random;

import org.springframework.stereotype.Component;

import com.cwsni.world.model.Game;
import com.cwsni.world.model.Population;
import com.cwsni.world.model.WorldMap;

@Component
public class GameGenerator {

	public Game createTestGame(int rows, int columns, int provinceRadius) {
		Game game = new Game();
		createMap(rows, columns, provinceRadius, game);
		fillPopulation(game);
		game.postConstruct();
		return game;
	}

	private void createMap(int rows, int columns, int provinceRadius, Game game) {
		WorldMap map = WorldMap.createMap(rows, columns, provinceRadius);
		game.setMap(map);
	}

	private void fillPopulation(Game game) {
		game.getMap().getProvinces().forEach(p -> {
			Population pop = new Population();
			//pop.setAmount(new Random().nextInt(1000));
			pop.setAmount(p.getId() * 100);
			p.getPopulation().clear();
			p.getPopulation().add(pop);
		});
	}

}
