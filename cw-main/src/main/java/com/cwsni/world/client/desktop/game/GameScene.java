package com.cwsni.world.client.desktop.game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.MainWindow;
import com.cwsni.world.client.desktop.UserUIPreferences;
import com.cwsni.world.client.desktop.game.CountriesPropertiesWindow.RowCountry;
import com.cwsni.world.client.desktop.game.SearchOnMapWindow.SearchResult;
import com.cwsni.world.client.desktop.game.infopanels.GsCountryInfoPane;
import com.cwsni.world.client.desktop.game.infopanels.GsGlobalInfoPane;
import com.cwsni.world.client.desktop.game.infopanels.GsProvArmiesInfoPane;
import com.cwsni.world.client.desktop.game.infopanels.GsProvEventsInfoPane;
import com.cwsni.world.client.desktop.game.infopanels.GsProvInfoPane;
import com.cwsni.world.client.desktop.game.infopanels.GsProvScienceInfoPane;
import com.cwsni.world.client.desktop.game.map.DWorldMap;
import com.cwsni.world.client.desktop.game.map.GsMapToolBar;
import com.cwsni.world.client.desktop.game.map.MapMode;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.AlertWithStackTraceFactory;
import com.cwsni.world.client.desktop.util.ZoomableScrollPane;
import com.cwsni.world.game.ai.ScriptAIHandler;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.Province;
import com.cwsni.world.model.engine.TimeMode;
import com.cwsni.world.services.GameDataModelLocker;
import com.cwsni.world.services.GameGenerator;
import com.cwsni.world.services.GameHandler;
import com.cwsni.world.services.GameRepository;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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

	// private static final Log logger = LogFactory.getLog(GameScene.class);

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
	private SearchOnMapWindow searchOnMapWindow;

	@Autowired
	private GroovyConsoleWindow groovyConsoleWindow;

	@Autowired
	private AppSettingsEditorWindow settingsEditorWindow;

	@Autowired
	private ScriptAIHandler scriptAIHandler;

	@Autowired
	private GameDataModelLocker gameDataModelLocker;

	private ZoomableScrollPane mapPane;
	private Text statusBarText;

	private Game game;
	private DWorldMap worldMap;
	private Integer selectedProvinceId;

	private MapMode mapMode = MapMode.GEO;
	private TimeMode timeMode = TimeMode.PAUSE;
	private boolean autoTurn = true;
	private boolean pauseBetweenTurn = true;

	private Map<MapMode, Stage> otherMapWindows;
	private Map<MapMode, DWorldMap> otherMaps;

	private MainWindow mainWindow;
	private ConfigurableApplicationContext springContext;

	public GameScene() {
		super(new BorderPane());
	}

	public void init(MainWindow mainWindow, ConfigurableApplicationContext springContext, Game oldGame) {
		this.mainWindow = mainWindow;
		this.springContext = springContext;

		initComponents();

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

		mapPane = new ZoomableScrollPane();
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

		setupGame(oldGame != null ? oldGame : gameGenerator.createEmptyGame());
	}

	private void initComponents() {
		// tabs and menu
		timeControl.init(this);
		mapToolBar.init(this);
		menuBar.init(this);

		// info panels
		globalInfoPane.init(this);
		provInfoPane.init(this);
		provScienceInfoPane.init(this);
		provArmiesInfoPane.init(this);
		countryInfoPane.init(this);
		provEventsInfoPane.init(this);

		{
			// windows
			// It is better to get them from spring context when they are needed.
			createGameWindow.init(this);
			countriesPropertiesWindow.init(this);
			searchOnMapWindow.init(this);
			groovyConsoleWindow.init(this);
			settingsEditorWindow.init(this);
		}
	}

	public ConfigurableApplicationContext getSpringContext() {
		return springContext;
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
				fileChooser.setInitialDirectory(new File(gameRepository.getSaveDirectoryFullPath()));
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
				fileChooser.setInitialDirectory(new File(gameRepository.getSaveDirectoryFullPath()));
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

	public TimeMode getTimeMode() {
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

	public void setTimeModeAndRun(TimeMode newMode) {
		runLocked(() -> {
			TimeMode oldMode = timeMode;
			this.timeMode = newMode;
			if (newMode != TimeMode.PAUSE && oldMode == TimeMode.PAUSE) {
				startProcessingNewTurn();
			}
		});
	}

	private void startProcessingNewTurn() {
		if (getGame().getMap().getProvinces().isEmpty()) {
			return;
		}
		if (timeMode == TimeMode.PAUSE) {
			return;
		}
		gameHadler.processNewTurns(game, timeMode, autoTurn, pauseBetweenTurn,
				() -> Platform.runLater(() -> refreshViewAndStartNewTurn()));
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

	public void runLocked(Runnable r) {
		gameDataModelLocker.runLocked(r);
	}

	public void setUserPreferences(UserUIPreferences userPref) {
		userPref.setTimeControlAutoTurn(isAutoTurn());
		userPref.setTimeControlPauseBetweenTurns(isPauseBetweenTurn());
		userPref.setInfoPaneGlobalMinimized(globalInfoPane.isMinimazed());
		userPref.setInfoPaneCountryMinimized(countryInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceMinimized(provInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceArmiesMinimized(provArmiesInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceScienceMinimized(provScienceInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceEventsMinimized(provEventsInfoPane.isMinimazed());
	}

	public void applyUserPreferences(UserUIPreferences userPref) {
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

			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.CONTROL_DOWN), () -> {
				newMapPane.scaleToDefault();
				if (selectedProvinceId != null) {
					newMapPane.ensureVisible(newWorldMap.findProvinceById(selectedProvinceId));
				}
			});
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.CONTROL_DOWN),
					() -> newMapPane.scaleToFitAllContent());
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

	public void scaleMapToDefault() {
		mapPane.scaleToDefault();
		Platform.runLater(() -> showProvince(game.getMap().findProvById(selectedProvinceId)));
	}

	public void scaleMapToFitAllContent() {
		mapPane.scaleToFitAllContent();
	}

	public void searchOnMap() {
		pauseGame();
		runLocked(() -> {
			Optional<ButtonType> result = searchOnMapWindow.showAndWait();
			if (result.isPresent() && result.get().getButtonData() == ButtonData.OK_DONE) {
				SearchResult selectedSearchResult = searchOnMapWindow.getSelectedSearchResult();
				if (selectedSearchResult != null) {
					switch (selectedSearchResult.type) {
					case PROVINCE:
						selectAndShowProvince(game.getMap().findProvById(selectedSearchResult.id));
						break;
					case COUNTRY:
						Country country = (Country) game.findCountryById(selectedSearchResult.id);
						if (country.getCapital() != null) {
							selectAndShowProvince(country.getCapital());
						} else if (!country.getProvinces().isEmpty()) {
							selectAndShowProvince(country.getProvinces().iterator().next());
						}
						break;
					}
				}
			}
		});
	}

	private void selectAndShowProvince(Province p) {
		selectProvince(p);
		showProvince(p);
	}

	private void showProvince(Province p) {
		if (p == null) {
			return;
		}
		mapPane.ensureVisible(worldMap.findProvinceById(p.getId()));
	}

	public void runGroovyConsole() {
		pauseGame();
		runLocked(() -> {
			groovyConsoleWindow.showAndWait();
		});
	}

	public void editSettings() {
		pauseGame();
		runLocked(() -> {
			settingsEditorWindow.reinit();
			Optional<ButtonType> result = settingsEditorWindow.showAndWait();
			if (result.isPresent() && result.get().getButtonData() == ButtonData.OK_DONE) {
				settingsEditorWindow.storeSettings();
			}
		});
	}

	public void refreshAllForLanguageChange() {
		mainWindow.refreshAllForLanguageChange(game, selectedProvinceId);
	}

	public void restoreSceneAfterLanguageChange(Integer id) {
		selectAndShowProvince(game.getMap().findProvById(id));
	}

}
