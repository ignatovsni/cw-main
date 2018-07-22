package com.cwsni.world.client.desktop;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;

@SpringBootApplication
@ComponentScan("com.cwsni.world")
public class MainWindow extends Application {

	private static final Log logger = LogFactory.getLog(MainWindow.class);

	private ConfigurableApplicationContext springContext;
	private Parent root;
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
		root = fxmlLoader.load();
	}

	@Override
	public void stop() throws Exception {
		setUserPreferences();
		getUserProperties().saveUserProperties();
		springContext.stop();
	}

	private void setUserPreferences() {
		if (stage == null || !successfulStart) {
			return;
		}
		UserPreferences userProp = getUserProperties();
		userProp.setMainWindowHeight(stage.getHeight());
		userProp.setMainWindowWidth(stage.getWidth());
		userProp.setMainWindowPositionX(stage.getX());
		userProp.setMainWindowPositionY(stage.getY());
		userProp.setMainWindowMaximazed(stage.isMaximized());
		if (stage.getScene() instanceof GameScene) {
			((GameScene) stage.getScene()).setUserPreferences(userProp);
		}
	}

	@Override
	public void start(final Stage stage) {
		this.stage = stage;
		stage.setTitle(getMessage("main.window.title"));
		createGameScene(stage);
		applyUserPreferences(stage);
		stage.setOnCloseRequest(e -> Platform.exit());
		stage.show();
		successfulStart = true;
	}

	private void createGameScene(Stage stage) {
		GameScene gameScene = getGameScene();
		gameScene.init();
		stage.setScene(gameScene);
	}

	private void applyUserPreferences(Stage stage) {
		UserPreferences userPref = getUserProperties();
		stage.setWidth(userPref.getMainWindowWidth());
		stage.setHeight(userPref.getMainWindowHeight());
		stage.setX(userPref.getMainWindowPosX());
		stage.setY(userPref.getMainWindowPosY());
		stage.setMaximized(userPref.isMainWindowMaximized());
		if (stage.getScene() instanceof GameScene) {
			((GameScene) stage.getScene()).applyUserPreferences(userPref);
		}
	}

	private String getMessage(String code) {
		return springContext.getBean(LocaleMessageSource.class).getMessage(code);
	}

	private GameScene getGameScene() {
		return springContext.getBean(GameScene.class);
	}

	private UserPreferences getUserProperties() {
		return springContext.getBean(UserPreferences.class);
	}

	private static void updateTooltipBehavior(double openDelay, double visibleDuration, double closeDelay,
			boolean hideOnExit) {
		try {
			// Get the non public field "BEHAVIOR"
			Field fieldBehavior = Tooltip.class.getDeclaredField("BEHAVIOR");
			// Make the field accessible to be able to get and set its value
			fieldBehavior.setAccessible(true);
			// Get the value of the static field
			Object objBehavior = fieldBehavior.get(null);
			// Get the constructor of the private static inner class TooltipBehavior
			Constructor<?> constructor = objBehavior.getClass().getDeclaredConstructor(Duration.class, Duration.class,
					Duration.class, boolean.class);
			// Make the constructor accessible to be able to invoke it
			constructor.setAccessible(true);
			// Create a new instance of the private static inner class TooltipBehavior
			Object tooltipBehavior = constructor.newInstance(new Duration(openDelay), new Duration(visibleDuration),
					new Duration(closeDelay), hideOnExit);
			// Set the new instance of TooltipBehavior
			fieldBehavior.set(null, tooltipBehavior);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	static {
		//updateTooltipBehavior(100, 5000, 200, false);
	}

}