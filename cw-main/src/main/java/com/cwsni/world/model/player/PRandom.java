package com.cwsni.world.model.player;

import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.player.interfaces.IPRandom;

public class PRandom implements IPRandom {

	private GameParams params;

	public PRandom(GameParams params) {
		this.params = params;
	}

	@Override
	public Integer nextInt(int bound) {
		return params.getRandom().nextInt(bound);
	}

	@Override
	public double nextDouble() {
		return params.getRandom().nextDouble();
	}

	@Override
	public double nextNormalDouble() {
		return params.getRandom().nextNormalDouble();
	}

}
