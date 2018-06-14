package com.cwsni.world.client.desktop.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.map.DWorldMap;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.ZoomableScrollPane;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.WorldMap;

import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

@Component
@Scope("prototype")
public class GameScene extends Scene {

	@Autowired
	private LocaleMessageSource messageSource;

	private Stage stage;
	private ZoomableScrollPane mapPane;
	private Text statusBarText;

	private Province selectedProvince;

	public GameScene() {
		super(new BorderPane());
	}

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void initialize() {
		mapPane = new ZoomableScrollPane();
		BorderPane layout = (BorderPane) getRoot();
		layout.setTop(createMenuBar(stage));		
		layout.setBottom(createStatusBar());
		layout.setCenter(mapPane);
		createTestMap();
	}

	private MenuBar createMenuBar(final Stage stage) {
		Menu fileMenu = new Menu(getMessage("menu.file"));
		MenuItem exitMenuItem = new MenuItem(getMessage("menu.exit"));
		exitMenuItem.setOnAction(event -> stage.close());
		fileMenu.getItems().setAll(exitMenuItem);
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().setAll(fileMenu);
		return menuBar;
	}
	
	private Pane createStatusBar() {
		Pane statusBar = new HBox();
		statusBarText = new Text("");
		statusBar.getChildren().add(statusBarText);
		return statusBar;
	}


	private void createTestMap() {
		DWorldMap worldMap = createTestMap(30, 30, 30);
		mapPane.setTarget(worldMap.getMapGroup());
		worldMap.setGameScene(this);
	}

	private DWorldMap createTestMap(int rows, int columns, int provinceRadius) {
		WorldMap map = WorldMap.createMap(rows, columns, provinceRadius);
		DWorldMap dMap = DWorldMap.createDMap(map, provinceRadius);
		return dMap;
	}

	public void selectProvince(Province province) {
		this.selectedProvince = province;
		if (selectedProvince != null) {
			statusBarText.setText("Selected province with id = " + selectedProvince.getId());
		}
	}
	
}
