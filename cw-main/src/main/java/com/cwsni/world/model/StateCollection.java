package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwsni.world.model.data.DataState;

public class StateCollection {

	private List<State> states;
	private Map<Integer, State> statesById;

	public void buildFrom(Game game, List<DataState> dataStates) {
		states = new ArrayList<>(dataStates.size());
		statesById = new HashMap<>(dataStates.size());
		dataStates.forEach(dc -> {
			State c = new State();
			states.add(c);
			statesById.put(dc.getId(), c);
			c.buildFrom(game, dc);
		});
	}

	public List<State> getStates() {
		return Collections.unmodifiableList(states);
	}

	public void addState(State c) {
		states.add(c);
		statesById.put(c.getId(), c);
	}

	public void removeState(State c) {
		states.remove(c);
		statesById.remove(c.getId());
	}

	public State findStateById(Integer stateId) {
		return statesById.get(stateId);
	}

}
