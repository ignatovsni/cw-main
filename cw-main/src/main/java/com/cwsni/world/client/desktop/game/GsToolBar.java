package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.map.MapMode;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Lighting;

@Component
@Scope("prototype")
public class GsToolBar extends ToolBar {

	@Autowired
	private LocaleMessageSource messageSource;

	private GameScene gameScene;
	private List<Button> toolBarMapModes;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Button toolBarMapGeoMode = new Button(getMessage("toolbar.map.mode.geo.button.text"));
		toolBarMapGeoMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.geo.button.tooltip")));
		toolBarMapGeoMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapGeoMode, MapMode.GEO);
		});
		toolBarMapGeoMode.setEffect(new Lighting());

		Button toolBarMapPopsMode = new Button(getMessage("toolbar.map.mode.population.button.text"));
		toolBarMapPopsMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.population.button.tooltip")));
		toolBarMapPopsMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapPopsMode, MapMode.POPULATION);
		});

		Button toolBarMapSoilMode = new Button(getMessage("toolbar.map.mode.soil.button.text"));
		toolBarMapSoilMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.soil.button.tooltip")));
		toolBarMapSoilMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapSoilMode, MapMode.SOIL);
		});
		
		Button toolBarMapDiseaseMode = new Button(getMessage("toolbar.map.mode.disease.button.text"));
		toolBarMapDiseaseMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.disease.button.tooltip")));
		toolBarMapDiseaseMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapDiseaseMode, MapMode.DISEASE);
		});

		toolBarMapModes = new ArrayList<>();
		toolBarMapModes.add(toolBarMapGeoMode);
		toolBarMapModes.add(toolBarMapPopsMode);
		toolBarMapModes.add(toolBarMapSoilMode);
		toolBarMapModes.add(toolBarMapDiseaseMode);
		getItems().addAll(toolBarMapModes);
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
		gameScene.getWorldMap().setMapModeAndRedraw(mapMode);
		gameScene.setMapMode(mapMode);
		toolBarMapModes.forEach(b -> b.setEffect(null));
		buttonMode.setEffect(new Lighting());
	}

}
