package com.cwsni.world.client.desktop.game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.UserPreferences;
import com.cwsni.world.client.desktop.game.CountriesPropertiesWindow.RowCountry;
import com.cwsni.world.client.desktop.game.map.DWorldMap;
import com.cwsni.world.client.desktop.game.map.MapMode;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.AlertWithStackTraceFactory;
import com.cwsni.world.client.desktop.util.ZoomableScrollPane;
import com.cwsni.world.game.ai.ScriptAIHandler;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;
import com.cwsni.world.services.GameGenerator;
import com.cwsni.world.services.GameHandler;
import com.cwsni.world.services.GameRepository;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

@Component
@Scope("prototype")
public class GameScene extends Scene {

	private static final Log logger = LogFactory.getLog(GameScene.class);

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private GameRepository gameRepository;

	@Autowired
	private GameGenerator gameGenerator;

	@Autowired
	private GsMapToolBar mapToolBar;

	@Autowired
	private GsMenuBar menuBar;

	@Autowired
	private GsGlobalInfoPane globalInfoPane;

	@Autowired
	private GsCountryInfoPane countryInfoPane;

	@Autowired
	private GsProvInfoPane provInfoPane;

	@Autowired
	private GsProvScienceInfoPane provScienceInfoPane;

	@Autowired
	private GsProvArmiesInfoPane provArmiesInfoPane;

	@Autowired
	private GsProvEventsInfoPane provEventsInfoPane;

	@Autowired
	private GsTimeControl timeControl;

	@Autowired
	private GameHandler gameHadler;

	@Autowired
	private CreateGameWindow createGameWindow;

	@Autowired
	private CountriesPropertiesWindow countriesPropertiesWindow;

	@Autowired
	private ScriptAIHandler scriptAIHandler;

	private ZoomableScrollPane mapPane;
	private Text statusBarText;

	private Game game;
	private DWorldMap worldMap;
	private Integer selectedProvinceId;

	private MapMode mapMode = MapMode.GEO;
	private GsTimeMode timeMode = GsTimeMode.PAUSE;
	private boolean autoTurn = true;
	private boolean pauseBetweenTurn = true;

	private Map<MapMode, Stage> otherMapWindows;
	private Map<MapMode, DWorldMap> otherMaps;

	private Object lockObj = new Object();

	public GameScene() {
		super(new BorderPane());
	}

	public void init() {
		mapPane = new ZoomableScrollPane();
		mapToolBar.init(this);
		menuBar.init(this);
		globalInfoPane.init(this);
		provInfoPane.init(this);
		provScienceInfoPane.init(this);
		provArmiesInfoPane.init(this);
		countryInfoPane.init(this);
		provEventsInfoPane.init(this);
		timeControl.init(this);
		createGameWindow.init(this);
		countriesPropertiesWindow.init(this);

		VBox rightInfoPanes = new VBox();
		rightInfoPanes.getChildren().addAll(countryInfoPane, provInfoPane, provScienceInfoPane, provArmiesInfoPane,
				provEventsInfoPane);
		rightInfoPanes.setMinWidth(220);
		rightInfoPanes.setMaxWidth(220);
		ScrollPane scrollRightSection = new ScrollPane();
		scrollRightSection.setContent(rightInfoPanes);
		scrollRightSection.setMinWidth(240);
		scrollRightSection.setMaxWidth(240);

		VBox rightTopSection = new VBox();
		rightTopSection.getChildren().addAll(timeControl, globalInfoPane);
		rightTopSection.setMinWidth(220);
		rightTopSection.setMaxWidth(220);

		VBox rightSection = new VBox();
		rightSection.setMinWidth(220);
		rightSection.setMaxWidth(220);
		rightSection.getChildren().addAll(rightTopSection, scrollRightSection);

		VBox menuSection = new VBox();
		menuSection.getChildren().addAll(menuBar);

		BorderPane mapBlock = new BorderPane();
		mapBlock.setBottom(mapToolBar);
		mapBlock.setCenter(mapPane);

		BorderPane leftSection = new BorderPane();
		leftSection.setTop(menuSection);
		leftSection.setCenter(mapBlock);

		BorderPane layout = (BorderPane) getRoot();
		layout.setBottom(createStatusBar());
		layout.setRight(rightSection);
		layout.setCenter(leftSection);

		otherMapWindows = new HashMap<>();
		otherMaps = new HashMap<>();

		setupGame(gameGenerator.createEmptyGame());
	}

	public LocaleMessageSource getMessageSource() {
		return messageSource;
	}

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void quickSaveGame() {
		if (game == null) {
			return;
		}
		runLocked(() -> saveFile(true));
	}

	public void saveGame() {
		if (game == null) {
			return;
		}
		pauseGame();
		runLocked(() -> saveFile(false));
	}

	private void saveFile(boolean isQuickSave) {
		try {
			if (isQuickSave) {
				gameRepository.quickSaveGame(game);
			} else {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle(getMessage("menu.save.window-title"));
				fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Game files", "*.cw"));
				fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
				File file = fileChooser.showSaveDialog(getWindow());
				if (file != null) {
					gameRepository.saveGame(game, file);
				}
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR, getMessage("error.msg.save") + ": " + e.getMessage());
			alert.setTitle("");
			alert.showAndWait();
		}
	}

