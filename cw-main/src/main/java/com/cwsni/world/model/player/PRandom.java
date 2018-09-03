package com.cwsni.world.model.player;

import com.cwsni.world.model.player.interfaces.IPRandom;
import com.cwsni.world.util.CwBaseRandom;

public class PRandom implements IPRandom {

	private CwBaseRandom cwRandom;

	public PRandom(CwBaseRandom cwRandom) {
		this.cwRandom = cwRandom;
	}

	@Override
	public Integer nextInt(int bound) {
		return cwRandom.nextInt(bound);
	}

	@Override
	public double nextDouble() {
		return cwRandom.nextDouble();
	}

	@Override
	public double nextNormalDouble() {
		return cwRandom.nextNormalDouble();
	}

}
