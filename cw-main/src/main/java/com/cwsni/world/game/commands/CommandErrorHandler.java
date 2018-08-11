package com.cwsni.world.game.commands;

import java.util.ArrayList;
import java.util.List;

public class CommandErrorHandler {

	private List<String> errors = new ArrayList<>();

	public void addError(Command cmd, String e) {
		getErrors().add(cmd.toString() + " : " + e);
	}

	public List<String> getErrors() {
		return errors;
	}

	public boolean isEmpty() {
		return errors.isEmpty();
	}

}
