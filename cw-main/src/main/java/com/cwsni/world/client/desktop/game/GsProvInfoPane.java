package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;

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
	// TODO схлопывающиеся панели и метка или самая важная информации в заголовке
	// справа. для событий - их количество, к примеру
	private GameScene gameScene;

	private List<RowValue> allRows;
	private RowValue valuesNameLabel;
	private RowValue valuesSizeLabel;
	private RowValue valuesTerrainTypeLabel;
	private RowValue valuesPopsLabel;
	private RowValue valuesInfrastructureLabel;
	private RowValue valuesSoilAreaLabel;
	private RowValue valuesSoilFertilityLabel;
	private RowValue valuesScienceLabel;
	private RowValue valuesScienceAgriculture;
	private RowValue valuesScienceMedicine;
	private RowValue valuesScienceAdministration;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.prov.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();
		allRows = new ArrayList<>();

		int idx = 0;
		valuesNameLabel = addRow("info.pane.prov.name", grid, allRows, idx++);
		valuesTerrainTypeLabel = addRow("info.pane.prov.terrain-type", grid, allRows, idx++);
		valuesSizeLabel = addRow("info.pane.prov.size", grid, allRows, idx++);
		valuesPopsLabel = addRow("info.pane.prov.population", grid, allRows, idx++);
		valuesInfrastructureLabel = addRow("info.pane.prov.infrastructure", grid, allRows, idx++);
		valuesSoilAreaLabel = addRow("info.pane.prov.soil.area", grid, allRows, idx++);
		valuesSoilFertilityLabel = addRow("info.pane.prov.soil.fertility", grid, allRows, idx++);

		valuesScienceLabel = addRow("info.pane.prov.science.title", grid, allRows, idx++);
		valuesScienceAgriculture = addRow("science.agriculture.name", grid, allRows, idx++);
		valuesScienceMedicine = addRow("science.medicine.name", grid, allRows, idx++);
		valuesScienceAdministration = addRow("science.administration.name", grid, allRows, idx++);

		return grid;
	}

	private RowValue addRow(String msgCode, GridPane grid, List<RowValue> rows, int row) {
		RowValue newRowValue = addRow(msgCode, grid, row, "");
		rows.add(newRowValue);
		return newRowValue;
	}

	public void refreshInfo() {
		Province prov = gameScene.getSelectedProvince();
		allRows.forEach(l -> l.setVisible(false));
		if (prov != null) {
			setLabelText(valuesNameLabel, prov.getName());
			setLabelText(valuesTerrainTypeLabel, getMessage(prov.getTerrainType().getCodeMsg()));
			switch (prov.getTerrainType()) {
			case GRASSLAND:
				setLabelText(valuesSizeLabel, DataFormatter.toLong(prov.getSize()));
				setLabelText(valuesPopsLabel, DataFormatter.toLong(prov.getPopulationAmount()));
				setLabelText(valuesInfrastructureLabel, createTextForInfrastructure(prov));
				setLabelText(valuesSoilAreaLabel, DataFormatter.toLong(prov.getSoilArea()));
				setLabelText(valuesSoilFertilityLabel, DataFormatter.toFraction(prov.getSoilFertility()));
				setLabelText(valuesScienceLabel, "");
				setLabelText(valuesScienceAgriculture, DataFormatter.toLong(prov.getScienceAgriculture()));
				setLabelText(valuesScienceMedicine, DataFormatter.toLong(prov.getScienceMedicine()));
				setLabelText(valuesScienceAdministration, DataFormatter.toLong(prov.getScienceAdministration()));
				break;
			case OCEAN:
				break;
			}
		} else {
			allRows.forEach(l -> l.setValue(""));
		}
	}

	private String createTextForInfrastructure(Province prov) {
		double infr = prov.getInfrastructure();
		return DataFormatter.toFraction(infr * 100) + " (" + DataFormatter.toLong(prov.getInfrastructureAbsoluteValue())
				+ ")";
	}

	private void setLabelText(RowValue l, String txt) {
		l.setValue(txt);
		l.setVisible(true);
	}

}
