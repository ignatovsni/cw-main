package com.cwsni.world.game.commands;

import com.cwsni.world.model.Country;
import com.cwsni.world.model.player.PCountry;

public abstract class Command {

	abstract public void apply(Country country, CommandErrorHandler errorHandler);

	public void apply(PCountry country, CommandErrorHandler errorHandler) {
	}

}
