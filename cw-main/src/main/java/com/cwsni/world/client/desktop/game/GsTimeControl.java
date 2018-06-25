package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.InternalInfoPane;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Lighting;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@Component
@Scope("prototype")
public class GsTimeControl extends InternalInfoPane {

	private static final Log logger = LogFactory.getLog(GsTimeControl.class);

	private GameScene gameScene;

	private List<Button> buttons;
	private Button pauseButton;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.time.title"), pane);
	}

	private Pane createUI() {
		pauseButton = new Button(getMessage("||"));
		pauseButton.setTooltip(new Tooltip(getMessage("info.pane.time.button.pause")));
		pauseButton.setOnAction(e -> {
			pressButton(pauseButton, GsTimeMode.PAUSE);
		});
		gameScene.putHotKey(new KeyCodeCombination(KeyCode.DIGIT0), () -> pauseButton.fire());
		pauseButton.setEffect(new Lighting());

		Button startButton = new Button(getMessage(">"));
		startButton.setTooltip(new Tooltip(getMessage("info.pane.time.button.start")));
		startButton.setOnAction(e -> {
			pressButton(startButton, GsTimeMode.RUN);
		});
		gameScene.putHotKey(new KeyCodeCombination(KeyCode.DIGIT1), () -> startButton.fire());

		Button startButton_2 = new Button(getMessage(">>"));
		startButton_2.setTooltip(new Tooltip(getMessage("info.pane.time.button.start") + " 2x"));
		startButton_2.setOnAction(e -> {
			pressButton(startButton_2, GsTimeMode.RUN_2);
		});
		gameScene.putHotKey(new KeyCodeCombination(KeyCode.DIGIT2), () -> startButton_2.fire());

		Button startButton_5 = new Button(getMessage(">>>"));
		startButton_5.setTooltip(new Tooltip(getMessage("info.pane.time.button.start") + " 5x"));
		startButton_5.setOnAction(e -> {
			pressButton(startButton_5, GsTimeMode.RUN_5);
		});
		gameScene.putHotKey(new KeyCodeCombination(KeyCode.DIGIT3), () -> startButton_5.fire());

		Button startButton_10 = new Button(getMessage(">>>>"));
		startButton_10.setTooltip(new Tooltip(getMessage("info.pane.time.button.start") + " 10x"));
		startButton_10.setOnAction(e -> {
			pressButton(startButton_10, GsTimeMode.RUN_10);
		});
		gameScene.putHotKey(new KeyCodeCombination(KeyCode.DIGIT4), () -> startButton_10.fire());

		Button systemButton = new Button(getMessage("?"));
		systemButton.setTooltip(new Tooltip("log jvm information"));
		systemButton.setOnAction(e -> {
			logger.info("totalMemory: " + toLong(Runtime.getRuntime().totalMemory()));
			logger.info("maxMemory: " + toLong(Runtime.getRuntime().maxMemory()));
			logger.info("freeMemory: " + toLong(Runtime.getRuntime().freeMemory()));
		});

		buttons = new ArrayList<>();
		buttons.add(pauseButton);
		buttons.add(startButton);
		buttons.add(startButton_2);
		buttons.add(startButton_5);
		buttons.add(startButton_10);
		buttons.add(systemButton);

		HBox hbox = new HBox();
		hbox.getChildren().addAll(buttons);
		VBox vbox = new VBox();

		RadioButton rbAutoTurn = new RadioButton();
		RadioButton rbPauseBetweenTurn = new RadioButton();
		Label labelPauseBetweenTurn = new Label(getMessage("info.pane.time.rb.pause.title"));
		rbAutoTurn.setOnAction(e -> {
			gameScene.setAutoTurn(rbAutoTurn.isSelected());
			rbPauseBetweenTurn.setDisable(!rbAutoTurn.isSelected());
			labelPauseBetweenTurn.setDisable(!rbAutoTurn.isSelected());
		});
		rbAutoTurn.setSelected(gameScene.isAutoTurn());
		rbPauseBetweenTurn.setDisable(!rbAutoTurn.isSelected());
		labelPauseBetweenTurn.setDisable(!rbAutoTurn.isSelected());

		rbPauseBetweenTurn.setOnAction(e -> {
			gameScene.setPauseBetweenTurn(rbPauseBetweenTurn.isSelected());
		});
		rbPauseBetweenTurn.setSelected(gameScene.isPauseBetweenTurn());

		vbox.getChildren().addAll(hbox,
				new HBox(rbAutoTurn, new Label(getMessage("info.pane.time.rb.auto.title") + "  "), rbPauseBetweenTurn,
						labelPauseBetweenTurn));
		return vbox;
	}

	private void pressButton(Button buttonMode, GsTimeMode timeMode) {
		buttons.forEach(b -> b.setEffect(null));
		buttonMode.setEffect(new Lighting());
		gameScene.setTimeModeAndRun(timeMode);
	}

	public void enablePauseButton() {
		buttons.forEach(b -> b.setEffect(null));
		pauseButton.setEffect(new Lighting());
		gameScene.setTimeModeAndRun(GsTimeMode.PAUSE);
	}

}
