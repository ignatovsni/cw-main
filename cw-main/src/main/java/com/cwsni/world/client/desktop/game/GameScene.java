package com.cwsni.world.client.desktop.game;

import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.UserPreferences;
import com.cwsni.world.client.desktop.game.map.DWorldMap;
import com.cwsni.world.client.desktop.game.map.MapMode;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.ZoomableScrollPane;
import com.cwsni.world.common.GameGenerator;
import com.cwsni.world.common.GameRepository;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
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
	private GsProvInfoPane provInfoPane;

	@Autowired
	private GsProvEventsInfoPane provEventsInfoPane;

	@Autowired
	private GsTimeControl timeControl;

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
		provEventsInfoPane.init(this);
		timeControl.init(this);

		VBox menuSection = new VBox();
		menuSection.getChildren().addAll(menuBar);

		VBox rightSection = new VBox();
		rightSection.getChildren().addAll(timeControl, globalInfoPane, provInfoPane, provEventsInfoPane);
		rightSection.setMinWidth(220);
		rightSection.setMaxWidth(220);

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
			refreshAllVisibleInfoAndResetSelections();
		});
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
				statusBarText.setText("Selected province with id = " + province.getId());
			}
			provInfoPane.refreshInfo();
			provEventsInfoPane.refreshInfo();
		});
	}

	public DWorldMap getWorldMap() {
		return worldMap;
	}

	public void setMapMode(MapMode mapMode) {
		this.mapMode = mapMode;
	}

	public void exitApp() {
		stage.close();
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
		provInfoPane.refreshInfo();
		provEventsInfoPane.refreshInfo();
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
				runLocked(() -> processNewTurn());
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
		if (autoTurn) {
			startProcessingNewTurn();
		} else {
			timeControl.enablePauseButton();
		}
	}

	private void processNewTurn() {
		try {
			for (int i = 0; i < timeMode.getTurnPerTime(); i++) {
				game.processNewTurn(messageSource);
			}
		} catch (Exception e) {
			logError(e);
		}
		if (autoTurn && pauseBetweenTurn) {
			try {
				Thread.sleep(50 * Math.max((10 - timeMode.getTurnPerTime()), 0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void logError(Exception e) {
		logger.error(e);
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
		userPref.setInfoPaneProvinceMinimized(provInfoPane.isMinimazed());
		userPref.setInfoPaneProvinceEventsMinimized(provEventsInfoPane.isMinimazed());
	}

	public void applyUserPreferences(UserPreferences userPref) {
		timeControl.applyUserPreferences(userPref);
		globalInfoPane.setMinimized(userPref.isInfoPaneGlobalMinimized());
		provInfoPane.setMinimized(userPref.isInfoPaneProvinceMinimized());
		provEventsInfoPane.setMinimized(userPref.isInfoPaneProvinceEventsMinimized());
	}

}
