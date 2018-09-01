package com.cwsni.world.game.commands;

import com.cwsni.world.model.engine.Province;
import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.util.ComparisonTool;

abstract public class CommandProvince extends Command {

	protected int provinceId;

	public CommandProvince(int provinceId) {
		this.provinceId = provinceId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" provinceId:");
		sb.append(provinceId);
		return sb.toString();
	}

	@Override
	abstract public void apply();

	protected Province getAndCheckProvince(int provId) {
		Province province = country.getGame().getMap().findProvById(provId);
		if (province == null) {
			addError("destination province not found. id = " + provId);
			return null;
		}
		if (!ComparisonTool.isEqual(province.getCountryId(), country.getId())) {
			addError("destination country id = " + province.getCountryId() + " but country.id = " + country.getId());
			return null;
		}
		return province;
	}

	protected IPProvince getAndCheckProvince(PCountry country, int provId) {
		IPProvince province = country.getGame().findProvById(provId);
		if (province == null) {
			addError("destination province not found. id = " + provId);
			return null;
		}
		if (!ComparisonTool.isEqual(province.getCountryId(), country.getId())) {
			addError("destination country id = " + province.getCountryId() + " but country.id = " + country.getId());
			return null;
		}
		return province;
	}

}
