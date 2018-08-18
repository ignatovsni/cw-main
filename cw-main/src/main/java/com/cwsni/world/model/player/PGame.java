package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.game.ai.AIData4Country;
import com.cwsni.world.game.commands.Command;
import com.cwsni.world.game.commands.CommandErrorHandler;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPGame;
import com.cwsni.world.model.player.interfaces.IPGameParams;
import com.cwsni.world.model.player.interfaces.IPProvince;

public class PGame implements IPGame {

	private Game game;
	private IPGameParams params;
	private Country country;
	private Map<Integer, PCountry> countries;
	private Map<Integer, PProvince> provinces;

	private List<Command> commands;
	private CommandErrorHandler errorHandler;

	/**
	 * Used by AI scripts. InMemoryOnly
	 */
	private AIData4Country aiData;

	public PGame(Country country) {
		this.game = country.getGame();
		this.country = country;
		this.params = new PGameParams(game.getGameParams());
		provinces = new HashMap<>();
		countries = new HashMap<>(game.getCountries().size());
		commands = new ArrayList<>();
		errorHandler = new CommandErrorHandler();
	}

	@Override
	public IPCountry getCountry() {
		return getCountry(country);
	}

	@Override
	public Integer getCountryId() {
		return country.getId();
	}

	@Override
	public IPGameParams getGameParams() {
		return params;
	}

	private IPCountry getCountry(Country c) {
		PCountry pc = countries.get(c.getId());
		if (pc == null) {
			pc = new PCountry(this, c);
			countries.put(c.getId(), pc);
		}
		return pc;
	}

	PProvince getProvince(Province p) {
		if (p == null) {
			return null;
		}
		PProvince pp = provinces.get(p.getId());
		if (pp == null) {
			pp = new PProvince(this, p);
			provinces.put(p.getId(), pp);
		}
		return pp;
	}

	@Override
	public IPProvince findProvById(Integer id) {
		return getProvince(game.getMap().findProvById(id));
	}

	@Override
	public double relativeDistance(IPProvince from, IPProvince to) {
		return relativeDistance(from.getId(), to.getId());
	}

	@Override
	public double relativeDistance(Integer fromId, Integer toId) {
		return game.getMap().findRelativeDistanceBetweenProvs(fromId, toId);
	}

	@Override
	public List<Object> findShortestPath(int fromId, int toId) {
		return game.getMap().findShortestPath(fromId, toId);
	}

	@Override
	public AIData4Country getAIData() {
		if (aiData == null) {
			aiData = new AIData4Country();
		}
		return aiData;
	}

	public List<Command> getCommands() {
		return Collections.unmodifiableList(commands);
	}

	public Object addCommand(Command command) {
		Object result = command.apply((PCountry) getCountry(country), errorHandler);
		commands.add(command);
		return result;
	}

	public void removeCommands(List commandsForCancellation) {
		commands.removeAll(commandsForCancellation);
	}

}
