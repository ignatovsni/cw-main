package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.game.ai.ScriptAIHandler;
import com.cwsni.world.model.data.Color;
import com.cwsni.world.model.engine.Country;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

@Component
@Scope("prototype")
public class CountriesPropertiesWindow extends Dialog<ButtonType> {

	public class RowCountry {
		int id;
		String name;
		Color color;
		String script;
	}

	@Autowired
	private LocaleMessageSource messageSource;

	@Autowired
	private ScriptAIHandler scriptAIHandler;

	private GridPane grid;
	private List<Integer> countryIds;
	private List<ColorPicker> countryСolors;
	private List<TextField> countryNames;
	private List<ComboBox<String>> countryScripts;
	private List<String> listOfScripts;

	private String getMessage(String code) {
		return messageSource.getMessage(code);
	}

	public void init(GameScene gameScene) {
		setTitle(getMessage("window.countries-settings.title"));
		ButtonType okButtonType = new ButtonType(getMessage("window.countries-settings.button.ok"), ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().add(okButtonType);
		ButtonType cancelButtonType = new ButtonType(getMessage("window.countries-settings.button.cancel"),
				ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().add(cancelButtonType);

		grid = createDefaultGrid();
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(grid);

		getDialogPane().setContent(scrollPane);
	}

	private GridPane createDefaultGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(0);
		grid.setPadding(new Insets(0, 10, 0, 10));

		ColumnConstraints column1 = new ColumnConstraints();
		column1.setHalignment(HPos.LEFT);
		column1.setMinWidth(30);
		column1.setMaxWidth(30);
		grid.getColumnConstraints().add(column1);

		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHalignment(HPos.LEFT);
		column2.setMinWidth(100);
		column2.setMaxWidth(100);
		grid.getColumnConstraints().add(column2);

		ColumnConstraints column3 = new ColumnConstraints();
		column3.setHalignment(HPos.LEFT);
		column3.setMinWidth(100);
		column3.setMaxWidth(100);
		grid.getColumnConstraints().add(column3);

		return grid;
	}

	public void reinit(List<Country> listCountries) {
		grid.getChildren().clear();
		int idx = 0;
		addRow(grid, idx++, new Label(getMessage("window.countries-settings.column.color")),
				new Label(getMessage("window.countries-settings.column.name")),
				new Label(getMessage("window.countries-settings.column.script")));
		listOfScripts = scriptAIHandler.getListOfAvailableScripts();
		countryIds = new ArrayList<>();
		countryСolors = new ArrayList<>();
		countryNames = new ArrayList<>();
		countryScripts = new ArrayList<>();
		for (Country c : listCountries) {
			addCountryRow(c, idx++);
		}
	}

	private void addRow(GridPane grid, int row, Node col1, Node col2, Node col3) {
		grid.add(col1, 0, row);
		grid.add(col2, 1, row);
		grid.add(col3, 2, row);
	}

	private void addCountryRow(Country c, int i) {
		ColorPicker countryColor = new ColorPicker();
		countryColor.setValue(new javafx.scene.paint.Color(c.getColor().getR() / 255.0, c.getColor().getG() / 255.0,
				c.getColor().getB() / 255.0, 1));
		countryColor.getStyleClass().add("button");
		countryColor.setStyle("-fx-color-label-visible: false ;");

		TextField countryName = new TextField();
		countryName.setText(c.getName());

		ComboBox<String> countryScript = new ComboBox<>();		
		listOfScripts.forEach(s -> countryScript.getItems().add(s));
		countryScript.setValue(c.getAiScriptName());

		countryIds.add(c.getId());
		countryСolors.add(countryColor);
		countryNames.add(countryName);
		countryScripts.add(countryScript);
		addRow(grid, i, countryColor, countryName, countryScript);
	}

	public List<RowCountry> getCountriesInfo() {
		List<RowCountry> countries = new ArrayList<>();
		for (int i = 0; i < countryIds.size(); i++) {
			RowCountry c = new RowCountry();
			c.id = countryIds.get(i);
			javafx.scene.paint.Color color = countryСolors.get(i).getValue();
			c.color = new Color((int) Math.round(color.getRed() * 255), (int) Math.round(color.getGreen() * 255),
					(int) Math.round(color.getBlue() * 255));
			c.name = countryNames.get(i).getText();
			c.script = countryScripts.get(i).getValue().toString();
			countries.add(c);
		}
		return countries;
	}

}
