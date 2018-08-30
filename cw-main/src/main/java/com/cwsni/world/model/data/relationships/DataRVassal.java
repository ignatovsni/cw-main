package com.cwsni.world.model.data.relationships;

import com.cwsni.world.client.desktop.util.DataFormatter;

public class DataRVassal extends DataRBaseAgreement {

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
		this.tax = DataFormatter.doubleWith2points(Math.min(0.9, Math.max(0.1, tax)));
	}

}
