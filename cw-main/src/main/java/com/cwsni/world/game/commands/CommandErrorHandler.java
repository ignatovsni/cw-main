package com.cwsni.world.game.commands;

import java.util.ArrayList;
import java.util.List;

public class CommandErrorHandler {

	private List<String> errors = new ArrayList<>();

	public void addError(String e) {
		getErrors().add(e);
	}

	public List<String> getErrors() {
		return errors;
	}
	
	public boolean isEmpty() {
		return errors.isEmpty();
	}

}
