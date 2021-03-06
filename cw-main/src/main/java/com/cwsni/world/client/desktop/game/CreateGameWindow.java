package com.cwsni.world.client.desktop.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.DialogUtil;
import com.cwsni.world.model.data.GameParams;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

@Component
@Scope("prototype")
public class CreateGameWindow extends Dialog<ButtonType> {

	private static final String MAP_SMALL = "20x20";
	private static final String MAP_MEDIUM = "30x40";
	private static final String MAP_LARGE = "50x60";
	private static final String MAP_HUGE = "70x70";
	private static final String MAP_MAX = "100x100";

	@Autowired
	private LocaleMessageSource messageSource;

	private GridPane grid;
	private ComboBox<String> cbMapSizes;
	private ComboBox<String> cbOceanFraction;
	private ComboBox<String> cbMountainFraction;
	private TextField tfRandomSeed;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {

		setTitle(getMessage("window.create-game.title"));
		ButtonType okButtonType = new ButtonType(getMessage("window.create-game.button.ok"), ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().add(okButtonType);
		// getDialogPane().lookupButton(createNewGameButtonType).setDisable(disabled);
		ButtonType cancelButtonType = new ButtonType(getMessage("window.create-game.button.cancel"),
				ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(cancelButtonType);

		ObservableList<String> mapSizesList = FXCollections.observableArrayList(MAP_SMALL, MAP_MEDIUM, MAP_LARGE,
				MAP_HUGE, MAP_MAX);
		cbMapSizes = new ComboBox<String>(mapSizesList);
		cbMapSizes.setValue(MAP_MEDIUM);

		ObservableList<String> oceanFractionList = FXCollections.observableArrayList("0", "10", "20", "30", "40", "50",
				"60", "70", "80");
		cbOceanFraction = new ComboBox<String>(oceanFractionList);
		cbOceanFraction.setValue("40");

		ObservableList<String> mountainFractionList = FXCollections.observableArrayList("0", "0.001", "0.003", "0.005",
				"0.010", "0.015", "0.020");
		cbMountainFraction = new ComboBox<String>(mountainFractionList);
		cbMountainFraction.setValue("0.005");

		tfRandomSeed = new TextField();
		tfRandomSeed.setText(String.valueOf(System.currentTimeMillis()));
		DialogUtil.textFieldOnlyDigits(tfRandomSeed);

		grid = createDefaultGrid();
		int idx = 0;
		addRow("window.create-game.map.size", grid, idx++, cbMapSizes);
		addRow("window.create-game.map.ocean-fraction", grid, idx++, cbOceanFraction);
		addRow("window.create-game.map.mountain-fraction", grid, idx++, cbMountainFraction);
		addRow("window.create-game.random-seed", grid, idx++, tfRandomSeed);

		getDialogPane().setContent(grid);
		this.setOnShown(e -> Platform.runLater(() -> cbMapSizes.requestFocus()));
	}

	public GameParams getGameParams() {
		GameParams gameParams = new GameParams();
		gameParams.setSeed(tfRandomSeed.getText().isEmpty() ? 0 : Long.valueOf(tfRandomSeed.getText()));
		gameParams.setOceanPercent(Double.valueOf(cbOceanFraction.getValue()) / 100);
		gameParams.setMountainsPerMapProvinces(Double.valueOf(cbMountainFraction.getValue()));
		switch (cbMapSizes.getValue()) {
		case MAP_MAX:
			gameParams.setRows(100);
			gameParams.setColumns(100);
			break;
		case MAP_HUGE:
			gameParams.setRows(70);
			gameParams.setColumns(70);
			break;
		case MAP_LARGE:
			gameParams.setRows(50);
			gameParams.setColumns(60);
			break;
		case MAP_MEDIUM:
			gameParams.setRows(30);
			gameParams.setColumns(40);
			break;
		default:
			gameParams.setRows(20);
			gameParams.setColumns(20);
		}
		return gameParams;
	}

	private GridPane createDefaultGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(0);
		grid.setPadding(new Insets(0, 10, 0, 10));
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setHalignment(HPos.RIGHT);
		column1.setPrefWidth(150);
		column1.setMaxWidth(200);
		grid.getColumnConstraints().add(column1);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHalignment(HPos.LEFT);
		grid.getColumnConstraints().add(column2);
		return grid;
	}

	private void addRow(String msgCode, GridPane grid, int row, Node node) {
		Label nameColumn = new Label(String.valueOf(getMessage(msgCode)));
		grid.add(nameColumn, 0, row);
		if (node != null) {
			grid.add(node, 1, row);
		}
	}

	public void reinit() {
		tfRandomSeed.setText(String.valueOf(System.currentTimeMillis()));
	}

}
