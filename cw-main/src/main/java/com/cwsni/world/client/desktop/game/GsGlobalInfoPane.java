package com.cwsni.world.client.desktop.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.InternalInfoPane;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsGlobalInfoPane extends InternalInfoPane {

	@Autowired
	private LocaleMessageSource messageSource;

	private GameScene gameScene;

	private Label valuesProvsLabel;
	private Label valuesTotalPopsLabel;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.global.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();

		grid.add(createGridNameColumn("Provinces"), 0, 0);
		valuesProvsLabel = createGridValueColumn(0);
		grid.add(valuesProvsLabel, 1, 0);

		grid.add(createGridNameColumn("Total population"), 0, 1);
		valuesTotalPopsLabel = createGridValueColumn(0);
		grid.add(valuesTotalPopsLabel, 1, 1);

		return grid;
	}

	public void refreshInfo() {
		if (gameScene.getGame() != null) {
			valuesProvsLabel.setText(toString(gameScene.getGame().getMap().getProvinces().size()));
			valuesTotalPopsLabel.setText(toInt(gameScene.getGame().getGameStats().getTotalPopulation()));
		}
	}

}
