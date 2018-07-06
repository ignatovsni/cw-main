package com.cwsni.world.model.data;

public class DataArmy {
	/*
	 * TODO нужно запоминать состав армии - культуру, против своих сражаться будут
	 * неохотно
	 */

	private int id;
	private int soldiers;
	private int training;
	private int organisation;
	private int equipment;
	private Integer province;

	public DataArmy() {
	}

	public DataArmy(int id) {
		this.setId(id);
	}

	public int getSoldiers() {
		return soldiers;
	}

	public void setSoldiers(int soldiers) {
		this.soldiers = Math.max(0, soldiers);
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

}
