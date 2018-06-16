package com.cwsni.world.client.desktop.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.Province;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsProvInfoPane extends InternalInfoPane {

	@Autowired
	private LocaleMessageSource messageSource;

	private GameScene gameScene;

	private Label valuesNameLabel;

	private Label valuesPopsLabel;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.prov.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();

		grid.add(createGridNameColumn(getMessage("info.pane.prov.name")), 0, 0);
		valuesNameLabel = createGridValueColumn(0);
		grid.add(valuesNameLabel, 1, 0);
		
		grid.add(createGridNameColumn(getMessage("info.pane.prov.population")), 0, 1);
		valuesPopsLabel = createGridValueColumn(0);
		grid.add(valuesPopsLabel, 1, 1);

		return grid;
	}

	public void refreshInfo() {
		Province prov = gameScene.getSelectedProvince();
		if (prov != null) {
			valuesNameLabel.setText(prov.getName());
			valuesPopsLabel.setText(toInt(prov.getPopulationAmount()));
		} else {
			valuesNameLabel.setText("");
			valuesPopsLabel.setText("");
		}
	}

}
