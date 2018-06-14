package com.cwsni.world.client.desktop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

@SpringBootApplication
public class MainWindow extends Application {

	private static final Log logger = LogFactory.getLog(MainWindow.class);

	private ConfigurableApplicationContext springContext;
	private Parent root;
	private Stage stage;

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
		if (stage == null) {
			return;
		}
		UserProperties userProp = getUserProperties();
		userProp.setMainWindowHeight(stage.getHeight());
		userProp.setMainWindowWidth(stage.getWidth());
		userProp.setMainWindowPositionX(stage.getX());
		userProp.setMainWindowPositionY(stage.getY());
	}

	@Override
	public void start(final Stage stage) {
		this.stage = stage;
		stage.setTitle(getMessage("main.window.title"));
		createGameScene(stage);
		applyUserPreferences(stage);
		stage.show();
	}

	private void createGameScene(Stage stage) {
		GameScene gameScene = getGameScene();
		gameScene.setStage(stage);
		gameScene.initialize();
		stage.setScene(gameScene);
	}

	private void applyUserPreferences(Stage stage) {
		UserProperties userProp = getUserProperties();
		stage.setWidth(userProp.getMainWindowWidth());
		stage.setHeight(userProp.getMainWindowHeight());
		stage.setX(userProp.getMainWindowPosX());
		stage.setY(userProp.getMainWindowPosY());
	}

	private String getMessage(String code) {
		return springContext.getBean(LocaleMessageSource.class).getMessage(code);
	}

	private GameScene getGameScene() {
		return springContext.getBean(GameScene.class);
	}

	private UserProperties getUserProperties() {
		return springContext.getBean(UserProperties.class);
	}

}