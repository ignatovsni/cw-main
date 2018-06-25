package com.cwsni.world.client.desktop.util;

import org.springframework.beans.factory.annotation.Autowired;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class InternalInfoPane extends BorderPane {

	protected class RowValue {
		private Node nameNode;
		private Label valueNode;

		public RowValue(Node nameColumn, Label valueColumn) {
			this.nameNode = nameColumn;
			this.valueNode = valueColumn;
		}

		public void setVisible(boolean b) {
			nameNode.setVisible(b);
			valueNode.setVisible(b);
		}

		public void setValue(String v) {
			valueNode.setText(v);
		}

	}

	@Autowired
	private LocaleMessageSource messageSource;
	private Pane internalPane;
	private Label modeLabel;
	private boolean isMinimized = false;

	protected String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(String title, Pane internalPane) {
		this.internalPane = internalPane;
		Pane titleBar = createTitle(title);
		setTop(titleBar);
		setCenter(internalPane);
		setMinimized(isMinimized);
	}

	private Pane createTitle(String title) {
		modeLabel = new Label();
		HBox titleBar = new HBox();
		titleBar.setStyle("-fx-background-color: #999999; -fx-padding: 3");
		Label label = new Label(title);
		titleBar.getChildren().addAll(modeLabel, label);
		setStyle("-fx-border-width: 1; -fx-border-color: black");

		titleBar.setOnMouseClicked(e -> setMinimized(!isMinimized));

		return titleBar;
	}

	public void setMinimized(boolean newMode) {
		isMinimized = newMode;
		if (isMinimized) {
			modeLabel.setText("> ");
			setCenter(null);
		} else {
			modeLabel.setText("v ");
			setCenter(internalPane);			
		}
	}

	public boolean isMinimazed() {
		return isMinimized;
	}

	protected GridPane createDefaultGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(0);
		grid.setPadding(new Insets(0, 10, 0, 10));
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setHalignment(HPos.RIGHT);
		grid.getColumnConstraints().add(column1);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHalignment(HPos.LEFT);
		grid.getColumnConstraints().add(column2);
		return grid;
	}

	protected RowValue addRow(String msgCode, GridPane grid, int row, String txt) {
		Node nameColumn = createGridNameColumn(getMessage(msgCode));
		grid.add(nameColumn, 0, row);
		Label label = createGridValueColumn(0);
		grid.add(label, 1, row);
		label.setText(txt);
		return new RowValue(nameColumn, label);
	}

	protected RowValue addRow(String msgCode, GridPane grid, int row) {
		return addRow(msgCode, grid, row, "");
	}

	protected Node createGridNameColumn(Object title) {
		String txt = String.valueOf(title);
		return new Label(txt);
	}

	protected Label createGridValueColumn(Object value) {
		String txt = String.valueOf(value);
		return new Label(txt);
	}

}
