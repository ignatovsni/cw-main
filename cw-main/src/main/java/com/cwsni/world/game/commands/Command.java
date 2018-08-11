package com.cwsni.world.game.commands;

import com.cwsni.world.model.Country;
import com.cwsni.world.model.player.PCountry;

public abstract class Command {

	protected Country country;
	private CommandErrorHandler errorHandler;

	abstract public void apply();

	public void apply(PCountry country, CommandErrorHandler errorHandler) {
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Integer getCountryId() {
		return country.getId();
	}

	public void setErrorHandler(CommandErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public void addError(String txt) {
		errorHandler.addError(this, txt);
	}

}
