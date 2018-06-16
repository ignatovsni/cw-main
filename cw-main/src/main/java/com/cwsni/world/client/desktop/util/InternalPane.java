package com.cwsni.world.client.desktop.util;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class InternalPane extends BorderPane {

	public void init(String title, Pane internalPane) {
		BorderPane titleBar = new BorderPane();
        titleBar.setStyle("-fx-background-color: #999999; -fx-padding: 3");
        Label label = new Label(title);
        titleBar.setLeft(label);
        setStyle("-fx-border-width: 1; -fx-border-color: black");
        setTop(titleBar);
        setCenter(internalPane);
	}
	
}
