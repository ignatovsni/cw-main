package com.cwsni.world.client.desktop.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

abstract public class InternalInfoPane extends BorderPane {

	protected class RowValue {
		private Label nameNode;
		private Label valueNode;

		public RowValue(Label nameColumn, Label valueColumn) {
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

		public void setNameColumnTooltip(String txt) {
			nameNode.setTooltip(new Tooltip(txt));
		}

		public void setValueColumnTooltip(String txt) {
			valueNode.setTooltip(new Tooltip(txt));
		}

	}

	@Autowired
	private LocaleMessageSource messageSource;
	private Pane internalPane;
	private Label modeLabel;
	private List<RowValue> infoRows;
	private boolean isMinimized = false;
	private Pane titleBar;
	private Stage tooltipWindow;
	private Pane tooltipLayout;

	protected String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(String title, Pane internalPane) {
		this.internalPane = internalPane;
		titleBar = createTitle(title);
		addTooltipWindow(titleBar);
		setTop(titleBar);
		setCenter(internalPane);
		setMinimized(isMinimized);
	}

	private void addTooltipWindow(Pane titleBar) {
		tooltipWindow = new Stage();
		tooltipWindow.initStyle(StageStyle.UNDECORATED);
		tooltipWindow.initModality(Modality.NONE);
		tooltipLayout = new VBox();
		tooltipLayout.setStyle("-fx-border-width: 1; -fx-border-color: black");
		Scene tooltipScene = new Scene(tooltipLayout);
		tooltipWindow.setScene(tooltipScene);
		titleBar.setOnMouseEntered(e -> showTooltipWindowIfPossible());
		titleBar.setOnMouseExited(e -> {
			tooltipLayout.getChildren().clear();
			tooltipWindow.hide();
		});
	}

	private void showTooltipWindowIfPossible() {
		if (isMinimized && hasDataForUser() && tooltipLayout.getChildren().isEmpty()) {
			tooltipLayout.getChildren().add(internalPane);
			Bounds screenCoords = titleBar.localToScreen(titleBar.getBoundsInLocal());
			tooltipWindow.setX(screenCoords.getMinX());
			tooltipWindow.setY(screenCoords.getMaxY() + 1);
			tooltipWindow.show();
		}
	}

	abstract protected boolean hasDataForUser();

	public void refreshInfo() {
		getInfoRows().forEach(l -> {
			l.setVisible(false);
			l.setValue("");
		});
		if (!hasDataForUser()) {
			return;
		}
		refreshInfoInternal();
	}

	protected void refreshInfoInternal() {
		// nothing
	};

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
			if (this.getWidth() > 0) {
				// getWidth() > 0 : to prevent showing before primary stage
				showTooltipWindowIfPossible();
			}
		} else {
			modeLabel.setText("v ");
			setCenter(internalPane);
		}
	}

	public boolean isMinimazed() {
		return isMinimized;
	}

	protected List<RowValue> getInfoRows() {
		return infoRows;
	}

	protected void setInfoRows(List<RowValue> rows) {
		this.infoRows = rows;
	}

	protected void clearPane() {
		internalPane.getChildren().clear();
		infoRows.clear();
	}

	protected Pane getInternalPane() {
		return internalPane;
	}

	protected GridPane createDefaultGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(0);
		grid.setPadding(new Insets(0, 10, 0, 10));
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setHalignment(HPos.RIGHT);
		column1.setMinWidth(90);
		column1.setMaxWidth(90);
		grid.getColumnConstraints().add(column1);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHalignment(HPos.LEFT);
		grid.getColumnConstraints().add(column2);
		return grid;
	}

	protected RowValue addRow(String msgCode, GridPane grid, int row, String txt) {
		Label nameColumn = createGridNameColumn(getMessage(msgCode));
		grid.add(nameColumn, 0, row);
		Label label = createGridValueColumn(0);
		grid.add(label, 1, row);
		label.setText(txt);
		RowValue rowValue = new RowValue(nameColumn, label);
		infoRows.add(rowValue);
		return rowValue;
	}

	protected RowValue addRow(String msgCode, GridPane grid, int row) {
		return addRow(msgCode, grid, row, "");
	}

	protected Label createGridNameColumn(Object value) {
		return new Label(String.valueOf(value));
	}

	protected Label createGridValueColumn(Object value) {
		return new Label(String.valueOf(value));
	}

	protected void setLabelText(RowValue l, String txt) {
		l.setValue(txt);
		l.setVisible(true);
	}

	protected void setLabelText(RowValue l, String txt, Object valueTooltip) {
		setLabelText(l, txt);
		if (valueTooltip != null) {
			l.setValueColumnTooltip(valueTooltip.toString());
		}
	}

	protected void setLabelTextWithLongFormatterAndValueTooltip(RowValue l, Long value) {
		setLabelText(l, DataFormatter.formatLongNumber(value), value);
	}

	protected void setLabelTextWithLongFormatterAndValueTooltip(RowValue l, Integer value) {
		setLabelText(l, DataFormatter.formatLongNumber(Long.valueOf(value)), value);
	}

}
