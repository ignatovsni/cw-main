package com.cwsni.world.model.player;

import java.util.List;

import com.cwsni.world.game.commands.CommandArmyDismiss;
import com.cwsni.world.game.commands.CommandArmyMerge;
import com.cwsni.world.game.commands.CommandArmyMove;
import com.cwsni.world.game.commands.CommandArmySplit;
import com.cwsni.world.game.commands.CommandArmySubjugate;
import com.cwsni.world.model.engine.Army;
import com.cwsni.world.model.player.interfaces.IPArmy;
import com.cwsni.world.model.player.interfaces.IPCountry;
import com.cwsni.world.model.player.interfaces.IPProvince;

public class PArmy implements IPArmy {

	private Army army;
	private PGame game;
	private IPCountry country;
	private CommandArmyMove moveCommand;
	private boolean isCanMove;

	private long id;
	private Integer locationId;
	private int soldiers = 0;

	PArmy(PGame game, Army army) {
		this(game, army.getId(), army.getLocationId(), army.getSoldiers());
		this.army = army;
	}

	PArmy(PGame game, long id, Integer locationId, int soldiers) {
		this.game = game;
		this.id = id;
		this.locationId = locationId;
		this.soldiers = soldiers;
		this.isCanMove = true;
	}

	@Override
	public IPProvince getLocation() {
		return game.findProvById(locationId);
	}

	@Override
	public IPCountry getCountry() {
		return country;
	}

	void setCountry(IPCountry country) {
		this.country = country;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return (int) getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PArmy)) {
			return false;
		}
		return ((IPArmy) obj).getId() == getId();
	}

	@Override
	public int getSoldiers() {
		return soldiers;
	}

	@Override
	public int getStrength() {
		// TODO organization, training. equipment
		return getSoldiers();
	}

	@Override
	public boolean isCanMove() {
		return isCanMove;
	}

	@Override
	public void moveTo(IPProvince destination) {
		if (isCanMove && destination.isPassable(this)) {
			moveCommand = new CommandArmyMove(id, destination.getId());
			game.addCommand(moveCommand);
			isCanMove = false;
		}
	}

	private void moveTo(int destId) {
		moveTo(game.findProvById(destId));
	}

	@Override
	public void moveTo(List<? extends Object> path) {
		if (path.size() > 1) {
			moveTo((Integer) path.get(1));
		}
	}

	@Override
	public double getCostForSoldierPerYear() {
		return game.getGameParams().getBudgetBaseCostPerSoldier();
	}

	@Override
	public double getCostPerYear() {
		return getSoldiers() * getCostForSoldierPerYear();
	}

	@Override
	public void dismiss() {
		dismiss(-1);
	}

	/**
	 * 
	 * @param howManySoldiersNeedToDismiss
	 *            if < 0 then dismiss all
	 */
	@Override
	public void dismiss(int howManySoldiersNeedToDismiss) {
		if (howManySoldiersNeedToDismiss == 0) {
			return;
		}
		CommandArmyDismiss dismissCommand = new CommandArmyDismiss(id, howManySoldiersNeedToDismiss);
		game.addCommand(dismissCommand);
	}

	@Override
	public IPArmy splitArmy(int soldiersToNewArmy) {
		if (soldiersToNewArmy <= 0 || soldiersToNewArmy >= getSoldiers()) {
			return null;
		}
		CommandArmySplit splitCommand = new CommandArmySplit(id, ((PCountry) country).getNewArmyId(),
				soldiersToNewArmy);
		return (IPArmy) game.addCommand(splitCommand);
	}

	@Override
	public void merge(IPArmy fromArmy) {
		merge(fromArmy, Integer.MAX_VALUE);
	}

	@Override
	public void merge(IPArmy fromArmy, int soldiers) {
		if (!this.equals(fromArmy)) {
			CommandArmyMerge mergeCommand = new CommandArmyMerge(id, fromArmy.getId(), soldiers);
			game.addCommand(mergeCommand);
		}
	}
	
	@Override
	public void subjugateProvince() {
		CommandArmySubjugate subjugateCommand = new CommandArmySubjugate(id);
		game.addCommand(subjugateCommand);
	}

	double getAgriculture() {
		// it works only for existing armies (not for created or splitted at the same
		// turn)
		return army != null ? army.getScienceAgriculture() : 0;
	}

	// ---------------- client change model section ----------------- 
	
	public void cmcAddSoldiers(int howManySoldiers) {
		soldiers += howManySoldiers;
	}


}
