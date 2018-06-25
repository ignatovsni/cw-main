package com.cwsni.world.client.desktop.game;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.client.desktop.util.InternalInfoPane;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsGlobalInfoPane extends InternalInfoPane {

	private GameScene gameScene;

	private RowValue valuesProvsLabel;
	private RowValue valuesTotalPopsLabel;
	private RowValue valuesTurnLabel;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.global.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();

		int idx = 0;
		valuesTurnLabel = addRow("info.pane.global.turn", grid, idx++);
		valuesProvsLabel = addRow("Provinces", grid, idx++);
		valuesTotalPopsLabel = addRow("Total population", grid, idx++);

		return grid;
	}

	public void refreshInfo() {
		if (gameScene.getGame() != null) {
			valuesTurnLabel.setValue(DataFormatter.toString(gameScene.getGame().getTurn().getTurnTexToDisplay()));
			valuesProvsLabel.setValue(DataFormatter.toString(gameScene.getGame().getMap().getProvinces().size()));
			valuesTotalPopsLabel
					.setValue(DataFormatter.toLong(gameScene.getGame().getGameTransientStats().getPopulationTotal()));
		}
	}

}
