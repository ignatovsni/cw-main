package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.GlobalLocaleMessageSource;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.services.GameGeneralController;
import com.cwsni.world.settings.ApplicationSettings;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

@Component
@Scope("prototype")
public class GsMenuBar extends MenuBar {

	private static final Log logger = LogFactory.getLog(GsMenuBar.class);

	private GameScene gameScene;

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private ApplicationSettings applicationSettings;

	@Autowired
	private GameGeneralController gameGeneralController;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Menu fileMenu = createFileMenu(gameScene);
		Menu worldMenu = createWorldMenu(gameScene);
		Menu mapMenu = createMapMenu(gameScene);
		Menu toolsMenu = createToolsMenu(gameScene);
		Menu settingsMenu = createSettingsMenu(gameScene);
		getMenus().setAll(fileMenu, worldMenu, mapMenu, toolsMenu, settingsMenu);
	}

	private Menu createFileMenu(GameScene gameScene) {
		Menu menu = new Menu(getMessage("menu.file"));

		MenuItem createGameMenuItem = new MenuItem(getMessage("menu.file.game.create"));
		createGameMenuItem.setOnAction(event -> gameScene.createNewGame());
		createGameMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));

		MenuItem createTestGameMenuItem = new MenuItem(getMessage("menu.file.game.create.test"));
		createTestGameMenuItem.setOnAction(event -> gameScene.createTestGame());
		createTestGameMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));

		MenuItem saveMenuItem = new MenuItem(getMessage("menu.file.save"));
		saveMenuItem.setOnAction(event -> gameScene.saveGame());
		saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

		MenuItem loadMenuItem = new MenuItem(getMessage("menu.file.load"));
		loadMenuItem.setOnAction(event -> gameScene.loadGame());
		loadMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));

		MenuItem quickSaveMenuItem = new MenuItem(getMessage("menu.file.quick-save"));
		quickSaveMenuItem.setOnAction(event -> gameScene.quickSaveGame());
		quickSaveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));

		MenuItem quickLoadMenuItem = new MenuItem(getMessage("menu.file.quick-load"));
		quickLoadMenuItem.setOnAction(event -> gameScene.quickLoadGame());
		quickLoadMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F9));

		MenuItem exitMenuItem = new MenuItem(getMessage("menu.file.exit"));
		exitMenuItem.setOnAction(event -> gameScene.exitApp());

		menu.getItems().setAll(createGameMenuItem, createTestGameMenuItem, new SeparatorMenuItem(), loadMenuItem,
				saveMenuItem, quickLoadMenuItem, quickSaveMenuItem, new SeparatorMenuItem(), exitMenuItem);
		return menu;
	}

	private Menu createWorldMenu(GameScene gameScene) {
		Menu menu = new Menu(getMessage("menu.world"));

		MenuItem countriesMenuItem = new MenuItem(getMessage("menu.world.countries"));
		countriesMenuItem.setOnAction(event -> gameScene.editCountriesSettings());

		menu.getItems().setAll(countriesMenuItem);
		return menu;
	}

	private Menu createMapMenu(GameScene gameScene) {
		Menu menu = new Menu(getMessage("menu.map"));

		MenuItem searchOnMapMenuItem = new MenuItem(getMessage("menu.map.search-on-map"));
		searchOnMapMenuItem.setOnAction(event -> gameScene.searchOnMap());
		searchOnMapMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));

		MenuItem scaleToDefaultMenuItem = new MenuItem(getMessage("menu.map.scale-to-default"));
		scaleToDefaultMenuItem.setOnAction(event -> gameScene.scaleMapToDefault());
		scaleToDefaultMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.CONTROL_DOWN));

		MenuItem scaleToFitMenuItem = new MenuItem(getMessage("menu.map.scale-to-fit"));
		scaleToFitMenuItem.setOnAction(event -> gameScene.scaleMapToFitAllContent());
		scaleToFitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.CONTROL_DOWN));

		menu.getItems().setAll(searchOnMapMenuItem, new SeparatorMenuItem(), scaleToDefaultMenuItem,
				scaleToFitMenuItem);
		return menu;
	}

	private Menu createToolsMenu(GameScene gameScene) {
		Menu menu = new Menu(getMessage("menu.tools"));

		MenuItem scriptConsoleMenuItem = new MenuItem(getMessage("menu.tools.script-console"));
		scriptConsoleMenuItem.setOnAction(event -> gameScene.runGroovyConsole());
		scriptConsoleMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));

		MenuItem cacheResetMenuItem = new MenuItem(getMessage("menu.tools.app-caches-reset"));
		cacheResetMenuItem.setOnAction(event -> gameGeneralController.resetAppCaches());
		cacheResetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));

		menu.getItems().setAll(scriptConsoleMenuItem, new SeparatorMenuItem(), cacheResetMenuItem);
		return menu;
	}

	private Menu createSettingsMenu(GameScene gameScene) {
		Menu menu = new Menu(getMessage("menu.settings"));

		MenuItem settingsEditorMenuItem = new MenuItem(getMessage("menu.settings.application"));
		settingsEditorMenuItem.setOnAction(event -> gameScene.editSettings());

		List<MenuItem> defaultMenuItems = new ArrayList<>();
		defaultMenuItems.add(settingsEditorMenuItem);
		defaultMenuItems.add(new SeparatorMenuItem());
		menu.getItems().addAll(defaultMenuItems);

		menu.setOnShowing(e -> {
			menu.getItems().clear();
			menu.getItems().addAll(defaultMenuItems);
			GlobalLocaleMessageSource languageService = (GlobalLocaleMessageSource) messageSource;
			Map<String, String> languages = languageService.getAvailableLanguages();
			languages.entrySet().forEach(entry -> {
				CheckMenuItem newLanguageMenuItem = new CheckMenuItem(entry.getValue());
				newLanguageMenuItem.setSelected(entry.getKey().equals(applicationSettings.getLanguage()));
				newLanguageMenuItem.setOnAction(event -> setLanguage(entry.getKey(), entry.getValue()));
				menu.getItems().add(newLanguageMenuItem);
			});
		});

		return menu;
	}

	private void setLanguage(String code, String label) {
		if (applicationSettings.getLanguage().equals(code)) {
			return;
		}
		logger.info("change language to [code=" + code + ", label=" + label + "]");
		applicationSettings.setLanguage(code);
		applicationSettings.savePropertiesToFile();
		gameScene.refreshAllForLanguageChange();
	}

}
