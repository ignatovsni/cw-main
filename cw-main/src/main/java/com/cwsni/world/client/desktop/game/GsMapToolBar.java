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
		Button toolBarMapGeoMode = new Button(getMessage("toolbar.map.mode.geo.button.text"));
		toolBarMapGeoMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.geo.button.tooltip")));
		toolBarMapGeoMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapGeoMode, MapMode.GEO);
		});
		toolBarMapGeoMode.setEffect(new Lighting());

		Button toolBarMapPoliticalMode = new Button(getMessage("toolbar.map.mode.political.button.text"));
		toolBarMapPoliticalMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.political.button.tooltip")));
		toolBarMapPoliticalMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapPoliticalMode, MapMode.POLITICAL);
		});
		
		Button toolBarMapPopsMode = new Button(getMessage("toolbar.map.mode.population.button.text"));
		toolBarMapPopsMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.population.button.tooltip")));
		toolBarMapPopsMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapPopsMode, MapMode.POPULATION);
		});
		
		Button toolBarMapCultureMode = new Button(getMessage("toolbar.map.mode.culture.button.text"));
		toolBarMapCultureMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.culture.button.tooltip")));
		toolBarMapCultureMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapCultureMode, MapMode.CULTURE);
		});
		
		Button toolBarMapInfrMode = new Button(getMessage("toolbar.map.mode.infrastructure.button.text"));
		toolBarMapInfrMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.infrastructure.button.tooltip")));
		toolBarMapInfrMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapInfrMode, MapMode.INFRASTRUCTURE);
		});

		Button toolBarMapSoilMode = new Button(getMessage("toolbar.map.mode.soil.button.text"));
		toolBarMapSoilMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.soil.button.tooltip")));
		toolBarMapSoilMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapSoilMode, MapMode.SOIL);
		});

		Button toolBarMapScienceAgricultureMode = new Button(
				getMessage("toolbar.map.mode.science.agriculture.button.text"));
		toolBarMapScienceAgricultureMode
				.setTooltip(new Tooltip(getMessage("toolbar.map.mode.science.agriculture.button.tooltip")));
		toolBarMapScienceAgricultureMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapScienceAgricultureMode, MapMode.SCIENCE_AGRICULTURE);
		});

		Button toolBarMapScienceMedicineMode = new Button(
				getMessage("toolbar.map.mode.science.medicine.button.text"));
		toolBarMapScienceMedicineMode
				.setTooltip(new Tooltip(getMessage("toolbar.map.mode.science.medicine.button.tooltip")));
		toolBarMapScienceMedicineMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapScienceMedicineMode, MapMode.SCIENCE_MEDICINE);
		});
		
		Button toolBarMapScienceAdministrationMode = new Button(
				getMessage("toolbar.map.mode.science.administration.button.text"));
		toolBarMapScienceAdministrationMode
				.setTooltip(new Tooltip(getMessage("toolbar.map.mode.science.administration.button.tooltip")));
		toolBarMapScienceAdministrationMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapScienceAdministrationMode, MapMode.SCIENCE_ADMINISTRATION);
		});

		Button toolBarMapDiseaseMode = new Button(getMessage("toolbar.map.mode.disease.button.text"));
		toolBarMapDiseaseMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.disease.button.tooltip")));
		toolBarMapDiseaseMode.setOnAction(e -> {
			pressMapModeButton(toolBarMapDiseaseMode, MapMode.DISEASE);
		});

		toolBarMapModes = new ArrayList<>();
		addButtonToPane(toolBarMapGeoMode);
		addButtonToPane(toolBarMapPoliticalMode);
		addButtonToPane(toolBarMapPopsMode);
		addButtonToPane(toolBarMapCultureMode);
		addButtonToPane(toolBarMapInfrMode);
		addButtonToPane(toolBarMapSoilMode);
		addButtonToPane(toolBarMapDiseaseMode);
		getItems().add(new Label("|"));
		addButtonToPane(toolBarMapScienceAgricultureMode);
		addButtonToPane(toolBarMapScienceMedicineMode);
		addButtonToPane(toolBarMapScienceAdministrationMode);
		getItems().add(new Label("|"));
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
