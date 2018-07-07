package com.cwsni.world.model.player;

import java.util.List;

import com.cwsni.world.game.commands.CommandArmyDismiss;
import com.cwsni.world.game.commands.CommandArmyMove;
import com.cwsni.world.model.Army;

public class PArmy {

	private Army army;
	private PGame game;
	private PCountry country;
	private CommandArmyMove moveCommand;
	private CommandArmyDismiss dismissCommand;

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

	public int getSoldiers() {
		return army.getSoldiers();
	}

	public void moveTo(PProvince destination) {
		int destId = destination.getId();
		moveTo(destId);
	}

	private void moveTo(int destId) {
		if (dismissCommand != null) {
			return;
		}
		game.removeCommand(moveCommand);
		moveCommand = new CommandArmyMove(army.getId(), destId);
		game.addCommand(moveCommand);
	}

	public void moveTo(List<Object> path) {
		moveTo((Integer) path.get(1));
	}

	public double getCostForSoldierPerYear() {
		return army.getCostForSoldierPerYear();
	}

	public double getCostPerYear() {
		return army.getCostPerYear();
	}

	public void dismiss() {
		dismissSoldiers(-1);
	}

	/**
	 * 
	 * @param howManySoldiersNeedToDismiss
	 *            if < 0 then dismiss all
	 */
	public void dismissSoldiers(int howManySoldiersNeedToDismiss) {
		if (howManySoldiersNeedToDismiss < 0) {
			game.removeCommand(moveCommand);
		}
		game.removeCommand(dismissCommand);
		dismissCommand = new CommandArmyDismiss(army.getId(), howManySoldiersNeedToDismiss);
		game.addCommand(dismissCommand);
	}

	public boolean isAbleToWork() {
		return army.isAbleToWork() && (dismissCommand == null || !dismissCommand.isFullDismiss());
	}

}
