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
public class GsProvScienceInfoPane extends InternalInfoPane {
	private GameScene gameScene;

	private List<RowValue> allRows;
	private RowValue valuesScienceAgriculture;
	private RowValue valuesScienceMedicine;
	private RowValue valuesScienceAdministration;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.prov.science.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();
		allRows = new ArrayList<>();

		int idx = 0;
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
			switch (prov.getTerrainType()) {
			case GRASSLAND:
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

	private void setLabelText(RowValue l, String txt) {
		l.setValue(txt);
		l.setVisible(true);
	}

}
