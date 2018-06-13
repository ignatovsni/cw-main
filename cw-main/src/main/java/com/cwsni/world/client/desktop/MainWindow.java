package com.cwsni.world.client.desktop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

@SpringBootApplication
public class MainWindow extends Application  {
	
	private static final Log logger = LogFactory.getLog(MainWindow.class);
	
	private ConfigurableApplicationContext springContext;
    private Parent root;
    
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
        springContext.stop();
    }
	
	@Override
	public void start(final Stage stage) {		
		stage.setTitle(getMessage("main.window.title"));
		stage.setScene(getGameScene().createScene(stage));
		stage.setHeight(600);
		stage.setWidth(800);		
		stage.show();
	}
	
	private String getMessage(String code) {
		return springContext.getBean(LocaleMessageSource.class).getMessage(code);
	}
	
	private GameScene getGameScene() {
		return springContext.getBean(GameScene.class);
	}

}