package com.cwsni.world.game.commands;

import com.cwsni.world.model.engine.Province;
import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.interfaces.IPProvince;

public class CommandProvinceSetCapital extends CommandProvince {

	public CommandProvinceSetCapital(int provinceId) {
		super(provinceId);
	}

	@Override
	public void apply() {
		Province province = getAndCheckProvince(provinceId);
		if (province == null) {
			return;
		}
		country.setCapital(province);
	}

	@Override
	public Object apply(PCountry country, CommandErrorHandler errorHandler) {
		IPProvince province = getAndCheckProvince(country, provinceId);
		if (province == null) {
			return null;
		}
		country.cmcSetCapital(province);
		return province;
	}

}
