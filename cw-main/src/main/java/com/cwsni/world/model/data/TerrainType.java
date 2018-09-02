package com.cwsni.world.model.data;

public enum TerrainType {
	GRASSLAND("model.province.terrain-type.grassland"), OCEAN("model.province.terrain-type.ocean"), MOUNTAIN(
			"model.province.terrain-type.mountain");

	/**
	 * Message code for internationalization.
	 */
	private String codeMsg;

	public String getCodeMsg() {
		return codeMsg;
	}

	TerrainType(String code) {
		this.codeMsg = code;
	}

	public boolean isSoilPossible() {
		return GRASSLAND.equals(this);
	}

	public boolean isPopulationPossible() {
		return GRASSLAND.equals(this);
	}

	public boolean isWater() {
		return OCEAN.equals(this);
	}

	public boolean isMountain() {
		return MOUNTAIN.equals(this);
	}

	public boolean isPassable() {
		return !isMountain();
	}
}
