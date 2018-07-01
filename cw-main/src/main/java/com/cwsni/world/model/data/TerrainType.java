package com.cwsni.world.model.data;

public enum TerrainType {
	// TODO for water provinces I can use "complexity" - короче, некая "сложность
	// мореходства", на практике это может быть просто расстояние от берега.
	// Чем более развита наука, тем дальше могут плавать корабли.

	GRASSLAND("model.province.terrain-type.grassland"), OCEAN("model.province.terrain-type.ocean");

	/**
	 * message code for internationalization
	 */
	private String codeMsg;

	public String getCodeMsg() {
		return codeMsg;
	}

	TerrainType(String code) {
		this.codeMsg = code;
	}

	public boolean isSoilPossible() {
		return !OCEAN.equals(this);
	}

	public boolean isPopulationPossible() {
		return !OCEAN.equals(this);
	}

	public boolean isWater() {
		return OCEAN.equals(this);
	}
}
