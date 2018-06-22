package com.cwsni.world.client.desktop.game;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private GsToolBar toolBar;

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

	public GameScene() {
		super(new BorderPane());
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void init() {
		mapPane = new ZoomableScrollPane();
		toolBar.init(this);
		menuBar.init(this);
		globalInfoPane.init(this);
		provInfoPane.init(this);
		provEventsInfoPane.init(this);
		timeControl.init(this);

		VBox topSection = new VBox();
		topSection.getChildren().addAll(menuBar, toolBar);

		VBox rightSection = new VBox();
		rightSection.getChildren().addAll(timeControl, globalInfoPane, provInfoPane, provEventsInfoPane);
		rightSection.setMinWidth(220);
		rightSection.setMaxWidth(220);

		BorderPane layout = (BorderPane) getRoot();
		layout.setTop(topSection);
		layout.setBottom(createStatusBar());
		layout.setRight(rightSection);
		layout.setCenter(mapPane);

		setupGame(gameGenerator.createEmptyGame());
	}

	public void quickSaveGame() {
		if (game != null) {
			gameRepository.quickSaveGame(game);
		}
	}

	public void quickLoadGame() {
		Game newGame = gameRepository.quickLoadGame();
		if (newGame != null) {
			setupGame(newGame);
		}
	}

	private void setupGame(Game newGame) {
		DWorldMap worldMap = DWorldMap.createDMap(newGame, mapMode);
		mapPane.setTarget(worldMap.getMapGroup());
		this.game = worldMap.getGame();
		this.worldMap = worldMap;
		worldMap.setGameScene(this);
		refreshAllVisibleInfoAndResetSelections();
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
		this.selectedProvinceId = province != null ? province.getId() : null;
		if (province != null) {
			statusBarText.setText("Selected province with id = " + province.getId());
		}
		provInfoPane.refreshInfo();
		provEventsInfoPane.refreshInfo();
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
		GsTimeMode oldMode = timeMode;
		this.timeMode = newMode;
		if (newMode != GsTimeMode.PAUSE && oldMode == GsTimeMode.PAUSE) {
			startProcessingNewTurn();
		}
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
				processNewTurn();
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

	private void processNewTurn() throws InterruptedException {
		try {
			for (int i = 0; i < timeMode.getTurnPerTime(); i++) {
				game.processNewTurn(messageSource);
			}
		} catch (Exception e) {
			logError(e);
		}
		if (autoTurn && pauseBetweenTurn) {
			Thread.sleep(50 * Math.max((10 - timeMode.getTurnPerTime()), 0));
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

}
