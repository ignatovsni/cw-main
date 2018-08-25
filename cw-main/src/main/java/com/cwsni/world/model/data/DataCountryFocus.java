package com.cwsni.world.model.data;

import com.cwsni.world.client.desktop.util.DataFormatter;

public class DataCountryFocus {

	public static final double BASE_VALUE = 1;
	public static final double MIN_VALUE = 0;
	public static final double MAX_VALUE = 10;
	public static final double GOAL_MIN_VALUE = 0.5;
	public static final double GOAL_MAX_VALUE = 2;
	public static final double MIN_STEP = 0.0001;

	private double focus;
	private double goal;
	private double step;

	public double getFocus() {
		return focus;
	}

	public void setFocus(double focus) {
		this.focus = normalizeFocus(focus);
	}

	private double normalizeFocus(double value) {
		return DataFormatter.doubleWith4points(Math.min(MAX_VALUE, Math.max(MIN_VALUE, value)));
	}

	public double getGoal() {
		return goal;
	}

	public void setGoal(double goal) {
		this.goal = normalizeFocus(goal);
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = DataFormatter.doubleWith4points(step);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append("[ focus=");
		sb.append(focus);
		sb.append(", goal=");
		sb.append(goal);
		sb.append(", step=");
		sb.append(step);
		sb.append("]");
		return sb.toString();
	}

}
