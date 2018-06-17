package com.cwsni.world.client.desktop.game;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.InternalInfoPane;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsGlobalInfoPane extends InternalInfoPane {

	private GameScene gameScene;

	private RowValue valuesProvsLabel;
	private RowValue valuesTotalPopsLabel;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.global.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();

		valuesProvsLabel = addRow("Provinces", grid, 0);
		valuesTotalPopsLabel = addRow("Total population", grid, 1);

		return grid;
	}

	public void refreshInfo() {
		if (gameScene.getGame() != null) {
			valuesProvsLabel.setValue(toString(gameScene.getGame().getMap().getProvinces().size()));
			valuesTotalPopsLabel.setValue(toInt(gameScene.getGame().getGameStats().getTotalPopulation()));
		}
	}

}
