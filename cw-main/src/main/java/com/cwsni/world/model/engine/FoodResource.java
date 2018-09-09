package com.cwsni.world.model.engine;

import com.cwsni.world.model.data.DataFoodResource;

public class FoodResource {

	private DataFoodResource data;

	public double getAmount() {
		return data.getAmount();
	}

	public double getQuality() {
		return data.getQuality();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(". amount=");
		sb.append(getAmount());
		sb.append(", quality=");
		sb.append(getQuality());
		return sb.toString();
	}

	public void buildFrom(Province province, DataFoodResource data) {
		this.data = data;
	}

}
