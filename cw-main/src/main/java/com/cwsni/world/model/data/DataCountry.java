package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.client.desktop.util.DataFormatter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "name", "color", "capital", "firstCapital", "focus", "budget", "scienceBudget", "isAI" })
public class DataCountry {

	private int id;
	private String name;
	private Color color;
	private Integer capital;
	private Integer firstCapital;
	private double focus;
	private List<DataArmy> armies = new ArrayList<>();
	private DataMoneyBudget budget;
	private DataScienceBudget scienceBudget;
	private boolean isAI = true;
	private String aiScriptName = "default";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public List<DataArmy> getArmies() {
		return armies;
	}

	public void setArmies(List<DataArmy> armies) {
		this.armies = armies;
	}

	public boolean isAI() {
		return isAI;
	}

	public void setAI(boolean isAI) {
		this.isAI = isAI;
	}

	public Integer getCapital() {
		return capital;
	}

	public void setCapital(Integer capital) {
		this.capital = capital;
	}

	public Integer getFirstCapital() {
		return firstCapital;
	}

	public void setFirstCapital(Integer firstCapital) {
		this.firstCapital = firstCapital;
	}

	public double getFocus() {
		return focus;
	}

	public void setFocus(double focus) {
		this.focus = DataFormatter.doubleWith3points(focus);
	}

	public DataMoneyBudget getBudget() {
		return budget;
	}

	public void setBudget(DataMoneyBudget budget) {
		this.budget = budget;
	}

	public DataScienceBudget getScienceBudget() {
		return scienceBudget;
	}

	public void setScienceBudget(DataScienceBudget scienceBudget) {
		this.scienceBudget = scienceBudget;
	}

	public String getAiScriptName() {
		return aiScriptName;
	}

	public void setAiScriptName(String aiScriptName) {
		this.aiScriptName = aiScriptName;
	}

}
