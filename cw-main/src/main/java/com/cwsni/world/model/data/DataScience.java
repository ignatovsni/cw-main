package com.cwsni.world.model.data;

import com.cwsni.world.client.desktop.util.DataFormatter;

public class DataScience {

	private double amount;
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
		this.amount = DataFormatter.doubleWith3points(amount);
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
		this.max = DataFormatter.doubleWith3points(max);
	}

	public DataScience createClone() {
		return new DataScience(amount, max);
	}

}
