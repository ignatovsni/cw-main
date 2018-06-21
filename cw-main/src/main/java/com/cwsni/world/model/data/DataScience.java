package com.cwsni.world.model.data;

public class DataScience {
	
	private int amount;
	private int max;

	public DataScience() {
		this(0, 0);
	}
	
	public DataScience(int amount, int max) {
		this.amount = amount;
		this.max = max;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
		if (amount > max) {
			max = amount;
		}
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public DataScience createClone() {
		return new DataScience(amount, max);
	}

}
