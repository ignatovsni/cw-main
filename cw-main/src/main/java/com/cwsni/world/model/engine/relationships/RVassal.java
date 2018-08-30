package com.cwsni.world.model.engine.relationships;

import com.cwsni.world.model.data.relationships.DataRVassal;

public class RVassal extends RBaseAgreement {

	protected DataRVassal getData() {
		return (DataRVassal) data;
	}

	public double getTax() {
		return getData().getTax();
	}

}
