package com.cwsni.world.model;

import com.cwsni.world.model.data.DataCountryFocus;
import com.cwsni.world.util.CwRandom;

public class CountryFocus {

	private Country country;
	private DataCountryFocus data;

	public void buildFrom(Country country, DataCountryFocus focus) {
		this.country = country;
		this.data = focus;
	}

	public double getValue() {
		return data.getFocus();
	}

	private double getFlatBonus() {
		double value = getValue();
		if (value <= DataCountryFocus.BASE_VALUE) {
			return value / DataCountryFocus.BASE_VALUE - 1;
		} else {
			return (value - DataCountryFocus.BASE_VALUE) / DataCountryFocus.MAX_VALUE;
		}
	}

	public double getArmyStrengthInfluence() {
		return getValue();
	}

	public double getGovernmentInfluenceOnDistance() {
		return getValue();
	}

	public double getGovernmentFlatBonus() {
		return getFlatBonus();
	}

	public double getLoyaltyFlatBonus() {
		return getFlatBonus();
	}

	public double getTaxInfluence() {
		return getValue();
	}

	public void processNewTurn() {
		CwRandom rnd = country.getGame().getGameParams().getRandom();
		double chance = rnd.nextDouble();
		double focus = getValue();
		if (chance > 0.999) {
			data.setFocus(focus + (rnd.nextNormalDouble() - 0.5) * 2);
			data.setGoal(DataCountryFocus.BASE_VALUE);
			data.setStep(createStep(rnd, 0.01));
		} else if (chance > 0.99) {
			data.setGoal(createNewGoal(rnd));
			if (rnd.nextDouble() > 0.9) {
				data.setStep(createStep(rnd, 0.01));
			} else {
				data.setStep(createStep(rnd, 0.001));
			}
		} else if (data.getStep() > 0) {
			double diff = data.getGoal() - focus;
			double diffAbs = Math.abs(diff);
			if (diffAbs >= data.getStep()) {
				// we are able to make a step
				data.setFocus(focus + Math.min(data.getStep(), diffAbs) * Math.signum(diff));
			} else if (Math.abs(data.getGoal() - DataCountryFocus.BASE_VALUE) < DataCountryFocus.BASE_VALUE / 100) {
				// the goal is base and we are close to it
				data.setFocus(DataCountryFocus.BASE_VALUE);
				data.setGoal(DataCountryFocus.BASE_VALUE);
				data.setStep(0);
			} else {
				// the goal is reached, we must come back to base
				data.setGoal(DataCountryFocus.BASE_VALUE);
				data.setStep(createStep(rnd, 0.01));
			}
		}
	}

	private double createStep(CwRandom rnd, double maxStep) {
		return Math.min(maxStep, rnd.nextDouble() * maxStep + DataCountryFocus.MIN_STEP);
	}

	// -------------------------- static section ----------------------

	public static DataCountryFocus createFocusForNewCountry(Game game) {
		DataCountryFocus dcf = new DataCountryFocus();
		CwRandom rnd = game.getGameParams().getRandom();
		double chance = rnd.nextDouble();
		double focus;
		if (chance > 0.99) {
			focus = createNewGoal(rnd);
		} else {
			focus = DataCountryFocus.BASE_VALUE;
		}
		dcf.setFocus(focus);
		dcf.setGoal(focus);
		return dcf;
	}

	private static double createNewGoal(CwRandom rnd) {
		double pg = (rnd.nextNormalDouble() - 0.5) * DataCountryFocus.GOAL_MAX_VALUE / DataCountryFocus.BASE_VALUE
				+ DataCountryFocus.BASE_VALUE;
		return Math.min(DataCountryFocus.GOAL_MAX_VALUE, Math.max(DataCountryFocus.GOAL_MIN_VALUE, pg));
	}

}
