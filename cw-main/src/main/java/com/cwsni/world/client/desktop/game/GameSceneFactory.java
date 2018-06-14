package com.cwsni.world.client.desktop.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.ZoomableScrollPane;

import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Sergei Ignatov
 * Copyright (c) 2018, Sergei Ignatov. All rights reserved.
 *
 */
@Component
public class GameSceneFactory {
	
	@Autowired
	private LocaleMessageSource messageSource; 
	
	public Scene createScene(Stage stage) {
		ZoomableScrollPane zoomPane = new ZoomableScrollPane();		
		BorderPane layout = new BorderPane();
		layout.setTop(createMenuBar(stage));
		layout.setCenter(zoomPane);
		GameScene scene = new GameScene(layout, zoomPane);
		scene.createTestMap();
		return scene;
	}

	private MenuBar createMenuBar(final Stage stage) {
		Menu fileMenu = new Menu(getMessage("menu.file"));
		MenuItem exitMenuItem = new MenuItem(getMessage("menu.exit"));
		exitMenuItem.setOnAction( event -> stage.close());
		fileMenu.getItems().setAll(exitMenuItem);
		
		/*
		Menu zoomMenu = new Menu("_Zoom");
		MenuItem zoomResetMenuItem = new MenuItem("Zoom _Reset");
		zoomResetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
		zoomResetMenuItem.setOnAction( event -> {
				group.setScaleX(1);
				group.setScaleY(1);
		});
		zoomMenu.getItems().setAll(zoomResetMenuItem);
		menuBar.getMenus().setAll(fileMenu, zoomMenu);
		*/
		
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().setAll(fileMenu);
		return menuBar;
	}
	
	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

}
