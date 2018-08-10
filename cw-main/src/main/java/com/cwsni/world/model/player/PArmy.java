package com.cwsni.world.model.player;

import java.util.List;

import com.cwsni.world.game.commands.CommandArmyDismiss;
import com.cwsni.world.game.commands.CommandArmyMerge;
import com.cwsni.world.game.commands.CommandArmyMove;
import com.cwsni.world.game.commands.CommandArmySplit;
import com.cwsni.world.model.Army;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPProvince;

public class PArmy implements IPArmy {

	private Army army;
	private PGame game;
	private IPCountry country;
	private CommandArmyMove moveCommand;
	private int dismissedSoldiers = 0;

	PArmy(PGame game, Army army) {
		this.game = game;
		this.army = army;
	}

	@Override
	public IPProvince getLocation() {
		if (army.getLocation() == null) {
			System.out.println();
		}
		return game.getProvince(army.getLocation().getId());
	}

	@Override
	public IPCountry getCountry() {
		return country;
	}

	void setCountry(IPCountry country) {
		this.country = country;
	}

	@Override
	public int getId() {
		return army.getId();
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PArmy)) {
			return false;
		}
		return ((IPArmy) obj).getId() == getId();
	}

	@Override
	public int getSoldiers() {
		return army.getSoldiers() - dismissedSoldiers;
	}

	@Override
	public void moveTo(IPProvince destination) {
		int destId = destination.getId();
		moveTo(destId);
	}

	private void moveTo(int destId) {
		moveCommand = new CommandArmyMove(army.getId(), destId);
		game.addCommand(moveCommand);
	}

	@Override
	public void moveTo(List<Object> path) {
		moveTo((Integer) path.get(1));
	}

	@Override
	public double getCostForSoldierPerYear() {
		return army.getCostForSoldierPerYear();
	}

	@Override
	public double getCostPerYear() {
		return army.getCostPerYear();
	}

	@Override
	public void dismiss() {
		dismissSoldiers(-1);
	}

	/**
	 * 
	 * @param howManySoldiersNeedToDismiss
	 *            if < 0 then dismiss all
	 */
	@Override
	public void dismissSoldiers(int howManySoldiersNeedToDismiss) {
		if (howManySoldiersNeedToDismiss == 0) {
			return;
		}
		CommandArmyDismiss dismissCommand = new CommandArmyDismiss(army.getId(), howManySoldiersNeedToDismiss);
		game.addCommand(dismissCommand);
	}

	@Override
	public void splitArmy(int soldiersToNewArmy) {
		if (soldiersToNewArmy <= 0 || soldiersToNewArmy >= getSoldiers()) {
			return;
		}
		CommandArmySplit splitCommand = new CommandArmySplit(army.getId(), soldiersToNewArmy);
		game.addCommand(splitCommand);
	}

	@Override
	public void merge(IPArmy fromArmy) {
		CommandArmyMerge mergeCommand = new CommandArmyMerge(army.getId(), fromArmy.getId());
		game.addCommand(mergeCommand);
	}

	public void cpDismissSoldiers(int howManySoldiers) {
		dismissedSoldiers += howManySoldiers;
	}

}
