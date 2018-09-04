package com.cwsni.world.client.desktop.game.infopanels;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.client.desktop.util.InternalInfoPane;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsGlobalInfoPane extends InternalInfoPane {

	private GameScene gameScene;

	private RowValue valuesCountriesLabel;
	private RowValue valuesTotalPopsLabel;
	private RowValue valuesTurnLabel;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.global.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();
		setInfoRows(new ArrayList<>());

		int idx = 0;
		valuesTurnLabel = addRow("info.pane.global.turn", grid, idx++);
		valuesCountriesLabel = addRow("info.pane.global.countries", grid, idx++);
		valuesTotalPopsLabel = addRow("info.pane.global.population", grid, idx++);

		return grid;
	}

	protected void refreshInfoInternal() {
		setLabelText(valuesTurnLabel, DataFormatter.toString(gameScene.getGame().getTurn().getDateTexToDisplay()));
		setLabelText(valuesCountriesLabel, DataFormatter.toString(gameScene.getGame().getCountries().size()));
		setLabelTextWithLongFormatterAndValueTooltip(valuesTotalPopsLabel,
				gameScene.getGame().getGameTransientStats().getPopulationTotal());
	}

	@Override
	protected boolean hasDataForUser() {
		return gameScene.getGame() != null;
	}

}
