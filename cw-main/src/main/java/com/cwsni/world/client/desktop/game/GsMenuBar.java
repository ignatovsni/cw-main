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
		Menu fileMenu = new Menu(getMessage("menu.file"));

		MenuItem exitMenuItem = new MenuItem(getMessage("menu.exit"));
		exitMenuItem.setOnAction(event -> gameScene.exitApp());

		MenuItem quickSaveMenuItem = new MenuItem(getMessage("menu.quick-save"));
		quickSaveMenuItem.setOnAction(event -> gameScene.quickSaveGame());
		quickSaveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));

		MenuItem quickLoadMenuItem = new MenuItem(getMessage("menu.quick-load"));
		quickLoadMenuItem.setOnAction(event -> gameScene.quickLoadGame());
		quickLoadMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F9));

		fileMenu.getItems().setAll(exitMenuItem, quickSaveMenuItem, quickLoadMenuItem);

		getMenus().setAll(fileMenu);
	}

}
