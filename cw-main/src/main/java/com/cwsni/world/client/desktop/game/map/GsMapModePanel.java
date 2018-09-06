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

	private class ButtonMapMode {
		String code;
		MapMode mapMode;
		String label;
		String toolTip;

		public ButtonMapMode(String code, MapMode mapMode, String label, String tooltip) {
			this.code = code;
			this.mapMode = mapMode;
			this.label = label;
			toolTip = tooltip;
		}
	}

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private ApplicationSettings applicationSettings;

	private GameScene gameScene;

	private Map<String, ButtonMapMode> mainPossibleModes;
	private List<String> contextMenuModes;
	private List<String> defaultModes = Arrays.asList("geo", "political", "diplomacy", "reachable-lands", "culture",
			"population", "government-influence", "loyalty", "wealth", "soil", "science.agriculture",
			"science.medicine", "science.administration");
	private List<String> activeButtonModes;
	private List<Button> buttonsMapModes;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		fillMainPossibleModes();

		activeButtonModes = new ArrayList<>(applicationSettings.getMapButtonCodes());
		Iterator<String> iter = activeButtonModes.iterator();
		while (iter.hasNext()) {
			if (!mainPossibleModes.containsKey(iter.next())) {
				iter.remove();
			}
		}
		while (activeButtonModes.size() < defaultModes.size()) {
			activeButtonModes.add(defaultModes.get(activeButtonModes.size()));
		}
		while (activeButtonModes.size() > defaultModes.size()) {
			activeButtonModes.remove(activeButtonModes.size() - 1);
		}

		buttonsMapModes = new ArrayList<>();
		VBox buttonPane = new VBox();
		HBox row1 = new HBox();
		HBox row2 = new HBox();
		for (int i = 0; i < activeButtonModes.size(); i++) {
			Button button = createButton(activeButtonModes, i);
			buttonsMapModes.add(button);
			if (i < 8) {
				row1.getChildren().add(button);
			} else {
				row2.getChildren().add(button);
			}
		}
		buttonPane.getChildren().addAll(row1, row2);
		setCenter(buttonPane);
	}

	private Button createButton(List<String> listOfActiveModes, int idx) {
		String modeCode = listOfActiveModes.get(idx);
		ButtonMapMode modeInfo = mainPossibleModes.get(modeCode);
		Button button = new Button(modeInfo.label);
		button.setTooltip(new Tooltip(modeInfo.toolTip + getMessage("toolbar.map.mode.button.tooltip")));
		button.setPrefWidth(100);
		button.setMaxWidth(100);
		button.setOnAction(e -> {
			ButtonMapMode selectedModeInfo = mainPossibleModes.get(listOfActiveModes.get(idx));
			gameScene.setMapModeAndRedraw(selectedModeInfo.mapMode);
			buttonsMapModes.forEach(b -> b.setEffect(null));
			button.setEffect(new Lighting());
		});
		button.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				showContextMenu(button, e, listOfActiveModes, idx);
			} else if (e.isControlDown()) {
				ButtonMapMode selectedModeInfo = mainPossibleModes.get(listOfActiveModes.get(idx));
				gameScene.openNewMapForMode(selectedModeInfo.mapMode, selectedModeInfo.toolTip);
			}
		});
		return button;
	}

	private void fillMainPossibleModes() {
		mainPossibleModes = new HashMap<>();
		contextMenuModes = new ArrayList<>();
		addMapMode("geo", MapMode.GEO);
		addMapMode("political", MapMode.POLITICAL);
		addMapMode("diplomacy", MapMode.DIPLOMACY);
		addMapMode("reachable-lands", MapMode.REACHABLE_LANDS);
		addMapMode("culture", MapMode.CULTURE);
		addMapMode("population", MapMode.POPULATION);
		addMapMode("government-influence", MapMode.GOVERNMENT_INFLUENCE);
		addMapMode("loyalty", MapMode.LOYALTY);
		addMapMode("states", MapMode.STATES);
		addMapMode("wealth", MapMode.WEALTH);
		addMapMode("infrastructure", MapMode.INFRASTRUCTURE);
		addMapMode("soil", MapMode.SOIL_FERTILITY);
		addMapMode("soil-raw", MapMode.SOIL_RAW_FERTILITY);
		contextMenuModes.add(null);
		addMapMode("science.agriculture", MapMode.SCIENCE_AGRICULTURE);
		addMapMode("science.medicine", MapMode.SCIENCE_MEDICINE);
		addMapMode("science.administration", MapMode.SCIENCE_ADMINISTRATION);
	}

	private void addMapMode(String modeCode, MapMode mapMode) {
		ButtonMapMode buttonMapMode = new ButtonMapMode(modeCode, mapMode,
				getMessage("toolbar.map.mode." + modeCode + ".button.text"),
				getMessage("toolbar.map.mode." + modeCode + ".button.tooltip"));
		mainPossibleModes.put(modeCode, buttonMapMode);
		contextMenuModes.add(modeCode);
	}

	private void showContextMenu(Button button, MouseEvent e, List<String> listOfActiveModes, int idx) {
		ButtonMapMode selectedModeInfo = mainPossibleModes.get(listOfActiveModes.get(idx));
		ContextMenu cm = new ContextMenu();
		for (String modeCode : contextMenuModes) {
			if (modeCode != null) {
				ButtonMapMode modeInfo = mainPossibleModes.get(modeCode);
				CheckMenuItem menuItem = new CheckMenuItem(modeInfo.label);
				menuItem.setSelected(modeInfo.code.equals(selectedModeInfo.code));
				cm.getItems().add(menuItem);
				menuItem.setOnAction(event -> {
					listOfActiveModes.set(idx, modeInfo.code);
					button.setText(modeInfo.label);
					button.setTooltip(new Tooltip(modeInfo.toolTip + getMessage("toolbar.map.mode.button.tooltip")));
					saveSettings();
				});
			} else {
				cm.getItems().add(new SeparatorMenuItem());
			}
		}
		cm.show(button, e.getScreenX(), e.getScreenY());
	}

	private void saveSettings() {
		applicationSettings.setMapButtonCodes(activeButtonModes);
		applicationSettings.savePropertiesToFile();
	}

}