	public void quickLoadGame() {
		pauseGame();
		loadGame(true);
	}

	public void loadGame() {
		pauseGame();
		loadGame(false);
	}

	private void loadGame(boolean isQuickLoad) {
		Game newGame = null;
		try {
			if (isQuickLoad) {
				newGame = gameRepository.quickLoadGame();
			} else {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle(getMessage("menu.load.window-title"));
				fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Game files", "*.cw"));
				fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
				File file = fileChooser.showOpenDialog(getWindow());
				if (file != null) {
					newGame = gameRepository.loadGame(file);
				}
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("");
			alert.setHeaderText(getMessage("error.msg.load"));
			AlertWithStackTraceFactory.addStackTrace(alert, e);
			alert.showAndWait();
		}
		if (newGame != null) {
			setupGame(newGame);
		}
	}

	private void setupGame(Game newGame) {
		runLocked(() -> {
			DWorldMap worldMap = DWorldMap.createDMap(newGame, mapMode);
			mapPane.setTarget(worldMap.getMapGroup());
			this.game = worldMap.getGame();
			this.worldMap = worldMap;
			worldMap.setGameScene(this);
			closeOtherMaps();
			refreshAllVisibleInfoAndResetSelections();
		});
	}

	private void closeOtherMaps() {
		List<Stage> copyMaps = new ArrayList<>(otherMapWindows.values());
		copyMaps.forEach(stage -> stage.hide());
		otherMapWindows.clear();
		otherMaps.clear();
	}

	private Pane createStatusBar() {
		Pane statusBar = new HBox();
		statusBarText = new Text("");
		statusBar.getChildren().add(statusBarText);
		return statusBar;
	}

	public void createTestGame() {
		pauseGame();
		Game game = gameGenerator.createTestGame();
		setupGame(game);
		refreshAllVisibleInfo();
	}

	public void createNewGame() {
		pauseGame();
		createGameWindow.reinit();
		Optional<ButtonType> result = createGameWindow.showAndWait();
		if (result.isPresent() && result.get().getButtonData() == ButtonData.OK_DONE) {
			Game game = gameGenerator.createGame(createGameWindow.getGameParams());
			setupGame(game);
			refreshAllVisibleInfo();
		}
	}

	public void selectProvince(Province province) {
		runLocked(() -> {
			this.selectedProvinceId = province != null ? province.getId() : null;
			if (province != null) {
				worldMap.selectProvince(selectedProvinceId);
				otherMaps.values().forEach(map -> map.selectProvince(selectedProvinceId));
				statusBarText.setText("Selected province with id = " + selectedProvinceId);
			}
			refreshProvinceInfos();
		});
	}

	private void refreshProvinceInfos() {
		countryInfoPane.refreshInfo();
		provInfoPane.refreshInfo();
		provArmiesInfoPane.refreshInfo();
		provScienceInfoPane.refreshInfo();
		provEventsInfoPane.refreshInfo();
	}

	public DWorldMap getWorldMap() {
		return worldMap;
	}

	public void setMapMode(MapMode mapMode) {
		this.mapMode = mapMode;
	}

	public void exitApp() {
		Platform.exit();
		// stage.close();
	}

	public Game getGame() {
		return game;
	}

	public GsTimeMode getTimeMode() {
		return timeMode;
	}

	public void putHotKey(KeyCodeCombination keyCombination, Runnable runnable) {
		getAccelerators().put(keyCombination, runnable);
	}

	private void refreshAllVisibleInfoAndResetSelections() {
		selectedProvinceId = null;
		refreshAllVisibleInfo();
	}

	private void refreshAllVisibleInfo() {
		globalInfoPane.refreshInfo();
		refreshProvinceInfos();
	}

	public Province getSelectedProvince() {
		return game.getMap().findProvById(selectedProvinceId);
	}

	public void setTimeModeAndRun(GsTimeMode newMode) {
		runLocked(() -> {
			GsTimeMode oldMode = timeMode;
			this.timeMode = newMode;
			if (newMode != GsTimeMode.PAUSE && oldMode == GsTimeMode.PAUSE) {
				startProcessingNewTurn();
			}
		});
	}

	private void startProcessingNewTurn() {
		if (getGame().getMap().getProvinces().isEmpty()) {
			return;
		}
		if (timeMode == GsTimeMode.PAUSE) {
			return;
		}
		Task<Integer> task = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				runLocked(() -> gameHadler.processNewTurn(game, timeMode, autoTurn, pauseBetweenTurn));
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							refreshViewAndStartNewTurn();
						} catch (Exception e) {
							logError(e);
						}
					}
				});
				return null;
			}
		};
		Thread th = new Thread(task);
		th.setName("turn processing");
		th.start();
	}

	private void refreshViewAndStartNewTurn() {
		refreshViewsAndMaps();
		if (autoTurn) {
			startProcessingNewTurn();
		} else {
			pauseGame();
		}
	}

	private void refreshViewsAndMaps() {
		runLocked(() -> {
			refreshAllVisibleInfo();
			getWorldMap().setMapModeAndRedraw(mapMode);
			otherMaps.forEach((mode, map) -> map.setMapModeAndRedraw(mode));
		});
	}

	private void pauseGame() {
		timeControl.enablePauseButton();
	}

	private void logError(Exception e) {
		logger.error(e.getMessage(), e);
	}

	public void setAutoTurn(boolean autoTurn) {
		this.autoTurn = autoTurn;
	}

	public void setPauseBetweenTurn(boolean pauseBetweenTurn) {
		this.pauseBetweenTurn = pauseBetweenTurn;
	}

	public boolean isAutoTurn() {
		return autoTurn;
	}

	public boolean isPauseBetweenTurn() {
		return pauseBetweenTurn;
	}

	public void setMapModeAndRedraw(MapMode mapMode) {
		runLocked(() -> {
			this.mapMode = mapMode;
			worldMap.setMapModeAndRedraw(mapMode);
		});
	}

	private void runLocked(Runnable r) {
		synchronized (lockObj) {
			r.run();
		}
	}

	public void setUserPreferences(UserPreferences userPref) {
		userPref.setTimeControlAutoTurn(isAutoTurn());
		userPref.setTimeControlPauseBetweenTurns(isPauseBetweenTurn());
		userPref.setInfoPaneGlobalMinimized(globalInfoPane.isMinimazed());
		userPref.setInfoPaneCountryMinimized(countryInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceMinimized(provInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceArmiesMinimized(provArmiesInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceScienceMinimized(provScienceInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceEventsMinimized(provEventsInfoPane.isMinimazed());
	}

	public void applyUserPreferences(UserPreferences userPref) {
		timeControl.applyUserPreferences(userPref);
		globalInfoPane.setMinimized(userPref.isInfoPaneGlobalMinimized());
		countryInfoPane.setMinimized(userPref.isInfoPaneCountryMinimized());
		provInfoPane.setMinimized(userPref.isInfoPaneProvinceMinimized());
		provArmiesInfoPane.setMinimized(userPref.isInfoPaneProvinceArmiesMinimized());
		provScienceInfoPane.setMinimized(userPref.isInfoPaneProvinceScienceMinimized());
		provEventsInfoPane.setMinimized(userPref.isInfoPaneProvinceEventsMinimized());
	}

	public void openNewMapForMode(MapMode mapModeForNewWindow, String windowTitle) {
		Stage window = otherMapWindows.get(mapModeForNewWindow);
		if (window != null) {
			window.close();
			otherMapWindows.remove(mapModeForNewWindow);
			otherMaps.remove(mapModeForNewWindow);
		} else {
			if (game.getMap().getProvinces().size() == 0) {
				return;
			}
			window = new Stage();
			window.setTitle(windowTitle);
			Bounds boundsInLocal = worldMap.getMapGroup().getBoundsInLocal();
			double width = Math.min(boundsInLocal.getWidth(), 800);
			double height = width * boundsInLocal.getHeight() / boundsInLocal.getWidth() * 1.05;
			window.setWidth(width);
			window.setHeight(height);
			window.setOnCloseRequest(e -> {
				otherMapWindows.remove(mapModeForNewWindow);
				otherMaps.remove(mapModeForNewWindow);
			});

			BorderPane root = new BorderPane();
			Scene scene = new Scene(root);
			ZoomableScrollPane newMapPane = new ZoomableScrollPane();
			root.setCenter(newMapPane);

			DWorldMap newWorldMap = DWorldMap.createDMap(game, mapModeForNewWindow);
			newMapPane.setTarget(newWorldMap.getMapGroup());
			newWorldMap.setGameScene(this);

			otherMapWindows.put(mapModeForNewWindow, window);
			otherMaps.put(mapModeForNewWindow, newWorldMap);

			window.setScene(scene);
			window.show();
		}
	}

	public ScriptAIHandler getScriptAIHandler() {
		return scriptAIHandler;
	}

	private void editCountriesSettings(List<Country> list4edit) {
		pauseGame();
		runLocked(() -> {
			countriesPropertiesWindow.reinit(list4edit);
			Optional<ButtonType> result = countriesPropertiesWindow.showAndWait();
			if (result.isPresent() && result.get().getButtonData() == ButtonData.OK_DONE) {
				List<RowCountry> countriesRows = countriesPropertiesWindow.getCountriesInfo();
				for (RowCountry rc : countriesRows) {
					Country country = game.findCountryById(rc.id);
					country.getColor().setR(rc.color.getR());
					country.getColor().setG(rc.color.getG());
					country.getColor().setB(rc.color.getB());
					country.setName(rc.name);
					country.setAiScriptName(rc.script);
				}
			}
		});
		refreshViewsAndMaps();
	}

	public void editCountriesSettings() {
		editCountriesSettings(game.getCountries());
	}
	
	public void editCountriesSettings(Country country) {
		List<Country> list = new ArrayList<>();
		list.add(country);
		editCountriesSettings(list);
	}

}
