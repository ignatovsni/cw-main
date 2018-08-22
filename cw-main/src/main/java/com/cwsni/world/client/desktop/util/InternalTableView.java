package com.cwsni.world.client.desktop.util;

import javafx.scene.control.TableView;

public class InternalTableView extends TableView<String> {
	/*
	 * 
	 * private final ObservableList<SimpleRow> data =
	 * FXCollections.observableArrayList();
	 * 
	 * public InternalTableView() { super(); TableColumn<String, String> column1 =
	 * new TableColumn(); column1.setCellValueFactory(new
	 * PropertyValueFactory<>("C1")); TableColumn<String, String> column2 = new
	 * TableColumn(); column2.setCellValueFactory(new PropertyValueFactory<>("C2"));
	 * getColumns().addAll(column1, column2);
	 * column1.prefWidthProperty().bind(widthProperty().multiply(0.3));
	 * column2.prefWidthProperty().bind(widthProperty().multiply(0.65));
	 * setItems(data); }
	 * 
	 * @Override public void resize(double width, double height) {
	 * super.resize(width, height); Pane header = (Pane) lookup("TableHeaderRow");
	 * if (header != null) { header.setMinHeight(0); header.setPrefHeight(0);
	 * header.setMaxHeight(0); header.setVisible(false); } }
	 * 
	 * public static class SimpleRow { private final SimpleStringProperty column1;
	 * private final SimpleStringProperty column2;
	 * 
	 * private SimpleRow(String c1, String c2) { this.column1 = new
	 * SimpleStringProperty(c1); this.column2 = new SimpleStringProperty(c2); }
	 * 
	 * public String getC1() { return column1.get(); }
	 * 
	 * public void setC1(String v) { column1.set(v); }
	 * 
	 * public String getC2() { return column2.get(); }
	 * 
	 * public void setC2(String v) { column2.set(v); } }
	 * 
	 * public void setData(String c1, Object c2) { data.add(new SimpleRow(c1,
	 * String.valueOf(c2))); }
	 * 
	 * public void clearAllRows() { data.clear(); }
	 */

}
