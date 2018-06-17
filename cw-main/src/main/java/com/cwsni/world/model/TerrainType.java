package com.cwsni.world.model;

public enum TerrainType {
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
}
