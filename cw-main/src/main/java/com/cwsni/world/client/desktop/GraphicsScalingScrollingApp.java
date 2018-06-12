package com.cwsni.world.client.desktop;

import com.cwsni.world.client.desktop.map.DWorldMap;
import com.cwsni.world.model.WorldMap;

import javafx.application.Application;
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

public class GraphicsScalingScrollingApp extends Application {

	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage stage) {		
		DWorldMap worldMap = createTestMap(30, 30, 30);
		
		//Parent zoomPane = new ZoomPane(worldMap.getMapGroup()).createZoomPane();
		Parent zoomPane = new ZoomableScrollPane(worldMap.getMapGroup());		

		VBox layout = new VBox();
		layout.getChildren().setAll(createMenuBar(stage, worldMap.getMapGroup()), zoomPane);

		VBox.setVgrow(zoomPane, Priority.ALWAYS);

		Scene scene = new Scene(layout);

		stage.setTitle("Zoomy");
		stage.setScene(scene);
		stage.setHeight(400);
		stage.setWidth(400);
		
		stage.show();
	}
	
	private DWorldMap createTestMap(int rows, int columns, int provinceRadius) {
		WorldMap map = WorldMap.createMap(rows, columns, provinceRadius);
		DWorldMap dMap = DWorldMap.createDMap(map, provinceRadius); 
		return dMap;
	}


	private MenuBar createMenuBar(final Stage stage, final Group group) {
		Menu fileMenu = new Menu("_File");
		MenuItem exitMenuItem = new MenuItem("E_xit");
		exitMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stage.close();
			}
		});
		fileMenu.getItems().setAll(exitMenuItem);
		Menu zoomMenu = new Menu("_Zoom");
		MenuItem zoomResetMenuItem = new MenuItem("Zoom _Reset");
		zoomResetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
		zoomResetMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				group.setScaleX(1);
				group.setScaleY(1);
			}
		});
		MenuItem zoomInMenuItem = new MenuItem("Zoom _In");
		zoomInMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I));
		zoomInMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				group.setScaleX(group.getScaleX() * 1.5);
				group.setScaleY(group.getScaleY() * 1.5);
			}
		});
		MenuItem zoomOutMenuItem = new MenuItem("Zoom _Out");
		zoomOutMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O));
		zoomOutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				group.setScaleX(group.getScaleX() * 1 / 1.5);
				group.setScaleY(group.getScaleY() * 1 / 1.5);
			}
		});
		zoomMenu.getItems().setAll(zoomResetMenuItem, zoomInMenuItem, zoomOutMenuItem);
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().setAll(fileMenu, zoomMenu);
		return menuBar;
	}

}