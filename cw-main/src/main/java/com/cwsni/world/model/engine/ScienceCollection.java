package com.cwsni.world.model.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.cwsni.world.model.data.DataScience;
import com.cwsni.world.model.data.DataScienceCollection;
import com.cwsni.world.model.data.GameParams;

public class ScienceCollection {

	private DataScienceCollection data;

	public ScienceCollection() {
		data = new DataScienceCollection();
	}

	public ScienceCollection(DataScienceCollection science) {
		this.data = science;
	}

	DataScienceCollection getScienceData() {
		return data;
	}

	public void buildFrom(DataScienceCollection science) {
		this.data = science;
	}

	public DataScience getAgriculture() {
		return data.getAgriculture();
	}

	public DataScience getMedicine() {
		return data.getMedicine();
	}

	public DataScience getAdministration() {
		return data.getAdministration();
	}

	public void cloneFrom(ScienceCollection from) {
		data.cloneFrom(from.getScienceData());
	}

	private void growScience(Game game, DataScienceCollection maxScience, Province p) {
		DataScienceCollection.allGetter4Science()
				.forEach(scienceGetter -> growScienceType(game, p, data, maxScience, scienceGetter));
	}

	public void mergeFrom(ScienceCollection from, double ownFraction) {
		DataScienceCollection.allGetter4Science()
				.forEach(scienceGetter -> mergeFrom(data, from.getScienceData(), ownFraction, scienceGetter));
	}

	private void growScienceType(Game game, Province p, DataScienceCollection science, DataScienceCollection maxScience,
			Function<DataScienceCollection, DataScience> getter4Science) {
		DataScience scienceType = getter4Science.apply(science);
		DataScience maxScienceType = getter4Science.apply(maxScience);
		GameParams gParams = game.getGameParams();

		// natural pops science
		double scienceIncrease = Math.max(1, +(Math.log10((double) p.getPopulationAmount()
				* game.getTurn().addPerWeek(gParams.getScienceBaseIncreasePerPersonPerWeek()))));
		double newAmount = scienceType.getAmount() + scienceIncrease;
		// province.totalPopulation instead of population.amount,
		// because people develop science together
		double maxNaturalScienceLimit = (double) p.getPopulationAmount()
				* gParams.getScienceNaturalGrowthLimitPerPerson();
		// science can't growth more than natural limit
		newAmount = Math.min(newAmount, maxNaturalScienceLimit);
		// ... and can't become lower current level without special reason
		newAmount = Math.max(newAmount, scienceType.getAmount());

		// science from max leveled people
		double scienceLocalMax = scienceType.getMax();
		double scienceExchangeWithMaxPerTurn = game.getTurn().addPerYear(gParams.getScienceExchangeWithMaxPerTurn());
		double deltaWithLocalMax = (scienceLocalMax - newAmount) * scienceExchangeWithMaxPerTurn;
		if (deltaWithLocalMax > 0) {
			scienceType.setMax(scienceLocalMax - deltaWithLocalMax);
		}

		// science from neighbors
		double deltaWithNeighborsMax = (maxScienceType.getAmount() - newAmount) * scienceExchangeWithMaxPerTurn;

		if (deltaWithLocalMax > 0 || deltaWithNeighborsMax > 0) {
			newAmount += Math.max(deltaWithLocalMax, deltaWithNeighborsMax);
		}

		scienceType.setAmount(newAmount);
	}

	private void mergeFrom(DataScienceCollection science, DataScienceCollection fromScience, double ownFraction,
			Function<DataScienceCollection, DataScience> getter4Science) {
		DataScience scienceType = getter4Science.apply(science);
		DataScience fromScienceType = getter4Science.apply(fromScience);
		double avg = (ownFraction * scienceType.getAmount() + fromScienceType.getAmount() * (1 - ownFraction));
		scienceType.setAmount((int) Math.round(avg));
		scienceType.setMax(Math.max(scienceType.getMax(), fromScienceType.getMax()));
	}

	private void spendMoneyForScienceProvince(Game game, Province p, double money) {
		if (p.getCountry() != null) {
			ScienceBudget scienceBudget = p.getCountry().getScienceBudget();
			data.getAdministration().addAmount(scienceBudget.getAdministrationFraction(game, money));
			data.getAgriculture().addAmount(scienceBudget.getAgricultureFraction(game, money));
			data.getMedicine().addAmount(scienceBudget.getMedicineFraction(game, money));
		} else {
			double eachScienceIncrease = ScienceBudget.calcHowToIncreaseScience(game, money / 3);
			data.getAdministration().addAmount(eachScienceIncrease);
			data.getAgriculture().addAmount(eachScienceIncrease);
			data.getMedicine().addAmount(eachScienceIncrease);
		}
	}

	// --------------------- static ----------------------------------
	public static void growScienceNewTurn(Province p, Game game) {
		DataScienceCollection maxScience = findMaxScienceAmongNeighbors(p, game);
		p.getPopulation().forEach(pop -> pop.getScience().growScience(game, maxScience, p));
	}

	private static DataScienceCollection findMaxScienceAmongNeighbors(Province p, Game game) {
		DataScienceCollection maxScience = new DataScienceCollection();
		DataScienceCollection.allGetter4Science()
				.forEach(scienceGetter -> findMaxScienceAmongNeighbors(p, game, maxScience, scienceGetter));
		return maxScience;
	}

	static private void findMaxScienceAmongNeighbors(Province p, Game game, DataScienceCollection maxScience,
			Function<DataScienceCollection, DataScience> getter4Science) {
		DataScience scienceTypeMax = getter4Science.apply(maxScience);

		// neighbors
		List<Population> listNPops = new ArrayList<>(p.getNeighbors().size() * 3);
		p.getNeighbors().stream().filter(n -> n.getPopulationAmount() > 0)
				.forEach(n -> listNPops.addAll(n.getPopulation()));
		if (!listNPops.isEmpty()) {
			scienceTypeMax
					.setAmount(listNPops.stream()
							.mapToInt(pop -> (int) (getter4Science.apply(pop.getScience().getScienceData()).getAmount()
									* game.getGameParams().getScienceExchangeFromNeighborsFractionFromMax()))
							.max().getAsInt());
		}

		// locals
		DataScience scienceLocalMax = new DataScience();
		scienceLocalMax.setAmount(p.getPopulation().stream()
				.mapToDouble(pop -> getter4Science.apply(pop.getScience().getScienceData()).getAmount()).max()
				.getAsDouble());

		if (scienceLocalMax.getAmount() > scienceTypeMax.getAmount()) {
			scienceTypeMax.setAmount(scienceLocalMax.getAmount());
		}
	}

	public static void spendMoneyForScience(Game game, Province p, double money) {
		p.getPopulation().forEach(pop -> pop.getScience().spendMoneyForScienceProvince(game, p, money));
	}

}
