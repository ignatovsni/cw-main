package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.UserPreferences;
import com.cwsni.world.client.desktop.game.map.DWorldMap;
import com.cwsni.world.client.desktop.game.map.MapMode;
import com.cwsni.world.client.desktop.util.ZoomableScrollPane;
import com.cwsni.world.common.GameGenerator;
import com.cwsni.world.common.GameRepository;
import com.cwsni.world.game.GameHandler;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
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

	private static final Log logger = LogFactory.getLog(GameScene.class);

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
	private GsProvEventsInfoPane provEventsInfoPane;

	@Autowired
	private GsTimeControl timeControl;

	@Autowired
	private GameHandler gameHadler;

	private Stage stage;
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

	private Semaphore lockObj = new Semaphore(1);

	public GameScene() {
		super(new BorderPane());
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void init() {
		mapPane = new ZoomableScrollPane();
		mapToolBar.init(this);
		menuBar.init(this);
		globalInfoPane.init(this);
		provInfoPane.init(this);
		provScienceInfoPane.init(this);
		countryInfoPane.init(this);
		provEventsInfoPane.init(this);
		timeControl.init(this);

		VBox rightInfoPanes = new VBox();
		rightInfoPanes.getChildren().addAll(countryInfoPane, provInfoPane, provScienceInfoPane, provEventsInfoPane);
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

	public void quickSaveGame() {
		if (game != null) {
			runLocked(() -> gameRepository.quickSaveGame(game));
		}
	}

	public void quickLoadGame() {
		Game newGame = gameRepository.quickLoadGame();
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
		Game game = gameGenerator.createTestGame();
		setupGame(game);
		refreshAllVisibleInfo();
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
		refreshAllVisibleInfo();
		getWorldMap().setMapModeAndRedraw(mapMode);
		otherMaps.forEach((mode, map) -> map.setMapModeAndRedraw(mode));
		if (autoTurn) {
			startProcessingNewTurn();
		} else {
			timeControl.enablePauseButton();
		}
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

	public void lock() {
		try {
			lockObj.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void releaseLock() {
		lockObj.release();
	}

	private void runLocked(Runnable r) {
		lock();
		try {
			r.run();
		} finally {
			releaseLock();
		}
	}

	public void setUserPreferences(UserPreferences userPref) {
		userPref.setTimeControlAutoTurn(isAutoTurn());
		userPref.setTimeControlPauseBetweenTurns(isPauseBetweenTurn());
		userPref.setInfoPaneGlobalMinimized(globalInfoPane.isMinimazed());
		userPref.setInfoPaneCountryMinimized(countryInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceMinimized(provInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceScienceMinimized(provScienceInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceEventsMinimized(provEventsInfoPane.isMinimazed());
	}

	public void applyUserPreferences(UserPreferences userPref) {
		timeControl.applyUserPreferences(userPref);
		globalInfoPane.setMinimized(userPref.isInfoPaneGlobalMinimized());
		countryInfoPane.setMinimized(userPref.isInfoPaneCountryMinimized());
		provInfoPane.setMinimized(userPref.isInfoPaneProvinceMinimized());
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

}
