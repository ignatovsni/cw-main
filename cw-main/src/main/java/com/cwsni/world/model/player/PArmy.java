package com.cwsni.world.model.player;

import java.util.List;

import com.cwsni.world.game.commands.Command;
import com.cwsni.world.game.commands.CommandArmyMove;
import com.cwsni.world.model.Army;

public class PArmy {

	private Army army;
	private PGame game;
	private PCountry country;
	private Command command;

	PArmy(PGame game, Army army) {
		this.game = game;
		this.army = army;
	}

	public PProvince getLocation() {
		return game.getProvince(army.getLocation().getId());
	}

	public PCountry getCountry() {
		return country;
	}

	void setCountry(PCountry country) {
		this.country = country;
	}

	private int getId() {
		return army.getId();
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PArmy)) {
			return false;
		}
		return ((PArmy) obj).getId() == getId();
	}

	public void moveTo(PProvince destination) {
		int destId = destination.getId();
		moveTo(destId);
	}

	private void moveTo(int destId) {
		if (command != null) {
			game.removeCommand(command);
		}
		command = new CommandArmyMove(army.getId(), destId);
		game.addCommand(command);
	}

	public void moveTo(List<Object> path) {
		moveTo((Integer) path.get(1));
	}

}
