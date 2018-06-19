package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.Province;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsProvInfoPane extends InternalInfoPane {
// TODO схлопывающиеся панели и метка или самая важная информации в заголовке справа. для событий - их количество, к примеру
	private GameScene gameScene;

	private RowValue valuesNameLabel;
	private RowValue valuesTerrainTypeLabel;
	private RowValue valuesPopsLabel;
	private RowValue valuesSoilAreaLabel;
	private RowValue valuesSoilFertilityLabel;
	private List<RowValue> allRows;

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
		valuesPopsLabel = addRow("info.pane.prov.population", grid, allRows, idx++);
		valuesSoilAreaLabel = addRow("info.pane.prov.soil.area", grid, allRows, idx++);
		valuesSoilFertilityLabel = addRow("info.pane.prov.soil.fertility", grid, allRows, idx++);

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
				setLabelText(valuesPopsLabel, toLong(prov.getPopulationAmount()));
				setLabelText(valuesSoilAreaLabel, toLong(prov.getSoilAreaEff()));
				setLabelText(valuesSoilFertilityLabel, toFraction(prov.getSoilFertilityEff()));
				break;
			case OCEAN:
				break;
			}
		} else {
			allRows.forEach(l -> l.setValue(""));
		}
	}

	private void setLabelText(RowValue l, String txt) {
		l.setValue(txt);
		l.setVisible(true);
	}

}
