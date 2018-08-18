package com.cwsni.world.game.commands;

abstract public class CommandProvince extends Command {

	protected int provinceId;

	public CommandProvince(int provinceId) {
		this.provinceId = provinceId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(".");
		sb.append(" provinceId:");
		sb.append(provinceId);
		return sb.toString();
	}

	@Override
	abstract public void apply();

}
