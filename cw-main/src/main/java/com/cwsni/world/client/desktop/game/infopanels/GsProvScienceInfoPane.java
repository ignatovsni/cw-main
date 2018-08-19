package com.cwsni.world.client.desktop.game.infopanels;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.Province;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsProvScienceInfoPane extends InternalInfoPane {
	private GameScene gameScene;

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
		setInfoRows(new ArrayList<>());

		int idx = 0;
		valuesScienceAgriculture = addRow("science.agriculture.name", grid, idx++);
		valuesScienceMedicine = addRow("science.medicine.name", grid, idx++);
		valuesScienceAdministration = addRow("science.administration.name", grid, idx++);

		return grid;
	}

	protected void refreshInfoInternal() {
		Province prov = gameScene.getSelectedProvince();
		switch (prov.getTerrainType()) {
		case GRASSLAND:
			setLabelTextWithLongFormatterAndValueTooltip(valuesScienceAgriculture, prov.getScienceAgriculture());
			setLabelTextWithLongFormatterAndValueTooltip(valuesScienceMedicine, prov.getScienceMedicine());
			setLabelTextWithLongFormatterAndValueTooltip(valuesScienceAdministration, prov.getScienceAdministration());
			break;
		case OCEAN:
			break;
		}
	}

	@Override
	protected boolean hasDataForUser() {
		return gameScene.getSelectedProvince() != null;
	}

}
