package com.cwsni.world.model.engine.relationships;

import com.cwsni.world.model.data.relationships.DataRTribute;

public class RTribute extends RBaseAgreement {

	protected DataRTribute getData() {
		return (DataRTribute) data;
	}

	public double getTax() {
		return getData().getTax();
	}

	public void setTax(double tax) {
		getData().setTax(tax);
	}

}
