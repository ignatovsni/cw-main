package com.cwsni.world.util;

import java.util.Random;

public class CwBaseRandom {

	private Random random;

	public CwBaseRandom(long seed) {
		internalResetWithSeed(seed);
	}

	protected void internalResetWithSeed(long seed) {
		random = new Random(seed);
	}

	public Integer nextInt(int bound) {
		return random.nextInt(bound);
	}

	public double nextDouble() {
		return random.nextDouble();
	}

	public double nextNormalDouble() {
		return Math.min(1, Math.max(0, (random.nextGaussian() / 5 + 0.5)));
	}

}
