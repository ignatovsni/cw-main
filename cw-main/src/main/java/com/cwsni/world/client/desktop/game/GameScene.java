package com.cwsni.world.client.desktop.game;

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

import javafx.scene.Scene;
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
	
	private Stage stage;
	private ZoomableScrollPane mapPane;
	private Text statusBarText;

	private Game game;
	private DWorldMap worldMap;
	private Province selectedProvince;

	private MapMode mapMode = MapMode.GEO;

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
		
		VBox topSection = new VBox();
		topSection.getChildren().addAll(menuBar, toolBar);

		VBox rightSection = new VBox();
		rightSection.getChildren().addAll(globalInfoPane, provInfoPane);
		rightSection.setMaxWidth(200);
		
		BorderPane layout = (BorderPane) getRoot();
		layout.setTop(topSection);
		layout.setBottom(createStatusBar());
		layout.setRight(rightSection);
		layout.setCenter(mapPane);
			
		createTestGame();
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
		refreshAllVisibleInfo();
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
		refreshAllVisibleInfo();
	}

	// Event listener can be a good idea instead of direct invocation
	public void selectProvince(Province province) {
		this.selectedProvince = province;
		if (selectedProvince != null) {
			statusBarText.setText("Selected province with id = " + selectedProvince.getId() + "; population = "
					+ selectedProvince.getPopulationAmount());
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

	private void refreshAllVisibleInfo() {
		globalInfoPane.refreshInfo();
		provInfoPane.refreshInfo();
	}

	public Province getSelectedProvince() {
		return selectedProvince;
	}

}
