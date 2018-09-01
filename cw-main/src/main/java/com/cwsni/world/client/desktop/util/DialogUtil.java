package com.cwsni.world.client.desktop.util;

import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class DialogUtil {

	static public String showTextInputDialog(String title, String contentText, String defaultValue) {
		TextInputDialog tiDialog = new TextInputDialog(defaultValue);
		tiDialog.setHeaderText(null);
		tiDialog.setGraphic(null);
		tiDialog.setTitle(title);
		tiDialog.setContentText(contentText);
		Optional<String> result = tiDialog.showAndWait();
		if (result.isPresent()) {
			return result.get();
		} else {
			return null;
		}
	}

	static public void textFieldOnlyDigits(TextField tf) {
		tf.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					tf.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}

}
