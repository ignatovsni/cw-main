package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.map.DWorldMap;
import com.cwsni.world.client.desktop.game.map.MapMode;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.ZoomableScrollPane;
import com.cwsni.world.common.GameGenerator;
import com.cwsni.world.common.GameRepository;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Lighting;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

@Component
@Scope("prototype")
public class GameScene extends Scene {

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private GameRepository gameRepository;

	@Autowired
	private GameGenerator gameGenerator;

	private Stage stage;
	private ZoomableScrollPane mapPane;
	private Text statusBarText;

	private Game game;
	private DWorldMap worldMap;
	private Province selectedProvince;

	private List<Button> toolBarMapModes;
	private MapMode mapMode = MapMode.GEO;

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
		VBox hbox = new VBox();
		hbox.getChildren().addAll(createMenuBar(), createToolBar());
		layout.setTop(hbox);
		layout.setBottom(createStatusBar());
		layout.setCenter(mapPane);
		createTestGame();
	}

	private ToolBar createToolBar() {
		Button toolBarMapGeoMode = new Button(getMessage("toolbar.map.mode.geo.button.text"));
		toolBarMapGeoMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.geo.button.tooltip")));
		toolBarMapGeoMode.setOnAction(e -> {
			enableMapModeButton(toolBarMapGeoMode, MapMode.GEO);
		});
		toolBarMapGeoMode.setEffect(new Lighting());

		Button toolBarMapPopsMode = new Button(getMessage("toolbar.map.mode.population.button.text"));
		toolBarMapPopsMode.setTooltip(new Tooltip(getMessage("toolbar.map.mode.population.button.tooltip")));
		toolBarMapPopsMode.setOnAction(e -> {
			enableMapModeButton(toolBarMapPopsMode, MapMode.POPULATION);
		});

		toolBarMapModes = new ArrayList<>();
		toolBarMapModes.add(toolBarMapGeoMode);
		toolBarMapModes.add(toolBarMapPopsMode);
		ToolBar tbar = new ToolBar();
		tbar.getItems().addAll(toolBarMapModes);
		return tbar;
	}

	private void enableMapModeButton(Button buttonMode, MapMode mapMode) {
		worldMap.setMapModeAndRedraw(mapMode);
		this.mapMode = mapMode; 
		toolBarMapModes.forEach(b -> b.setEffect(null));
		buttonMode.setEffect(new Lighting());
	}

	private MenuBar createMenuBar() {
		Menu fileMenu = new Menu(getMessage("menu.file"));

		MenuItem exitMenuItem = new MenuItem(getMessage("menu.exit"));
		exitMenuItem.setOnAction(event -> stage.close());

		MenuItem quickSaveMenuItem = new MenuItem(getMessage("menu.quick-save"));
		quickSaveMenuItem.setOnAction(event -> quickSaveGame());
		quickSaveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));

		MenuItem quickLoadMenuItem = new MenuItem(getMessage("menu.quick-load"));
		quickLoadMenuItem.setOnAction(event -> quickLoadGame());
		quickLoadMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F9));

		fileMenu.getItems().setAll(exitMenuItem, quickSaveMenuItem, quickLoadMenuItem);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().setAll(fileMenu);
		return menuBar;
	}

	private void quickSaveGame() {
		if (game != null) {
			gameRepository.quickSaveGame(game);
		}
	}

	private void quickLoadGame() {
		Game newGame = gameRepository.quickLoadGame();
		if (newGame != null && newGame.isCorrect()) {
			if (game != null) {
				game.destroy();
			}
			setupGame(newGame);			
		}
	}

	private void setupGame(Game newGame) {
		DWorldMap worldMap = DWorldMap.createDMap(newGame, mapMode);
		mapPane.setTarget(worldMap.getMapGroup());
		this.game = worldMap.getGame();
		this.worldMap = worldMap;
		worldMap.setGameScene(this);
	}

	private Pane createStatusBar() {
		Pane statusBar = new HBox();
		statusBarText = new Text("");
		statusBar.getChildren().add(statusBarText);
		return statusBar;
	}

	private void createTestGame() {
		Game game = gameGenerator.createTestGame(20, 20, 30);
		setupGame(game);
	}

	// Event listener can be a good idea instead of direct invocation
	public void selectProvince(Province province) {
		this.selectedProvince = province;
		if (selectedProvince != null) {
			statusBarText.setText("Selected province with id = " + selectedProvince.getId()
					+ "; population = " + selectedProvince.getPopulationAmount());
		}
	}

}
