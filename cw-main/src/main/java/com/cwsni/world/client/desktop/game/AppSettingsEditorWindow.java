package com.cwsni.world.client.desktop.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.DialogUtil;
import com.cwsni.world.settings.ApplicationSettings;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

@Component
@Scope("prototype")
public class AppSettingsEditorWindow extends Dialog<ButtonType> {

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private ApplicationSettings applicationSettings;

	private GridPane grid;
	private CheckBox autoSaveCheckBox;
	private TextField autoSaveTurnSteps;
	private TextField autoSaveMaxFiles;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		setTitle(getMessage("window.app-settings.title"));
		ButtonType okButtonType = new ButtonType(getMessage("window.app-settings.button.ok"), ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().add(okButtonType);
		ButtonType cancelButtonType = new ButtonType(getMessage("window.app-settings.button.cancel"),
				ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(cancelButtonType);

		grid = createDefaultGrid();

		autoSaveCheckBox = new CheckBox();
		autoSaveCheckBox.setOnAction(e -> {
			autoSaveTurnSteps.setDisable(!autoSaveCheckBox.isSelected());
			autoSaveMaxFiles.setDisable(!autoSaveCheckBox.isSelected());
		});
		autoSaveTurnSteps = new TextField();
		DialogUtil.textFieldOnlyDigits(autoSaveTurnSteps);
		autoSaveMaxFiles = new TextField();
		DialogUtil.textFieldOnlyDigits(autoSaveMaxFiles);

		int idx = 0;

		addRow("window.app-settings.autosave.section.label", grid, idx++, null);
		addRow("window.app-settings.autosave.use", grid, idx++, autoSaveCheckBox);
		addRow("window.app-settings.autosave.turn-step", grid, idx++, autoSaveTurnSteps);
		addRow("window.app-settings.autosave.max-files", grid, idx++, autoSaveMaxFiles);

		addRow(null, grid, idx++, null);

		Label warningAboutChanges = new Label(getMessage("window.app-settings.warning-about-changes"));
		VBox paneContent = new VBox();
		paneContent.getChildren().addAll(grid, warningAboutChanges);
		getDialogPane().setContent(paneContent);
		this.setOnShown(e -> Platform.runLater(() -> autoSaveCheckBox.requestFocus()));
	}

	private GridPane createDefaultGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(0);
		grid.setPadding(new Insets(0, 10, 0, 10));
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setHalignment(HPos.RIGHT);
		column1.setMinWidth(100);
		column1.setMaxWidth(200);
		grid.getColumnConstraints().add(column1);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHalignment(HPos.LEFT);
		column2.setMinWidth(100);
		column2.setMaxWidth(100);
		grid.getColumnConstraints().add(column2);

		return grid;
	}

	public void reinit() {
		autoSaveCheckBox.setSelected(applicationSettings.isUseAutoSave());
		autoSaveTurnSteps.setText(String.valueOf(applicationSettings.getAutoSaveTurnStep()));
		autoSaveMaxFiles.setText(String.valueOf(applicationSettings.getAutoSaveMaxFiles()));
	}

	private void addRow(String msgCode, GridPane grid, int row, Node node) {
		Label nameColumn;
		if (msgCode != null) {
			nameColumn = new Label(String.valueOf(getMessage(msgCode)));
		} else {
			nameColumn = new Label("");
		}
		grid.add(nameColumn, 0, row);

		if (node != null) {
			grid.add(node, 1, row);
		}
	}

	public void storeSettings() {
		applicationSettings.setUseAutoSave(autoSaveCheckBox.isSelected());
		applicationSettings
				.setAutoSaveTurnStep(toInt(autoSaveTurnSteps.getText(), applicationSettings.getAutoSaveTurnStep()));
		applicationSettings
				.setAutoSaveMaxFiles(toInt(autoSaveMaxFiles.getText(), applicationSettings.getAutoSaveMaxFiles()));

		applicationSettings.savePropertiesToFile();
	}

	private int toInt(String text, int oldValue) {
		try {
			return Integer.valueOf(text);
		} catch (NumberFormatException e) {
			return oldValue;
		}
	}

}
