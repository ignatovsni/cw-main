package com.cwsni.world.model.data;

public class DataArmy {
	/*
	 * TODO нужно запоминать состав армии - культуру, против своих сражаться будут
	 * неохотно
	 */

	private int soldiers;
	private int training;
	private int organisation;
	private int equipment;

	private Integer province;

	public int getSoldiers() {
		return soldiers;
	}

	public void setSoldiers(int soldiers) {
		this.soldiers = soldiers;
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

}
