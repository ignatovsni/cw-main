package com.cwsni.world.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.game.ai.AIData4Country;
import com.cwsni.world.game.commands.Command;
import com.cwsni.world.game.commands.CommandArmyMove;
import com.cwsni.world.model.Army;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;

public class PGame {

	private Game game;
	private PGameParams params;
	private Country country;
	private Map<Integer, PCountry> countries;
	private Map<Integer, PProvince> provinces;
	private Map<Integer, PArmy> armies;
	private List<Command> commands;

	/**
	 * Used by AI scripts. InMemoryOnly
	 */
	private AIData4Country aiData;

	public PGame(Country country) {
		this.game = country.getGame();
		this.country = country;
		this.params = new PGameParams(game.getGameParams());
		provinces = new HashMap<>();
		armies = new HashMap<>();
		countries = new HashMap<>(game.getCountries().size());
		commands = new ArrayList<>();
	}

	public PCountry getCountry() {
		return getCountry(country);
	}

	public Integer getCountryId() {
		return country.getId();
	}
	
	public PGameParams getParams() {
		return params;
	}

	public Collection<PCountry> getCountries() {
		return Collections.unmodifiableCollection((countries.values()));
	}

	PCountry getCountry(Country c) {
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

	PArmy getArmy(Army a) {
		return new PArmy(this, a);
	}

	public PProvince getProvince(Integer id) {
		return getProvince(game.getMap().findProvById(id));
	}

	public double relativeDistance(PProvince from, PProvince to) {
		return relativeDistance(from.getId(), to.getId());
	}

	public double relativeDistance(Integer fromId, Integer toId) {
		return game.getMap().findRelativeDistanceBetweenProvs(fromId, toId);
	}

	public AIData4Country getAIData() {
		if (aiData == null) {
			aiData = new AIData4Country();
		}
		return aiData;
	}

	public List<Command> getCommands() {
		return Collections.unmodifiableList(commands);
	}

	public void addCommand(Command command) {
		commands.add(command);
	}

	public void removeCommand(Command command) {
		commands.remove(command);
	}

	public void removeCommands(List<CommandArmyMove> commandsForCancellation) {
		commands.removeAll(commandsForCancellation);
	}

}
