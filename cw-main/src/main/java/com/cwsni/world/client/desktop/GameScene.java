package com.cwsni.world.client.desktop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.map.DWorldMap;
import com.cwsni.world.model.WorldMap;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Sergei Ignatov
 * Copyright (c) 2018, Sergei Ignatov. All rights reserved.
 *
 */
@Component
public class GameScene {
	
	@Autowired
	private LocaleMessageSource messageSource; 
	
	public Scene createScene(Stage stage) {
		DWorldMap worldMap = createTestMap(30, 30, 30);
		
		Parent zoomPane = new ZoomableScrollPane(worldMap.getMapGroup());		

		VBox layout = new VBox();
		layout.getChildren().setAll(createMenuBar(stage, worldMap.getMapGroup()), zoomPane);

		VBox.setVgrow(zoomPane, Priority.ALWAYS);

		Scene scene = new Scene(layout);
		return scene;
	}
	
	private DWorldMap createTestMap(int rows, int columns, int provinceRadius) {
		WorldMap map = WorldMap.createMap(rows, columns, provinceRadius);
		DWorldMap dMap = DWorldMap.createDMap(map, provinceRadius); 
		return dMap;
	}


	private MenuBar createMenuBar(final Stage stage, final Group group) {
		Menu fileMenu = new Menu(getMessage("menu.file"));
		MenuItem exitMenuItem = new MenuItem(getMessage("menu.exit"));
		exitMenuItem.setOnAction( event -> stage.close());
		fileMenu.getItems().setAll(exitMenuItem);
		
		Menu zoomMenu = new Menu("_Zoom");
		MenuItem zoomResetMenuItem = new MenuItem("Zoom _Reset");
		zoomResetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
		zoomResetMenuItem.setOnAction( event -> {
				group.setScaleX(1);
				group.setScaleY(1);
		});
		zoomMenu.getItems().setAll(zoomResetMenuItem);
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().setAll(fileMenu, zoomMenu);
		return menuBar;
	}
	
	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

}
