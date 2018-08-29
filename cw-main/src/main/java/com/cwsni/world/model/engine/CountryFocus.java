package com.cwsni.world.model.engine;

import com.cwsni.world.model.data.DataCountryFocus;
import com.cwsni.world.model.data.GameParams;
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
		return (getValue() - DataCountryFocus.BASE_VALUE) / 2 + DataCountryFocus.BASE_VALUE;
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
		GameParams gParams = country.getGame().getGameParams();
		CwRandom rnd = gParams.getRandom();
		double chance = rnd.nextDouble();
		double focus = getValue();
		if (chance > 0.999) {
			data.setFocus(focus + (rnd.nextNormalDouble() - 0.5) * 2);
			data.setGoal(DataCountryFocus.BASE_VALUE);
			data.setStep(createStep(gParams, rnd, 0.005));
		} else if (chance > 0.997) {
			data.setGoal(createNewGoal(gParams, rnd));
			if (rnd.nextDouble() > 0.9) {
				data.setStep(createStep(gParams, rnd, 0.001));
			} else {
				data.setStep(createStep(gParams, rnd, 0.005));
			}
		} else if (chance > 0.995) {
			data.setGoal((createNewGoal(gParams, rnd) + DataCountryFocus.BASE_VALUE) / 2);
			if (rnd.nextDouble() > 0.9) {
				data.setStep(createStep(gParams, rnd, 0.0025));
			} else {
				data.setStep(createStep(gParams, rnd, 0.01));
			}
		} else if (data.getStep() > 0) {
			double step = data.getStep();
			double currentFocus = data.getFocus();
			if (currentFocus > DataCountryFocus.MAX_VALUE * 0.8) {
				step = DataCountryFocus.BASE_VALUE * 0.02;
			} else if (currentFocus > DataCountryFocus.MAX_VALUE * 0.5) {
				step = DataCountryFocus.BASE_VALUE * 0.01;
			} else if (currentFocus > DataCountryFocus.MAX_VALUE * 0.3) {
				step = DataCountryFocus.BASE_VALUE * 0.008;
			} else if (currentFocus > DataCountryFocus.MAX_VALUE * 0.2) {
				step = DataCountryFocus.BASE_VALUE * 0.005;
			}
			double diff = data.getGoal() - focus;
			double diffAbs = Math.abs(diff);
			if (diffAbs >= step) {
				// we are able to make a step
				data.setFocus(focus + Math.min(step, diffAbs) * Math.signum(diff));
			} else if (Math.abs(data.getGoal() - DataCountryFocus.BASE_VALUE) < DataCountryFocus.BASE_VALUE / 100) {
				// the goal is base and we are close to it
				data.setFocus(DataCountryFocus.BASE_VALUE);
				data.setGoal(DataCountryFocus.BASE_VALUE);
				data.setStep(0);
			} else {
				// the goal is reached, we must come back to base
				data.setGoal(DataCountryFocus.BASE_VALUE);
				data.setStep(createStep(gParams, rnd, 0.01));
			}
		}
	}

	// -------------------------- static section ----------------------

	public static DataCountryFocus createFocusForNewCountry(Game game) {
		DataCountryFocus dcf = new DataCountryFocus();
		CwRandom rnd = game.getGameParams().getRandom();
		double chance = rnd.nextDouble();
		double focus;
		if (chance > 0.99) {
			focus = createNewGoal(game.getGameParams(), rnd);
			dcf.setStep(createStep(game.getGameParams(), rnd, 0.01));
		} else {
			focus = DataCountryFocus.BASE_VALUE;
		}
		dcf.setFocus(focus);
		dcf.setGoal(focus);
		return dcf;
	}

	private static double createStep(GameParams gParams, CwRandom rnd, double maxStep) {
		return Math.min(maxStep, rnd.nextDouble() * maxStep + gParams.getFocusMinStep());
	}

	private static double createNewGoal(GameParams gParams, CwRandom rnd) {
		double rndValue = rnd.nextNormalDouble();
		double pg;
		if (rndValue > 0.5) {
			pg = (rndValue - 0.5) * (gParams.getFocusMaxGoal() - 1) * 2 / DataCountryFocus.BASE_VALUE
					+ DataCountryFocus.BASE_VALUE;
		} else {
			pg = gParams.getFocusMinGoal() + rndValue * 2 * (DataCountryFocus.BASE_VALUE - gParams.getFocusMinGoal());
		}
		return Math.min(gParams.getFocusMaxGoal(), Math.max(gParams.getFocusMinGoal(), pg));
	}

}
