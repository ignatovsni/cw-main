package com.cwsni.world.game.commands;

import com.cwsni.world.model.Country;

public abstract class Command {

	abstract public void apply(Country country, CommandErrorHandler errorHandler);

}
