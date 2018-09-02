package com.cwsni.world.client.desktop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.engine.Game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

@SpringBootApplication
@ComponentScan("com.cwsni.world")
public class MainWindow extends Application {

	private ConfigurableApplicationContext springContext;
	// private Parent root;
	private Stage stage;

	/**
	 * Use it to prevent savings of user preferences if application didn't start
	 * successfully
	 */
	private boolean successfulStart;

	static private String[] args;

	public static void main(String[] args) {
		MainWindow.args = args;
		launch(MainWindow.class, args);
	}

	@Override
	public void init() throws Exception {
		springContext = SpringApplication.run(MainWindow.class, args);
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
		fxmlLoader.setControllerFactory(springContext::getBean);
		// root = fxmlLoader.load();
	}

	@Override
	public void stop() throws Exception {
		setUserPreferences();
		getUserProperties().savePropertiesToFile();
		springContext.stop();
	}

	private void setUserPreferences() {
		if (stage == null || !successfulStart) {
			return;
		}
		UserUIPreferences userProp = getUserProperties();
		userProp.setMainWindowHeight((int) stage.getHeight());
		userProp.setMainWindowWidth((int) stage.getWidth());
		userProp.setMainWindowPosX((int) stage.getX());
		userProp.setMainWindowPosY((int) stage.getY());
		userProp.setMainWindowMaximized(stage.isMaximized());
		if (stage.getScene() instanceof GameScene) {
			((GameScene) stage.getScene()).setUserPreferences(userProp);
		}
	}

	@Override
	public void start(final Stage stage) {
		this.stage = stage;
		applyUserPreferences2Stage(stage, getUserProperties());
		stage.setOnCloseRequest(e -> Platform.exit());
		initUI(null);
		stage.show();
		successfulStart = true;
	}

	private GameScene initUI(Game game) {
		stage.setTitle(getMessage("main.window.title"));
		GameScene gameScene = createGameScene(stage, game);
		gameScene.applyUserPreferences(getUserProperties());
		return gameScene;
	}

	private GameScene createGameScene(Stage stage, Game game) {
		GameScene gameScene = getGameScene();
		gameScene.init(this, springContext, game);
		stage.setScene(gameScene);
		return gameScene;
	}

	private void applyUserPreferences2Stage(Stage stage, UserUIPreferences userPref) {
		stage.setWidth(userPref.getMainWindowWidth());
		stage.setHeight(userPref.getMainWindowHeight());
		stage.setX(userPref.getMainWindowPosX());
		stage.setY(userPref.getMainWindowPosY());
		stage.setMaximized(userPref.isMainWindowMaximized());
	}

	private String getMessage(String code) {
		return springContext.getBean(LocaleMessageSource.class).getMessage(code);
	}

	private GameScene getGameScene() {
		return springContext.getBean(GameScene.class);
	}

	private UserUIPreferences getUserProperties() {
		return springContext.getBean(UserUIPreferences.class);
	}

	public void refreshAllForLanguageChange(Game game, Integer selectedProvinceId) {
		initUI(game).restoreSceneAfterLanguageChange(selectedProvinceId);
	}

	/*
	 * private static void updateTooltipBehavior(double openDelay, double
	 * visibleDuration, double closeDelay, boolean hideOnExit) { try { // Get the
	 * non public field "BEHAVIOR" Field fieldBehavior =
	 * Tooltip.class.getDeclaredField("BEHAVIOR"); // Make the field accessible to
	 * be able to get and set its value fieldBehavior.setAccessible(true); // Get
	 * the value of the static field Object objBehavior = fieldBehavior.get(null);
	 * // Get the constructor of the private static inner class TooltipBehavior
	 * Constructor<?> constructor =
	 * objBehavior.getClass().getDeclaredConstructor(Duration.class, Duration.class,
	 * Duration.class, boolean.class); // Make the constructor accessible to be able
	 * to invoke it constructor.setAccessible(true); // Create a new instance of the
	 * private static inner class TooltipBehavior Object tooltipBehavior =
	 * constructor.newInstance(new Duration(openDelay), new
	 * Duration(visibleDuration), new Duration(closeDelay), hideOnExit); // Set the
	 * new instance of TooltipBehavior fieldBehavior.set(null, tooltipBehavior); }
	 * catch (Throwable e) { logger.error(e.getMessage(), e); } }
	 * 
	 * static { // there are problems during working (if it is started by command
	 * line) // updateTooltipBehavior(100, 5000, 200, false); }
	 */

}