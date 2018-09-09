package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.UserUIPreferences;
import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.model.engine.TimeMode;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Lighting;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@Component
@Scope("prototype")
public class GsTimeControl extends BorderPane {

	private static final Log logger = LogFactory.getLog(GsTimeControl.class);

	@Autowired
	private LocaleMessageSource messageSource;

	private GameScene gameScene;

	private List<Button> buttons;
	private Button pauseButton;

	private RadioButton rbAutoTurn;

	private RadioButton rbPauseBetweenTurn;

	private Label labelPauseBetweenTurn;

	private Boolean isCtrlDown;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		setCenter(pane);
	}

	private Pane createUI() {
		pauseButton = new Button(getMessage("||"));
		pauseButton.setTooltip(new Tooltip(getMessage("info.pane.time.button.pause") + "\n"
				+ getMessage("info.pane.time.tooltip.press-key") + " [0]"));
		pauseButton.setOnAction(e -> {
			pressButton(pauseButton, TimeMode.PAUSE);
		});
		gameScene.putHotKey(new KeyCodeCombination(KeyCode.DIGIT0), () -> pauseButton.fire());
		// gameScene.putHotKey(new KeyCodeCombination(KeyCode.SPACE), () ->
		// pauseButton.fire());
		pauseButton.setEffect(new Lighting());

		Button weekButton = createButton(">", "week", TimeMode.WEEK, KeyCode.DIGIT1);
		Button monthButton = createButton(">>", "month", TimeMode.MONTH, KeyCode.DIGIT2);
		Button yearButton = createButton(">>>", "year", TimeMode.YEAR, KeyCode.DIGIT3);
		Button year10Button = createButton(">>>>", "year-10", TimeMode.YEAR_10, KeyCode.DIGIT4);

		Button systemButton = new Button(getMessage("?"));
		systemButton.setTooltip(new Tooltip("system/development action"));
		systemButton.setOnAction(e -> {
			logger.info("totalMemory: " + DataFormatter.toLong(Runtime.getRuntime().totalMemory()));
			logger.info("maxMemory: " + DataFormatter.toLong(Runtime.getRuntime().maxMemory()));
			logger.info("freeMemory: " + DataFormatter.toLong(Runtime.getRuntime().freeMemory()));
		});

		buttons = new ArrayList<>();
		buttons.add(pauseButton);
		buttons.add(weekButton);
		buttons.add(monthButton);
		buttons.add(yearButton);
		buttons.add(year10Button);
		buttons.add(systemButton);

		HBox hbox = new HBox();
		hbox.getChildren().addAll(buttons);
		VBox vbox = new VBox();

		rbAutoTurn = new RadioButton();
		rbPauseBetweenTurn = new RadioButton();
		labelPauseBetweenTurn = new Label(getMessage("info.pane.time.rb.pause.title"));
		rbAutoTurn.setOnAction(e -> {
			gameScene.setAutoTurn(rbAutoTurn.isSelected());
			switchPauseBetweenTurn();
		});
		rbAutoTurn.setSelected(gameScene.isAutoTurn());
		switchPauseBetweenTurn();

		rbPauseBetweenTurn.setOnAction(e -> {
			gameScene.setPauseBetweenTurn(rbPauseBetweenTurn.isSelected());
		});
		rbPauseBetweenTurn.setSelected(gameScene.isPauseBetweenTurn());

		vbox.getChildren().addAll(hbox,
				new HBox(rbAutoTurn, new Label(getMessage("info.pane.time.rb.auto.title") + "  "), rbPauseBetweenTurn,
						labelPauseBetweenTurn));
		return vbox;
	}

	private void switchPauseBetweenTurn() {
		rbPauseBetweenTurn.setDisable(!rbAutoTurn.isSelected());
		labelPauseBetweenTurn.setDisable(!rbAutoTurn.isSelected());
	}

	private Button createButton(String messageLabelCode, String turnSubCode, TimeMode timeMode, KeyCode keyCode) {
		Button button = new Button(getMessage(messageLabelCode));
		button.setTooltip(new Tooltip(getMessage("info.pane.time.button.start." + turnSubCode) + "\n"
				+ String.format(getMessage("info.pane.time.tooltip.press-key"), keyCode.getName()) + "\n"
				+ String.format(getMessage("info.pane.time.tooltip.press-key.ctrl"), "Ctrl+" + keyCode.getName())));
		button.setOnAction(e -> {
			pressButton(button, timeMode);
		});
		gameScene.putHotKey(new KeyCodeCombination(keyCode), () -> {
			isCtrlDown = false;
			button.fire();
			isCtrlDown = null;
		});
		gameScene.putHotKey(new KeyCodeCombination(keyCode, KeyCombination.CONTROL_DOWN), () -> {
			isCtrlDown = true;
			button.fire();
			isCtrlDown = null;
		});
		return button;
	}

	private void pressButton(Button buttonMode, TimeMode timeMode) {
		if (gameScene.getTimeMode() != timeMode) {
			buttons.forEach(b -> b.setEffect(null));
			buttonMode.setEffect(new Lighting());
			if (isCtrlDown != null) {
				rbAutoTurn.setSelected(!isCtrlDown);
				rbAutoTurn.fire();
			}
			gameScene.setTimeModeAndRun(timeMode);
		} else {
			enablePauseButton();
		}
	}

	public void enablePauseButton() {
		buttons.forEach(b -> b.setEffect(null));
		pauseButton.setEffect(new Lighting());
		gameScene.setTimeModeAndRun(TimeMode.PAUSE);
	}

	protected String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void applyUserPreferences(UserUIPreferences userPref) {
		rbAutoTurn.setSelected(userPref.isTimeControlAutoTurn());
		rbPauseBetweenTurn.setSelected(userPref.isTimeControlPauseBetweenTurns());
		switchPauseBetweenTurn();
		gameScene.setAutoTurn(rbAutoTurn.isSelected());
		gameScene.setPauseBetweenTurn(rbPauseBetweenTurn.isSelected());
	}

}
