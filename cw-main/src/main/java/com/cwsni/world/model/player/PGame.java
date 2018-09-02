package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.game.ai.AIData4Country;
import com.cwsni.world.game.commands.Command;
import com.cwsni.world.game.commands.CommandErrorHandler;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.Province;
import com.cwsni.world.model.engine.State;
import com.cwsni.world.model.player.interfaces.IData4Country;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPGame;
import com.cwsni.world.model.player.interfaces.IPGameParams;
import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.model.player.interfaces.IPRelationshipsCollection;
import com.cwsni.world.model.player.interfaces.IPState;
import com.cwsni.world.model.player.interfaces.IPTurn;
import com.cwsni.world.model.player.relationships.PRelationshipsCollection;

public class PGame implements IPGame {
	private static final Log logger = LogFactory.getLog(PGame.class);

	private Game game;
	private IPGameParams params;
	private IPRelationshipsCollection relationships;
	private Country country;
	private Map<Integer, PCountry> countries;
	private Map<Integer, PState> states;
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
		relationships = new PRelationshipsCollection(this, game.getRelationships());
		provinces = new HashMap<>();
		countries = new HashMap<>(game.getCountries().size());
		states = new HashMap<>(game.getStates().size());
		commands = new ArrayList<>();
		errorHandler = new CommandErrorHandler() {
			@Override
			public void addError(Command cmd, String e) {
				super.addError(cmd, e);
				logger.warn(e);
			}
		};
	}

	@Override
	public IPRelationshipsCollection getRelationships() {
		return relationships;
	}

	@Override
	public IPGameParams getGameParams() {
		return params;
	}

	public IPCountry getCountry() {
		return getCountry(country);
	}

	private IPCountry getCountry(Country c) {
		PCountry pc = countries.get(c.getId());
		if (pc == null) {
			pc = new PCountry(this, c);
			countries.put(c.getId(), pc);
		}
		return pc;
	}

	IPState findStateById(Integer stateId) {
		PState ps = states.get(stateId);
		if (ps == null) {
			State state = game.findStateById(stateId);
			if (state != null) {
				ps = new PState(this, state);
				states.put(stateId, ps);
			}
		}
		return ps;
	}

	IPProvince getProvince(Province p) {
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
	public double findDistance(IPProvince from, IPProvince to) {
		return game.getMap().findDistanceBetweenProvs(from.getId(), to.getId());
	}

	@Override
	public double findDistance(Integer fromId, Integer toId) {
		return game.getMap().findDistanceBetweenProvs(fromId, toId);
	}

	@Override
	public double findDistanceApproximateCountOfProvinces(IPProvince from, IPProvince to) {
		return game.getMap().findDistanceApproximateCountOfProvinces(from.getId(), to.getId());
	}

	@Override
	public List<? extends Object> findShortestPath(int fromId, int toId, IPArmy a) {
		return game.getMap().findShortestPath(fromId, toId, a.getCountry().getId());
	}

	@Override
	public IData4Country getAIData() {
		if (aiData == null) {
			aiData = new AIData4Country(this);
		}
		return aiData;
	}

	@Override
	public IPCountry findCountryById(Integer countryId) {
		return getCountry(game.findCountryById(countryId));
	}

	@Override
	public IPTurn getTurn() {
		return new PTurn(game.getTurn());
	}

	public List<Command> getCommands() {
		return Collections.unmodifiableList(commands);
	}

	public Object addCommand(Command command) {
		command.setErrorHandler(errorHandler);
		Object result = command.apply((PCountry) getCountry(country), errorHandler);
		commands.add(command);
		return result;
	}

	public void removeCommands(List<? extends Command> commandsForCancellation) {
		commands.removeAll(commandsForCancellation);
	}

}
