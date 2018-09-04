package com.cwsni.world.model.data.relationships;

import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataRTribute extends DataRBaseAgreement {

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 2)
	private double tax;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" tax:");
		sb.append(getTax());
		return sb.toString();
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = Math.min(0.9, Math.max(0.1, tax));
	}

}
