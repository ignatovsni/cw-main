package com.cwsni.world.model.data;

public class DataArmy {
	/**
	 * Maybe id should be string as "country-id" + "-" +"army-id" - to avoid
	 * constantly increasing of {@link DataGame#lastArmyId} Or I can search min
	 * value while the game is loading and then make 1) min--; 2) find max; 3)
	 * max++;
	 */
	private int id;
	private int training;
	private int organisation;
	private int equipment;
	private Integer province;
	private DataPopulation population;

	public DataArmy() {
	}

	public DataArmy(int id) {
		this.id = id;
		this.population = new DataPopulation();
	}

	public int getTraining() {
		return training;
	}

	public void setTraining(int training) {
		this.training = training;
	}

	public int getOrganisation() {
		return organisation;
	}

	public void setOrganisation(int organisation) {
		this.organisation = organisation;
	}

	public int getEquipment() {
		return equipment;
	}

	public void setEquipment(int equipment) {
		this.equipment = equipment;
	}

	public Integer getProvince() {
		return province;
	}

	public void setProvince(Integer province) {
		this.province = province;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public DataPopulation getPopulation() {
		return population;
	}

	public void setPopulation(DataPopulation population) {
		this.population = population;
	}

}
