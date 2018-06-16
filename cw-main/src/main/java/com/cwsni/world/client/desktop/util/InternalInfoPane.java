package com.cwsni.world.client.desktop.util;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class InternalInfoPane extends BorderPane {

	public void init(String title, Pane internalPane) {
		BorderPane titleBar = new BorderPane();
        titleBar.setStyle("-fx-background-color: #999999; -fx-padding: 3");
        Label label = new Label(title);
        titleBar.setLeft(label);
        setStyle("-fx-border-width: 1; -fx-border-color: black");
        setTop(titleBar);
        setCenter(internalPane);
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
	
	protected Node createGridNameColumn(Object title) {
		String txt = String.valueOf(title);
		return new Label(txt);
	}

	protected Label createGridValueColumn(Object value) {
		String txt = String.valueOf(value);
		return new Label(txt);
	}
	
	protected String toString(Object o) {
		return String.valueOf(o);
	}
	
	protected String toInt(int i) {
		return DataFormatter.formatIntNumber(i);
	}
	
}
