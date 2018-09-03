package com.cwsni.world.util;

public class CwRandom extends CwBaseRandom {

	public CwRandom(long seed) {
		super(seed);
	}

	public void resetWithSeed(long seed) {
		internalResetWithSeed(seed);
	}

	public static void main(String[] args) {
		CwRandom cwRandom = new CwRandom(1);
		for (int i = 0; i < 100; i++) {
			System.out.println(cwRandom.nextNormalDouble());
		}
	}

}
