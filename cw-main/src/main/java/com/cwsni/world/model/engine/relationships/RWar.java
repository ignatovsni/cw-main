package com.cwsni.world.model.engine.relationships;

import com.cwsni.world.model.data.relationships.DataRWar;
import com.cwsni.world.model.engine.Game;

public class RWar {

	private DataRWar data;

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof RWar)) {
			return false;
		}
		return data.equals(((RWar) obj).data);
	}

	public void buildFrom(Game game, DataRWar dw) {
		this.data = dw;
	}

}
