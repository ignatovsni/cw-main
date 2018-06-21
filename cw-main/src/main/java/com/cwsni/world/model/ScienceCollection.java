package com.cwsni.world.model;

import com.cwsni.world.model.data.DataScienceCollection;
import com.cwsni.world.model.data.GameParams;

public class ScienceCollection {

	private DataScienceCollection science;

	public ScienceCollection() {
		science = new DataScienceCollection();
	}

	public ScienceCollection(DataScienceCollection science) {
		this.science = science;
	}

	public DataScienceCollection getScience() {
		return science;
	}

	public void buildFrom(DataScienceCollection science) {
		this.science = science;
	}

	public void cloneFrom(ScienceCollection from) {
		science.cloneFrom(from.getScience());
	}

	private void growScience(Game game, DataScienceCollection maxScience) {
		GameParams gParams = game.getGameParams();
		int scienceAgricultureInc = gParams.getScienceBaseIncreasePerTurn();
		science.getAgriculture().setMax(science.getAgriculture().getMax() - scienceAgricultureInc);
		if (maxScience.getAgriculture().getAmount() > science.getAgriculture().getAmount()) {
			// if neighbors have more advanced science then science will growth more quicker
			scienceAgricultureInc *= 2;
		}
		science.getAgriculture().setAmount(science.getAgriculture().getAmount() + scienceAgricultureInc);
	}

	public void mergeFrom(ScienceCollection from, double ownFraction) {
		double avg = (ownFraction * science.getAgriculture().getAmount()
				+ from.getScience().getAgriculture().getAmount() * (1 - ownFraction));
		science.getAgriculture().setAmount((int) Math.round(avg));
		science.getAgriculture()
				.setMax(Math.max(science.getAgriculture().getMax(), from.getScience().getAgriculture().getMax()));
	}

	// --------------------- static ----------------------------------
	public static void growScience(Province p, Game game) {
		DataScienceCollection maxScience = findMaxScienceAmongNeighbors(p);
		p.getPopulation().forEach(pop -> pop.getScience().growScience(game, maxScience));
	}

	private static DataScienceCollection findMaxScienceAmongNeighbors(Province p) {
		DataScienceCollection maxScience = new DataScienceCollection();
		// pops in the same province
		maxScience.getAgriculture().setAmount(p.getPopulation().stream()
				.mapToInt(pop -> pop.getScience().getScience().getAgriculture().getAmount()).max().getAsInt());
		// Now we have migration from neighbors provinces, so we can rely on this.
		// Probably in the future we need calculate level in neighbors provinces.
		return maxScience;
	}

}
