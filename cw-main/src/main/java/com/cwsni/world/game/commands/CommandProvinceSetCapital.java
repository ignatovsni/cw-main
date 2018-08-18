package com.cwsni.world.game.commands;

import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.player.PCountry;
import com.cwsni.world.model.player.PGame;
import com.cwsni.world.model.player.interfaces.IPProvince;

public class CommandProvinceSetCapital extends CommandProvince {

	public CommandProvinceSetCapital(int provinceId) {
		super(provinceId);
	}

	@Override
	public void apply() {
		Game game = country.getGame();
		Province province = game.getMap().findProvById(provinceId);
		if (province == null) {
			addError("destination province not found. id = " + provinceId);
			return;
		}
		if (!ComparisonTool.isEqual(province.getCountryId(), country.getId())) {
			addError("destination country id = " + province.getCountryId() + " but country.id = " + country.getId());
			return;
		}
		country.setCapital(province);
	}

	@Override
	public Object apply(PCountry country, CommandErrorHandler errorHandler) {
		PGame game = country.getGame();
		IPProvince province = game.findProvById(provinceId);
		if (province == null) {
			addError("destination province not found. id = " + provinceId);
			return null;
		}
		if (!ComparisonTool.isEqual(province.getCountryId(), country.getId())) {
			addError("destination country id = " + province.getCountryId() + " but country.id = " + country.getId());
			return null;
		}
		country.cmcSetCapital(province);
		return province;
	}

}
