package com.cwsni.world.model.data;

import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataFoodResource {

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 0)
	private double amount;
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 3)
	private double quality;

	public double getAmount() {
		return amount;
	}

	public void setAmount(double quantity) {
		this.amount = quantity;
	}

	public double getQuality() {
		return quality;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(". amount=");
		sb.append(amount);
		sb.append(", quality=");
		sb.append(quality);
		return sb.toString();
	}

}
