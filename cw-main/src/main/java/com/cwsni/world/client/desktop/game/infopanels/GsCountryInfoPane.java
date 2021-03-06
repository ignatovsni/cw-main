package com.cwsni.world.client.desktop.game.infopanels;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Province;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsCountryInfoPane extends InternalInfoPane {

	private GameScene gameScene;

	private RowValue valuesNameLabel;
	private RowValue valuesProvincesLabel;
	private RowValue valuesPopulationLabel;
	private RowValue valuesRecruitsLabel;
	private RowValue valuesMoneyLabel;
	private RowValue valuesMoneyExpensesLabel;
	private RowValue valuesFocusLabel;
	private RowValue valuesArmiesSoldiersLabel;
	private RowValue valuesCausaltiesLabel;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.country.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();
		setInfoRows(new ArrayList<>());

		int idx = 0;
		valuesNameLabel = addRow("info.pane.country.name", grid, idx++);
		valuesProvincesLabel = addRow("info.pane.country.provinces", grid, idx++);
		valuesPopulationLabel = addRow("info.pane.country.population", grid, idx++);
		valuesRecruitsLabel = addRow("info.pane.country.recruits", grid, idx++);
		valuesMoneyLabel = addRow("info.pane.country.money", grid, idx++);
		valuesMoneyExpensesLabel = addRow("info.pane.country.money.expenses", grid, idx++);
		valuesFocusLabel = addRow("info.pane.country.focus", grid, idx++);
		valuesArmiesSoldiersLabel = addRow("info.pane.country.armies-soldiers", grid, idx++);
		valuesCausaltiesLabel = addRow("info.pane.country.casualties", grid, idx++);

		return grid;
	}

	protected void refreshInfoInternal() {
		Province prov = gameScene.getSelectedProvince();
		Country country = prov.getCountry();
		setLabelText(valuesNameLabel, country.getName());
		setLabelText(valuesProvincesLabel, DataFormatter.toLong(country.getProvinces().size()));
		setLabelTextWithLongFormatterAndValueTooltip(valuesPopulationLabel, country.getPopulationAmount());
		setLabelTextWithLongFormatterAndValueTooltip(valuesRecruitsLabel, country.getAvailablePeopleForRecruiting());
		setLabelText(valuesMoneyLabel, DataFormatter.toLong((long) country.getMoney()) + " / "
				+ DataFormatter.toLong((long) country.getIncome()));
		setLabelText(valuesMoneyExpensesLabel,
				DataFormatter.toLong((long) country.getMoneyBudget().getExpensesForArmyCost()) + " / "
						+ DataFormatter.toLong((long) country.getMoneyBudget().getExpensesForSupportGovernment()));
		setLabelText(valuesFocusLabel, DataFormatter.toLong((long) (country.getFocus().getValue() * 100)));
		String armies = DataFormatter.toLong(country.getArmies().size()) + " / "
				+ DataFormatter.toLong(country.getArmiesSoldiers());
		setLabelText(valuesArmiesSoldiersLabel, armies);
		setLabelText(valuesCausaltiesLabel, createTextForCasualties(country));
	}

	private String createTextForCasualties(Country country) {
		StringBuilder sb = new StringBuilder();
		sb.append(Math.round(country.getLoyaltyToCountryFromCountryCasualties() * 100));
		sb.append(" ");
		sb.append(getMessage("info.pane.country.casualties.loayalty"));
		sb.append("(");
		sb.append(DataFormatter.toLong((long) country.getCasualties()));
		sb.append(")");
		return sb.toString();
	}

	@Override
	protected boolean hasDataForUser() {
		Province prov = gameScene.getSelectedProvince();
		if (prov == null) {
			return false;
		} else {
			return prov.getCountry() != null;
		}
	}

}
