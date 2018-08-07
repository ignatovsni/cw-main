package com.cwsni.world.client.desktop.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

@Component
@Scope("prototype")
public class GsMenuBar extends MenuBar {

	@Autowired
	private LocaleMessageSource messageSource;

	private GameScene gameScene;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Menu fileMenu = createFileMenu(gameScene);
		Menu worldMenu = createWorldMenu(gameScene);
		getMenus().setAll(fileMenu, worldMenu);
	}

	private Menu createFileMenu(GameScene gameScene) {
		Menu fileMenu = new Menu(getMessage("menu.file"));

		MenuItem exitMenuItem = new MenuItem(getMessage("menu.file.exit"));
		exitMenuItem.setOnAction(event -> gameScene.exitApp());

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

		MenuItem createTestGameMenuItem = new MenuItem(getMessage("menu.file.game.create.test"));
		createTestGameMenuItem.setOnAction(event -> gameScene.createTestGame());
		createTestGameMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));

		MenuItem createGameMenuItem = new MenuItem(getMessage("menu.file.game.create"));
		createGameMenuItem.setOnAction(event -> gameScene.createNewGame());
		createGameMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));

		fileMenu.getItems().setAll(createGameMenuItem, createTestGameMenuItem, loadMenuItem, saveMenuItem,
				quickLoadMenuItem, quickSaveMenuItem, exitMenuItem);
		return fileMenu;
	}

	private Menu createWorldMenu(GameScene gameScene) {
		Menu worldMenu = new Menu(getMessage("menu.world"));

		MenuItem countriesMenuItem = new MenuItem(getMessage("menu.world.countries"));
		countriesMenuItem.setOnAction(event -> gameScene.editCountriesSettings());

		worldMenu.getItems().setAll(countriesMenuItem);
		return worldMenu;
	}

}
