package com.cwsni.world.client.desktop.game;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.Province;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsProvInfoPane extends InternalInfoPane {

	private GameScene gameScene;

	private Label valuesNameLabel;
	private Label valuesPopsLabel;
	private Label valuesSoilAmountLabel;
	private Label valuesSoilFertilityLabel;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.prov.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();

		valuesNameLabel = addRow("info.pane.prov.name", grid, 0);
		valuesPopsLabel = addRow("info.pane.prov.population", grid, 1);
		valuesSoilAmountLabel = addRow("info.pane.prov.soil.amount", grid, 2);
		valuesSoilFertilityLabel = addRow("info.pane.prov.soil.fertility", grid, 3);

		return grid;
	}

	public void refreshInfo() {
		Province prov = gameScene.getSelectedProvince();
		if (prov != null) {
			valuesNameLabel.setText(prov.getName());
			valuesPopsLabel.setText(toInt(prov.getPopulationAmount()));
			valuesSoilAmountLabel.setText(toInt(prov.getSoilAmount()));
			valuesSoilFertilityLabel.setText(toInt(prov.getSoilFertility()));
		} else {
			valuesNameLabel.setText("");
			valuesPopsLabel.setText("");
			valuesSoilAmountLabel.setText("");
			valuesSoilFertilityLabel.setText("");
		}
	}

}
