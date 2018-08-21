package com.cwsni.world.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cwsni.world.CwException;
import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.data.DataState;
import com.cwsni.world.model.data.GameParams;
import com.cwsni.world.util.CwRandom;

public class State {

	private DataState data;
	private Game game;
	private Collection<Province> provinces;

	public void buildFrom(Game game, DataState ds) {
		this.game = game;
		this.data = ds;
		provinces = new HashSet<>();

		data.getProvinces().forEach(pId -> {
			Province province = game.getMap().findProvById(pId);
			province.setState(this);
			provinces.add(province);
		});
	}

	public int getId() {
		return data.getId();
	}

	public String getName() {
		return data.getName();
	}

	public Color getColor() {
		return data.getColor();
	}

	public Game getGame() {
		return game;
	}

	public Province getCapital() {
		return game.getMap().findProvById(data.getCapital());
	}

	public void setCapital(Province province) {
		if (province == null) {
			data.setCapital(null);
		} else {
			if (this.equals(province.getState())) {
				data.setCapital(province.getId());
			} else {
				throw new CwException("Trying to set up capital in alien province: province state id = "
						+ province.getState() + " but state.id = " + getId());
			}
		}
	}

	public Integer getCapitalId() {
		return data.getCapital();
	}

	public void addProvince(Province p) {
		if (data.getProvinces().contains(p.getId())) {
			return;
		}
		if (p.getTerrainType().isPopulationPossible()) {
			p.setState(this);
			provinces.add(p);
			data.getProvinces().add(p.getId());
		}
	}

	public void removeProvince(Province p) {
		if (!data.getProvinces().contains(p.getId())) {
			return;
		}
		p.setState(null);
		provinces.remove(p);
		data.getProvinces().remove(p.getId());
		if (ComparisonTool.isEqual(getCapitalId(), p.getId())) {
			setCapital(null);
		}
	}

	public Collection<Province> getProvinces() {
		return Collections.unmodifiableCollection(provinces);
	}

	DataState getStateData() {
		return data;
	}

	@Override
	public int hashCode() {
		return data.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof State)) {
			return false;
		}
		return ((State) obj).getId() == getId();
	}

	public void processNewTurn() {
		processScienceNewTurn();
	}

	private void processScienceNewTurn() {
		Province capital = getCapital();
		if (capital != null) {
			Country country = capital.getCountry();
			if (country != null) {
				MoneyBudget moneyBudget = country.getMoneyBudget();
				capital.spendMoneyForScience(moneyBudget.getAvailableMoneyForScience() * 0.5);
			}
		}
	}

	public void setName(String name) {
		data.setName(name);
	}

	// --------------------- static -------------------------------

	public static void createOrGrowthStates(Country country) {
		GameParams gParams = country.getGame().getGameParams();
		Province capital = country.getCapital();
		int stateCreateWithMinProvinces = gParams.getStateCreateWithMinProvinces();
		if (capital != null && capital.getState() == null
				&& country.getProvinces().size() >= stateCreateWithMinProvinces * 2) {
			// create state for capital
			createNewState(country, capital, stateCreateWithMinProvinces);
		}
		List<Province> provs = country.getProvinces().stream().filter(p -> p.getState() == null)
				.collect(Collectors.toList());
		if (provs.isEmpty()) {
			return;
		}
		Province prov = provs.get(gParams.getRandom().nextInt(provs.size()));
		// create state for random province
		createNewState(country, prov, stateCreateWithMinProvinces);
		// if we can't create state then we try to join province to the nearest state
		if (prov.getState() == null) {
			Province n = prov.getNeighbors().get(gParams.getRandom().nextInt(prov.getNeighbors().size()));
			if (n.getState() != null) {
				n.getState().addProvince(prov);
			}
		}
	}

	private static void createNewState(Country country, Province prov, int minProvinces) {
		if (prov.getState() != null) {
			return;
		}
		Game game = country.getGame();
		List<Province> provs = new ArrayList<>();
		Set<Integer> provsIds = new HashSet<>();
		provs.add(prov);
		provsIds.add(prov.getId());
		for (int i = 0; i < provs.size() && provs.size() < minProvinces; i++) {
			List<Province> suitableNeighbors = provs.get(i).getNeighbors().stream().filter(p -> p.getState() == null
					&& !provsIds.contains(p.getId()) && p.getTerrainType().isPopulationPossible())
					.collect(Collectors.toList());
			Iterator<Province> iter = suitableNeighbors.iterator();
			while (iter.hasNext() && provs.size() < minProvinces) {
				Province p = iter.next();
				provs.add(p);
				provsIds.add(p.getId());
			}
		}

		if (provs.size() < minProvinces) {
			return;
		}
		DataState dc = new DataState();
		dc.setId(game.nextStateId());
		dc.setName("#" + String.valueOf(dc.getId()));
		dc.setColor(createNewColorForState(game));
		State state = new State();
		state.buildFrom(game, dc);
		provs.forEach(p -> state.addProvince(p));
		state.setCapital(prov);
		game.registerState(state);
	}

	private static Color createNewColorForState(Game game) {
		CwRandom random = game.getGameParams().getRandom();
		int minValue = 50;
		int minDiff = 50;
		Color color = null;
		while (color == null) {
			int r = minValue + random.nextInt(255 - minValue);
			int g = minValue + random.nextInt(255 - minValue);
			int b = minValue + random.nextInt(255 - minValue);
			// TODO match with colors of others states
			if (Math.abs(r - g) >= minDiff && Math.abs(r - b) >= minDiff && Math.abs(b - g) >= minDiff) {
				color = new Color(r, g, b);
			}
		}
		return color;
	}

}
