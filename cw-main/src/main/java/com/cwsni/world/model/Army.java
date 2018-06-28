package com.cwsni.world.model;

import com.cwsni.world.model.data.DataArmy;

public class Army {

	private DataArmy data;
	private Country country;

	public int getId() {
		return data.getId();
	}

	public Country getCountry() {
		return country;
	}

	public int getSoldiers() {
		return data.getSoldiers();
	}

	public void setSoldiers(int soldiers) {
		data.setSoldiers(soldiers);
	}

	public int getTraining() {
		return data.getTraining();
	}

	public void setTraining(int training) {
		data.setTraining(training);
	}

	public int getOrganisation() {
		return data.getOrganisation();
	}

	public void setOrganisation(int organisation) {
		data.setOrganisation(organisation);
	}

	public int getEquipment() {
		return data.getEquipment();
	}

	public void setEquipment(int equipment) {
		data.setEquipment(equipment);
	}

	public Province getLocation() {
		return country.getGame().getMap().findProvById(data.getProvince());
	}

	public void setProvince(Province province) {
		Province p = country.getGame().getMap().findProvById(data.getProvince());
		if (p != null) {
			p.removeArmy(this);
		}
		data.setProvince(province.getId());
		province.addArmy(this);
	}

	DataArmy getDataArmy() {
		return data;
	}

	public void buildFrom(Country country, DataArmy da) {
		this.country = country;
		this.data = da;
	}

	@Override
	public int hashCode() {
		return data.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		return ((Army) obj).getId() == getId();
	}

	public void processNewTurn() {
		Country locationCountry = getLocation().getCountry();
		if (getCountry().equals(locationCountry)) {
			// our land, doing nothing
			return;
		}
		boolean successful = fightIfNeededInCurrentLocation();
		if (successful) {
			// it is our land now!
			if (locationCountry != null) {
				locationCountry.removeProvince(getLocation());
			}
			getCountry().addProvince(getLocation());
		} else {
			// TODO retreat OR do it in fightIfNeededInCurrentLocation()
		}
	}

	private boolean fightIfNeededInCurrentLocation() {
		// TODO
		return true;
	}

	public void dismiss() {
		// TODO
	}

}
