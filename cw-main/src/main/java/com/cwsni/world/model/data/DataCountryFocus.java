package com.cwsni.world.model.data;

import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataCountryFocus {

	public static final double BASE_VALUE = 1;
	public static final double MIN_VALUE = 0;
	public static final double MAX_VALUE = 10;
	public static final double MIN_STEP = 0.0001;

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 4)
	private double focus;

	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 4)
	private double goal;
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 4)
	private double step;

	public double getFocus() {
		return focus;
	}

	public void setFocus(double focus) {
		this.focus = normalizeFocus(focus);
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
		this.step = Math.max(DataCountryFocus.MIN_STEP, step);
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

	public static double normalizeFocus(double value) {
		return Math.min(MAX_VALUE, Math.max(MIN_VALUE, value));
	}

}
