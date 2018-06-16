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
			enableMapModeButton(toolBarMapGeoMode, MapMode.GEO);
		});
		toolBarMapGeoMode.setEffect(new Lighting());

		Button toolBarMapPopsMode = new Button(getMessage("toolbar.map.mode.population.button.text"));
		toolBarMapPopsMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.population.button.tooltip")));
		toolBarMapPopsMode.setOnAction(e -> {
			enableMapModeButton(toolBarMapPopsMode, MapMode.POPULATION);
		});

		toolBarMapModes = new ArrayList<>();
		toolBarMapModes.add(toolBarMapGeoMode);
		toolBarMapModes.add(toolBarMapPopsMode);
		getItems().addAll(toolBarMapModes);
	}
	
	private void enableMapModeButton(Button buttonMode, MapMode mapMode) {
		gameScene.getWorldMap().setMapModeAndRedraw(mapMode);
		gameScene.setMapMode(mapMode); 
		toolBarMapModes.forEach(b -> b.setEffect(null));
		buttonMode.setEffect(new Lighting());
	}
	
}
