package com.cwsni.world.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cwsni.world.game.ai.ScriptAIHandler;
import com.cwsni.world.model.data.util.DoubleContextualSerializer;
import com.cwsni.world.model.data.util.Precision;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonPropertyOrder({ "id", "name", "color", "capital", "firstCapital", "focus", "budget", "scienceBudget", "isAI" })
public class DataCountry {

	private int id;
	private String name;
	private Color color;
	private Integer capital;
	private Integer firstCapital;
	private DataCountryFocus focus;
	private Set<Integer> provinces = new HashSet<>();
	private List<DataArmy> armies = new ArrayList<>();
	private DataMoneyBudget budget;
	private DataScienceBudget scienceBudget;
	private boolean isAI = true;
	private String aiScriptName = ScriptAIHandler.DEFAULT_SCRIPT;
	private int turnOfCreation;
	private int turnOfRestoring;
	private int turnsOfExistence;
	/**
	 * died people by different reasons (mostly fights and diseases)
	 */
	private double casualties;
	@JsonSerialize(using = DoubleContextualSerializer.class)
	@Precision(precision = 5)
	private double rebelAddChances;
	private Map<Object, Object> aiRecords = new HashMap<>();

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

	public Set<Integer> getProvinces() {
		return provinces;
	}

	public void setProvinces(Set<Integer> provinces) {
		this.provinces = provinces;
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DataCountry)) {
			return false;
		}
		return ((DataCountry) obj).getId() == getId();
	}

	@Override
	public String toString() {
		return "Country with id " + getId();
	}

	public DataCountryFocus getFocus() {
		return focus;
	}

	public void setFocus(DataCountryFocus focus) {
		this.focus = focus;
	}

	public int getTurnOfCreation() {
		return turnOfCreation;
	}

	public void setTurnOfCreation(int turnOfCreation) {
		this.turnOfCreation = turnOfCreation;
	}

	public int getTurnsOfExistence() {
		return turnsOfExistence;
	}

	public void setTurnsOfExistence(int turnsOfExistence) {
		this.turnsOfExistence = turnsOfExistence;
	}

	public double getCasualties() {
		return casualties;
	}

	public void setCasualties(double casualties) {
		this.casualties = casualties;
	}

	public Map<Object, Object> getAiRecords() {
		return aiRecords;
	}

	public void setAiRecords(Map<Object, Object> aiRecords) {
		this.aiRecords = aiRecords;
	}

	public double getRebelAddChances() {
		return rebelAddChances;
	}

	public void setRebelAddChances(double rebelAddChances) {
		this.rebelAddChances = rebelAddChances;
	}

	public int getTurnOfRestoring() {
		return turnOfRestoring;
	}

	public void setTurnOfRestoring(int turnOfRestoring) {
		this.turnOfRestoring = turnOfRestoring;
	}

}
