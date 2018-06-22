package com.cwsni.world.model;

import java.util.function.Function;

import com.cwsni.world.model.data.DataScience;
import com.cwsni.world.model.data.DataScienceCollection;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.model.events.Event;

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

	public DataScience getAgriculture() {
		return science.getAgriculture();
	}

	public DataScience getMedicine() {
		return science.getMedicine();
	}

	public void cloneFrom(ScienceCollection from) {
		science.cloneFrom(from.getScience());
	}

	private void growScience(Game game, DataScienceCollection maxScience, Province p) {
		growScienceType(game, science, maxScience, ds -> ds.getAgriculture());
		growScienceType(game, science, maxScience, ds -> ds.getMedicine());
		if (p.getEvents().hasEventWithType(Event.EVENT_EPIDEMIC)) {
			science.getMedicine().setAmount(science.getMedicine().getAmount() + 1);
		}
	}

	public void mergeFrom(ScienceCollection from, double ownFraction) {
		mergeFrom(science, from.getScience(), ownFraction, ds -> ds.getAgriculture());
		mergeFrom(science, from.getScience(), ownFraction, ds -> ds.getMedicine());
	}

	private void growScienceType(Game game, DataScienceCollection science, DataScienceCollection maxScience,
			Function<DataScienceCollection, DataScience> getter4Science) {
		DataScience scienceType = getter4Science.apply(science);
		DataScience maxScienceType = getter4Science.apply(maxScience);
		GameParams gParams = game.getGameParams();
		int scienceIncrease = gParams.getScienceBaseIncreasePerTurn();
		scienceType.setMax(scienceType.getMax() - scienceIncrease);
		if (maxScienceType.getAmount() > scienceType.getAmount()) {
			// if neighbors have more advanced science then science will growth more quicker
			scienceIncrease *= 2;
		}
		scienceType.setAmount(scienceType.getAmount() + scienceIncrease);
	}

	private void mergeFrom(DataScienceCollection science, DataScienceCollection fromScience, double ownFraction,
			Function<DataScienceCollection, DataScience> getter4Science) {
		DataScience scienceType = getter4Science.apply(science);
		DataScience fromScienceType = getter4Science.apply(fromScience);
		double avg = (ownFraction * scienceType.getAmount() + fromScienceType.getAmount() * (1 - ownFraction));
		scienceType.setAmount((int) Math.round(avg));
		scienceType.setMax(Math.max(scienceType.getMax(), fromScienceType.getMax()));
	}

	// --------------------- static ----------------------------------
	public static void growScienceNewTurn(Province p, Game game) {
		DataScienceCollection maxScience = findMaxScienceAmongNeighbors(p);
		p.getPopulation().forEach(pop -> pop.getScience().growScience(game, maxScience, p));
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
