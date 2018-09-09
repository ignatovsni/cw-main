package com.cwsni.world.client.desktop.game.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.ApplicationSettings;
import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.game.events.GameEventHandler;

import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
@Scope("prototype")
public class GsMapModePanel extends BorderPane {

	private static final int MAX_MODE_BUTTONS = 20;

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private ApplicationSettings applicationSettings;

	@Autowired
	private GameEventHandler gameEventHandler;

	private GameScene gameScene;

	private List<Button> buttonsMapModes;

	private Map<String, MapMode> possibleModes;
	private List<String> contextMenuModes;
	private List<String> availableDefaultModes = Arrays.asList("geo", "political", "diplomacy", "reachable-lands",
			"culture", "population", "government-influence", "loyalty", "loyalty-dangerous", "states", "wealth",
			"soil-fertility", "soil-natural-fertility", "soil-area", "infrastructure", "science.agriculture",
			"science.medicine", "science.administration", "geo", "geo", "geo");
	private List<String> activeButtonModes;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	private void saveSettings() {
		applicationSettings.setMapButtonCodes(activeButtonModes);
		applicationSettings.savePropertiesToFile();
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		buttonsMapModes = new ArrayList<>();
		HBox row1 = new HBox();
		HBox row2 = new HBox();

		fillPossibleModes();
		activeButtonModes = new ArrayList<>(applicationSettings.getMapButtonCodes());
		Iterator<String> iter = activeButtonModes.iterator();
		while (iter.hasNext()) {
			if (!possibleModes.containsKey(iter.next())) {
				iter.remove();
			}
		}
		while (activeButtonModes.size() < availableDefaultModes.size() && activeButtonModes.size() < MAX_MODE_BUTTONS) {
			activeButtonModes.add(availableDefaultModes.get(activeButtonModes.size()));
		}
		while (activeButtonModes.size() > availableDefaultModes.size()) {
			activeButtonModes.remove(activeButtonModes.size() - 1);
		}
		for (int i = 0; i < activeButtonModes.size(); i++) {
			Button button = createModeButton(activeButtonModes, i);
			buttonsMapModes.add(button);
			if (i < 10) {
				row1.getChildren().add(button);
			} else {
				row2.getChildren().add(button);
			}
		}
		VBox buttonPane = new VBox();
		buttonPane.getChildren().addAll(row1, row2);
		setCenter(buttonPane);
	}

	private Button createModeButton(List<String> listOfActiveModes, int idx) {
		String modeCode = listOfActiveModes.get(idx);
		MapMode modeInfo = possibleModes.get(modeCode);
		Button button = new Button(modeInfo.getLabel());
		button.setTooltip(new Tooltip(modeInfo.getToolTip() + getMessage("toolbar.map.mode.button.tooltip")));
		button.setPrefWidth(100);
		button.setMaxWidth(100);
		button.setOnAction(e -> {
			MapMode selectedModeInfo = possibleModes.get(listOfActiveModes.get(idx));
			gameScene.setMapModeAndRedraw(selectedModeInfo);
			buttonsMapModes.forEach(b -> b.setEffect(null));
			button.setEffect(new Lighting());
		});
		button.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				showModeContextMenu(button, e, listOfActiveModes, idx);
			} else if (e.isControlDown()) {
				MapMode selectedModeInfo = possibleModes.get(listOfActiveModes.get(idx));
				gameScene.openNewMapForMode(selectedModeInfo, selectedModeInfo.getToolTip());
			}
		});
		return button;
	}

	private void fillPossibleModes() {
		possibleModes = new HashMap<>();
		contextMenuModes = new ArrayList<>();
		addMapMode("geo", MapModeEnum.GEO);
		addMapMode("political", MapModeEnum.POLITICAL);
		addMapMode("diplomacy", MapModeEnum.DIPLOMACY);
		addMapMode("reachable-lands", MapModeEnum.REACHABLE_LANDS);
		addMapMode("culture", MapModeEnum.CULTURE);
		addMapMode("population", MapModeEnum.POPULATION);
		addMapMode("government-influence", MapModeEnum.GOVERNMENT_INFLUENCE);
		addMapMode("loyalty", MapModeEnum.LOYALTY);
		addMapMode("loyalty-dangerous", MapModeEnum.LOYALTY_DANGEROUS);
		addMapMode("states", MapModeEnum.STATES);
		addMapMode("wealth", MapModeEnum.WEALTH);
		addMapMode("infrastructure", MapModeEnum.INFRASTRUCTURE);
		addMapMode("soil-area", MapModeEnum.SOIL_AREA);
		addMapMode("soil-fertility", MapModeEnum.SOIL_FERTILITY);
		addMapMode("soil-natural-fertility", MapModeEnum.SOIL_NATURAL_FERTILITY);
		contextMenuModes.add(null);
		addMapMode("science.agriculture", MapModeEnum.SCIENCE_AGRICULTURE);
		addMapMode("science.medicine", MapModeEnum.SCIENCE_MEDICINE);
		addMapMode("science.administration", MapModeEnum.SCIENCE_ADMINISTRATION);
		contextMenuModes.add(null);
		Map<String, List<String>> availableEventsModes = gameEventHandler
				.getAvailableEventsMapModes(gameScene.getGame());
		availableEventsModes.entrySet().forEach(entry -> addEventMapMode(entry.getKey(), entry.getValue()));
	}

	private void addEventMapMode(String key, List<String> value) {
		MapMode buttonMapMode = new MapMode(this, key, MapModeEnum.EVENT, value.get(0), value.get(1));
		possibleModes.put(key, buttonMapMode);
		contextMenuModes.add(key);
	}

	private void addMapMode(String modeCode, MapModeEnum mapMode) {
		MapMode buttonMapMode = new MapMode(this, modeCode, mapMode,
				getMessage("toolbar.map.mode." + modeCode + ".button.text"),
				getMessage("toolbar.map.mode." + modeCode + ".button.tooltip"));
		possibleModes.put(modeCode, buttonMapMode);
		contextMenuModes.add(modeCode);
	}

	private void showModeContextMenu(Button button, MouseEvent e, List<String> listOfActiveModes, int idx) {
		MapMode selectedModeInfo = possibleModes.get(listOfActiveModes.get(idx));
		ContextMenu cm = new ContextMenu();
		for (String modeCode : contextMenuModes) {
			if (modeCode != null) {
				MapMode modeInfo = possibleModes.get(modeCode);
				CheckMenuItem menuItem = new CheckMenuItem(modeInfo.getLabel());
				menuItem.setSelected(modeInfo.getCode().equals(selectedModeInfo.getCode()));
				cm.getItems().add(menuItem);
				menuItem.setOnAction(event -> {
					listOfActiveModes.set(idx, modeInfo.getCode());
					button.setText(modeInfo.getLabel());
					button.setTooltip(
							new Tooltip(modeInfo.getToolTip() + getMessage("toolbar.map.mode.button.tooltip")));
					saveSettings();
				});
			} else {
				cm.getItems().add(new SeparatorMenuItem());
			}
		}
		cm.show(button, e.getScreenX(), e.getScreenY());
	}

}
