package com.cwsni.world.game.commands;

abstract public class CommandDiplomacy extends Command {

	protected int targetCountryId;

	public CommandDiplomacy(int targetCountryId) {
		this.targetCountryId = targetCountryId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" targetCountryId:");
		sb.append(targetCountryId);
		return sb.toString();
	}
	
	protected boolean checkTargetCountry() {
		if (getGame().findCountryById(targetCountryId) == null) {
			if (getGame().getHistory().findCountry(targetCountryId) == null) {
				System.out.println("country doesn't exist: " + this);
			}
			return false;
		}
		return true;
	}
	

}
