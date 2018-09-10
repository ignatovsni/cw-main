package com.cwsni.world.util;

import java.util.Random;

import com.cwsni.world.model.data.util.DataNormalizer;

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
	
	public double nextDouble(double min, double max) {
		return min + random.nextDouble() * (max - min);
	}

	public double nextNormalDouble() {
		return DataNormalizer.minMax(random.nextGaussian() / 5 + 0.5, 0, 1);
	}

}
