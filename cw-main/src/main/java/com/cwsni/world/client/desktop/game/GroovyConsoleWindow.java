package com.cwsni.world.client.desktop.game;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

import groovy.lang.GroovyShell;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
@Scope("prototype")
public class GroovyConsoleWindow extends Dialog<ButtonType> {

	@Autowired
	private LocaleMessageSource messageSource;

	private GameScene gameScene;

	private TextArea scriptArea;
	private TextArea outArea;
	private Button runScriptButton;
	private Button needOutAreaButton;

	private boolean isNeedOutArea = true;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		setTitle(getMessage("window.script-console.title"));
		ButtonType okButtonType = new ButtonType(getMessage("window.script-console.button.ok"), ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().add(okButtonType);

		scriptArea = new TextArea();
		scriptArea.setPrefWidth(600);
		scriptArea.setPrefHeight(300);

		outArea = new TextArea();
		outArea.setPrefHeight(200);

		runScriptButton = new Button(getMessage("window.script-console.button.run"));
		runScriptButton.setOnAction(e -> runScript(scriptArea.getText()));

		needOutAreaButton = new Button(getMessage("window.script-console.button.out-area"));
		needOutAreaButton.setOnAction(e -> {
			this.isNeedOutArea = !this.isNeedOutArea;
			fillDialog();
		});

		fillDialog();
		this.setOnShown(e -> Platform.runLater(() -> scriptArea.requestFocus()));
		this.setResizable(true);
	}

	private void fillDialog() {
		if (isNeedOutArea) {
			needOutAreaButton.setEffect(new Lighting());
			scriptArea.setPrefHeight(400);
			getDialogPane().setContent(new VBox(scriptArea, outArea, new HBox(runScriptButton, needOutAreaButton)));
		} else {
			needOutAreaButton.setEffect(null);
			scriptArea.setPrefHeight(600);
			getDialogPane().setContent(new VBox(scriptArea, new HBox(runScriptButton, needOutAreaButton)));
		}
	}

	private void runScript(String scriptText) {
		try {
			GroovyShell shell = new GroovyShell();
			shell.setProperty("game", gameScene.getGame());
			if (isNeedOutArea) {
				shell.setProperty("out", osToOutArea);
				shell.setProperty("springContext", gameScene.getSpringContext());
			}
			shell.evaluate(scriptText);
		} catch (Exception e) {
			if (isNeedOutArea) {
				e.printStackTrace(osToOutArea);
			} else {
				e.printStackTrace();
			}
		}
	}

	private PrintStream osToOutArea = new PrintStream(System.out) {
		@Override
		public void println(String x) {
			addText(x + "\n");
		}

		@Override
		public void println(Object x) {
			addText(String.valueOf(x) + "\n");
		}

		@Override
		public void println() {
			addText("\n");
		}

		@Override
		public void println(boolean x) {
			addText(String.valueOf(x) + "\n");
		}

		@Override
		public void println(char x) {
			addText(String.valueOf(x) + "\n");
		}

		@Override
		public void println(char[] x) {
			addText(String.valueOf(x) + "\n");
		}

		@Override
		public void println(double x) {
			addText(String.valueOf(x) + "\n");
		}

		@Override
		public void println(float x) {
			addText(String.valueOf(x) + "\n");
		}

		@Override
		public void println(int x) {
			addText(String.valueOf(x) + "\n");
		}

		@Override
		public void println(long x) {
			addText(String.valueOf(x) + "\n");
		}

		@Override
		public void print(boolean b) {
			addText(String.valueOf(b));
		}

		@Override
		public void print(char c) {
			addText(String.valueOf(c));
		}

		@Override
		public void print(char[] s) {
			addText(String.valueOf(s));
		}

		@Override
		public void print(double d) {
			addText(String.valueOf(d));
		}

		@Override
		public void print(float f) {
			addText(String.valueOf(f));
		}

		@Override
		public void print(int i) {
			addText(String.valueOf(i));
		}

		@Override
		public void print(long l) {
			addText(String.valueOf(l));
		}

		@Override
		public void print(Object obj) {
			addText(String.valueOf(obj));
		}

		@Override
		public void print(String s) {
			addText(String.valueOf(s));
		}

		private void addText(Object txt) {
			outArea.setText(outArea.getText() + txt);
		}

		@SuppressWarnings("unused")
		public void clear() {
			outArea.setText("");
		}
	};

}
