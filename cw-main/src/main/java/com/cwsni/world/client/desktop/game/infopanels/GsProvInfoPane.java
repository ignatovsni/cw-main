package com.cwsni.world.client.desktop.game.infopanels;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.engine.Population;
import com.cwsni.world.model.engine.Province;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsProvInfoPane extends InternalInfoPane {

	private GameScene gameScene;

	private RowValue valuesNameLabel;
	private RowValue valuesSizeLabel;
	private RowValue valuesTerrainTypeLabel;
	private RowValue valuesPopsLabel;
	private RowValue valuesRecruitsLabel;
	private RowValue valuesWealthLabel;
	private RowValue valuesGovInfluenceLabel;
	private RowValue valuesCountryLoyalty;
	private RowValue valuesStateLoyalty;
	private RowValue valuesInfrastructureLabel;
	private RowValue valuesSoilAreaLabel;
	private RowValue valuesSoilFertilityLabel;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.prov.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();
		setInfoRows(new ArrayList<>());

		int idx = 0;
		valuesNameLabel = addRow("info.pane.prov.name", grid, idx++);
		valuesTerrainTypeLabel = addRow("info.pane.prov.terrain-type", grid, idx++);
		valuesSizeLabel = addRow("info.pane.prov.size", grid, idx++);
		valuesPopsLabel = addRow("info.pane.prov.population", grid, idx++);
		valuesRecruitsLabel = addRow("info.pane.prov.recruits", grid, idx++);
		valuesWealthLabel = addRow("info.pane.prov.wealth", grid, idx++);
		valuesGovInfluenceLabel = addRow("info.pane.prov.government-influence", grid, idx++);
		valuesCountryLoyalty = addRow("info.pane.prov.country.loyalty", grid, idx++);
		valuesStateLoyalty = addRow("info.pane.prov.state.loyalty", grid, idx++);
		valuesInfrastructureLabel = addRow("info.pane.prov.infrastructure", grid, idx++);
		valuesSoilAreaLabel = addRow("info.pane.prov.soil.area", grid, idx++);
		valuesSoilFertilityLabel = addRow("info.pane.prov.soil.fertility", grid, idx++);

		return grid;
	}

	protected void refreshInfoInternal() {
		Province prov = gameScene.getSelectedProvince();
		setLabelText(valuesNameLabel, prov.getName());
		setLabelText(valuesTerrainTypeLabel, getMessage(prov.getTerrainType().getCodeMsg()));
		switch (prov.getTerrainType()) {
		case GRASSLAND:
			setLabelText(valuesSizeLabel, DataFormatter.toLong((long) prov.getSize()));
			setLabelTextWithLongFormatterAndValueTooltip(valuesPopsLabel, prov.getPopulationAmount());
			setLabelTextWithLongFormatterAndValueTooltip(valuesRecruitsLabel, prov.getAvailablePeopleForRecruiting());
			setLabelText(valuesWealthLabel, createTextForWealth(prov));
			setLabelText(valuesGovInfluenceLabel,
					String.valueOf(Math.round(prov.getGovernmentInfluence() * 100)) + "%");
			setLabelText(valuesCountryLoyalty, String.valueOf(Math.round(prov.getLoyaltyToCountry() * 100)) + "%",
					Population.createDescriptionForLoyaltyChangesPerYear(gameScene.getGame(), prov,
							getMessageSource()));
			setLabelText(valuesStateLoyalty, String.valueOf(Math.round(prov.getLoyaltyToState() * 100)) + "%");
			setLabelText(valuesInfrastructureLabel, createTextForInfrastructure(prov));
			setLabelTextWithLongFormatterAndValueTooltip(valuesSoilAreaLabel, (long) prov.getSoilArea());
			setLabelText(valuesSoilFertilityLabel, createTextForSoilQuality(prov));
			break;
		case OCEAN:
		case MOUNTAIN:
			break;
		}
	}

	private String createTextForSoilQuality(Province prov) {
		StringBuilder sb = new StringBuilder();
		sb.append(DataFormatter.doubleWithPrecison(prov.getSoilFertility(), 3));
		sb.append(" (");
		sb.append(DataFormatter.doubleWithPrecison(prov.getSoilNaturalFertility(), 3));
		sb.append(")");
		return sb.toString();
	}

	private String createTextForWealth(Province prov) {
		StringBuilder sb = new StringBuilder();
		if (prov.getPopulationAmount() > 0) {
			sb.append(String.valueOf(Math.round(prov.getWealthLevel() * 100))).append("%");
		}
		sb.append(" (")
				.append(DataFormatter.toLong((long) (prov.getRawWealthOfProvince() + prov.getWealthOfPopulation())))
				.append(")");
		return sb.toString();
	}

	private String createTextForInfrastructure(Province prov) {
		double infr = prov.getInfrastructurePercent();
		return DataFormatter.toFraction(infr * 100) + "% (" + DataFormatter.toLong(prov.getInfrastructure()) + ")";
	}

	@Override
	protected boolean hasDataForUser() {
		return gameScene.getSelectedProvince() != null;
	}

}
