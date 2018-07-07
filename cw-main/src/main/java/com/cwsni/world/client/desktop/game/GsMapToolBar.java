package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.map.MapMode;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseButton;

@Component
@Scope("prototype")
public class GsMapToolBar extends ToolBar {

	@Autowired
	private LocaleMessageSource messageSource;

	private GameScene gameScene;
	private List<Button> toolBarMapModes;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		toolBarMapModes = new ArrayList<>();

		addButton("geo", MapMode.GEO).setEffect(new Lighting());
		addButton("political", MapMode.POLITICAL);
		addButton("population", MapMode.POPULATION);
		addButton("wealth", MapMode.WEALTH);
		addButton("culture", MapMode.CULTURE);
		addButton("infrastructure", MapMode.INFRASTRUCTURE);
		addButton("soil", MapMode.SOIL);
		addButton("disease", MapMode.DISEASE);
		getItems().add(new Label("|"));
		addButton("science.agriculture", MapMode.SCIENCE_AGRICULTURE);
		addButton("science.medicine", MapMode.SCIENCE_MEDICINE);
		addButton("science.administration", MapMode.SCIENCE_ADMINISTRATION);
		getItems().add(new Label("|"));

	}

	private Button addButton(String modeCode, MapMode mapMode) {
		Button button = new Button(getMessage("toolbar.map.mode." + modeCode + ".button.text"));
		String tooltip = getMessage("toolbar.map.mode." + modeCode + ".button.tooltip");
		button.setTooltip(new Tooltip(tooltip + getMessage("toolbar.map.mode.button.tooltip")));
		button.setOnAction(e -> {
			pressMapModeButton(button, mapMode);
		});
		addButtonToPane(button);
		button.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				gameScene.openNewMapForMode(mapMode, tooltip);
			}
		});
		return button;
	}

	private void addButtonToPane(Button button) {
		toolBarMapModes.add(button);
		getItems().add(button);
	}

	private void pressMapModeButton(Button buttonMode, MapMode mapMode) {
		if (MapMode.POPULATION.equals(mapMode)) {
			switch (gameScene.getWorldMap().getMapMode()) {
			case POPULATION:
				mapMode = MapMode.POPULATION_2;
				break;
			case POPULATION_2:
				mapMode = MapMode.POPULATION;
				break;
			default:
				break;
			}
		} else if (MapMode.SOIL.equals(mapMode)) {
			switch (gameScene.getWorldMap().getMapMode()) {
			case SOIL:
				mapMode = MapMode.SOIL_2;
				break;
			case SOIL_2:
				mapMode = MapMode.SOIL;
				break;
			default:
				break;
			}
		}
		gameScene.setMapModeAndRedraw(mapMode);
		toolBarMapModes.forEach(b -> b.setEffect(null));
		buttonMode.setEffect(new Lighting());
	}

}
