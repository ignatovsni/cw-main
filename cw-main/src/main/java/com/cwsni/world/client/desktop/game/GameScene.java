package com.cwsni.world.client.desktop.game;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.game.map.DWorldMap;
import com.cwsni.world.client.desktop.game.map.MapMode;
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
	private GsTimeControl timeControl;

	private Stage stage;
	private ZoomableScrollPane mapPane;
	private Text statusBarText;

	private Game game;
	private DWorldMap worldMap;
	private Integer selectedProvinceId;

	private MapMode mapMode = MapMode.GEO;
	private GsTimeMode timeMode = GsTimeMode.PAUSE;

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
		timeControl.init(this);

		VBox topSection = new VBox();
		topSection.getChildren().addAll(menuBar, toolBar);

		VBox rightSection = new VBox();
		rightSection.getChildren().addAll(timeControl, globalInfoPane, provInfoPane);
		rightSection.setMaxWidth(200);

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

	// Event listener can be a good idea instead of direct invocation
	public void selectProvince(Province province) {
		this.selectedProvinceId = province != null ? province.getId() : null;
		if (province != null) {
			statusBarText.setText("Selected province with id = " + province.getId() + "; population = "
					+ province.getPopulationAmount());
		}
		provInfoPane.refreshInfo();
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
		startProcessingNewTurn();
	}

	private void processNewTurn() throws InterruptedException {
		try {
			for (int i = 0; i < timeMode.getTurnPerTime(); i++) {
				game.processNewTurn();
			}
		} catch (Exception e) {
			logError(e);
		}
		Thread.sleep(50 * (10 - timeMode.getTurnPerTime()));
	}

	private void logError(Exception e) {
		logger.error(e);
	}

}
