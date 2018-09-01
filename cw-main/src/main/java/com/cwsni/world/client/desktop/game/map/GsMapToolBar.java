package com.cwsni.world.client.desktop.game.map;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.GameScene;
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
		addButton("diplomacy", MapMode.DIPLOMACY);
		addButton("culture", MapMode.CULTURE);
		addButton("population", MapMode.POPULATION);
		addButton("government-influence", MapMode.GOVERNMENT_INFLUENCE);
		addButton("loyalty", MapMode.LOYALTY);
		addButton("states", MapMode.STATES);
		addButton("wealth", MapMode.WEALTH);
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
				gameScene.openNewMapForMode(getRealMode(mapMode), tooltip);
			}
		});
		return button;
	}

	private void addButtonToPane(Button button) {
		toolBarMapModes.add(button);
		getItems().add(button);
	}

	private void pressMapModeButton(Button buttonMode, MapMode mapMode) {
		mapMode = getRealMode(mapMode);
		gameScene.setMapModeAndRedraw(mapMode);
		toolBarMapModes.forEach(b -> b.setEffect(null));
		buttonMode.setEffect(new Lighting());
	}

	private MapMode getRealMode(MapMode mapMode) {
		if (MapMode.DIPLOMACY.equals(mapMode)) {
			switch (gameScene.getWorldMap().getMapMode()) {
			case DIPLOMACY:
				mapMode = MapMode.DIPLOMACY_REACHABLE_LANDS;
				break;
			case DIPLOMACY_REACHABLE_LANDS:
				mapMode = MapMode.DIPLOMACY;
				break;
			default:
				break;
			}
		} else if (MapMode.SOIL.equals(mapMode)) {
			switch (gameScene.getWorldMap().getMapMode()) {
			case SOIL:
				mapMode = MapMode.SOIL_RAW_FERTILITY;
				break;
			case SOIL_RAW_FERTILITY:
				mapMode = MapMode.SOIL;
				break;
			default:
				break;
			}
		}else if (MapMode.LOYALTY.equals(mapMode)) {
			switch (gameScene.getWorldMap().getMapMode()) {
			case LOYALTY:
				mapMode = MapMode.LOYALTY_DANGEROUS;
				break;
			case LOYALTY_DANGEROUS:
				mapMode = MapMode.LOYALTY;
				break;
			default:
				break;
			}
		}
		return mapMode;
	}

}
