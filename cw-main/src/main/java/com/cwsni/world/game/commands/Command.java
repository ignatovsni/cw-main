package com.cwsni.world.game.commands;

import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.player.PCountry;

public abstract class Command {

	protected Country country;
	private CommandErrorHandler errorHandler;

	abstract public void apply();

	public Object apply(PCountry country, CommandErrorHandler errorHandler) {
		return null;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public int getCountryId() {
		return country.getId();
	}

	public Game getGame() {
		return country.getGame();
	}

	public void setErrorHandler(CommandErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public void addError(String txt) {
		errorHandler.addError(this, txt);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(".");
		sb.append(" country.id=");
		sb.append(getCountryId());
		return sb.toString();
	}

}
