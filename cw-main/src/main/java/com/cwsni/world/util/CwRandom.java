package com.cwsni.world.util;

import java.util.Random;

public class CwRandom {

	private Random random;

	public CwRandom(long seed) {
		random = new Random(seed);
	}

	public Integer nextInt(int bound) {
		return random.nextInt(bound);
	}

	public double nextDouble() {
		return random.nextDouble();
	}

	public double nextNormalDouble() {
		return Math.min(1, Math.max(0, (random.nextGaussian()/5 + 0.5)));
	}

	public static void main(String[] args) {
		CwRandom cwRandom = new CwRandom(1);
		for (int i = 0; i < 100; i++) {
			System.out.println(cwRandom.nextNormalDouble());
			// System.out.println(cwRandom.random.nextGaussian());
		}
	}

}
