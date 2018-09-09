package com.cwsni.world.model.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataScience {

	private static final Log logger = LogFactory.getLog(DataScience.class);
	
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 3)
	private double amount;

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 3)
	private double max;

	public DataScience() {
		this(0, 0);
	}

	public DataScience(double amount, double max) {
		this.amount = amount;
		this.max = max;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		if (amount < 0) {
			logger.error("DataScience: amount < 0");
			amount = 0;
		}
		this.amount = amount;
		if (this.amount > 10000000) {
			logger.warn("DataScience: amount > 10,000,000");
		}
		if (this.amount > max) {
			max = this.amount;
		}
	}

	public void addAmount(double delta) {
		setAmount(getAmount() + delta);
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public DataScience createClone() {
		return new DataScience(amount, max);
	}

}
