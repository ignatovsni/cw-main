package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.Province;

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
	private RowValue valuesInfrastructureLabel;
	private RowValue valuesSoilAreaLabel;
	private RowValue valuesSoilFertilityLabel;
	// private RowValue valuesArmiesLabel;

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
			setLabelText(valuesSizeLabel, DataFormatter.toLong(prov.getSize()));
			setLabelText(valuesPopsLabel, DataFormatter.toLong(prov.getPopulationAmount()));
			setLabelText(valuesInfrastructureLabel, createTextForInfrastructure(prov));
			setLabelText(valuesSoilAreaLabel, DataFormatter.toLong(prov.getSoilArea()));
			setLabelText(valuesSoilFertilityLabel, DataFormatter.toFraction(prov.getSoilFertility()));
			break;
		case OCEAN:
			break;
		}
	}

	private String createTextForInfrastructure(Province prov) {
		double infr = prov.getInfrastructure();
		return DataFormatter.toFraction(infr * 100) + " (" + DataFormatter.toLong(prov.getInfrastructureAbsoluteValue())
				+ ")";
	}

	@Override
	protected boolean hasDataForUser() {
		return gameScene.getSelectedProvince() != null;
	}

}
