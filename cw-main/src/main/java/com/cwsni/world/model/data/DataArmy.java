package com.cwsni.world.model.data;

import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataArmy {
	/**
	 * Maybe id should be string as "country-id" + "-" +"army-id" - to avoid
	 * constantly increasing of {@link DataGame#lastArmyId} Or I can search min
	 * value while the game is loading and then make 1) min--; 2) find max; 3)
	 * max++;
	 */
	private long id;
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 4)
	private double training;
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 4)
	private double organisation;
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 4)
	private double equipment;
	private Integer province;
	private DataPopulation population;

	public DataArmy() {
	}

	public DataArmy(long id) {
		this.id = id;
		this.population = new DataPopulation();
	}

	public Integer getProvince() {
		return province;
	}

	public void setProvince(Integer province) {
		this.province = province;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public DataPopulation getPopulation() {
		return population;
	}

	public void setPopulation(DataPopulation population) {
		this.population = population;
	}

	public double getTraining() {
		return training;
	}

	public void setTraining(double training) {
		this.training = training;
	}

	public double getOrganisation() {
		return organisation;
	}

	public void setOrganisation(double organisation) {
		this.organisation = organisation;
	}

	public double getEquipment() {
		return equipment;
	}

	public void setEquipment(double equipment) {
		this.equipment = equipment;
	}

}
