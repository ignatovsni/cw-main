package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.data.Color;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

@Component
@Scope("prototype")
public class CountriesPropertiesWindow extends Dialog {

	public class RowCountry {
		int id;
		String name;
		Color color;
	}

	@Autowired
	private LocaleMessageSource messageSource;

	private GridPane grid;
	private List<Integer> countryIds;
	private List<ColorPicker> countryСolors;
	private List<TextField> countryNames;

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
		column1.setMinWidth(90);
		column1.setMaxWidth(90);
		grid.getColumnConstraints().add(column1);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHalignment(HPos.LEFT);
		grid.getColumnConstraints().add(column2);
		return grid;
	}

	public void reinit(Game game) {
		grid.getChildren().clear();
		int idx = 0;
		addRow(grid, idx++, new Label(getMessage("window.countries-settings.column.color")),
				new Label(getMessage("window.countries-settings.column.name")));
		countryIds = new ArrayList<>();
		countryСolors = new ArrayList<>();
		countryNames = new ArrayList<>();
		for (Country c : game.getCountries()) {
			addCountryRow(c, idx++);
		}
	}

	private void addRow(GridPane grid, int row, Node col1, Node col2) {
		grid.add(col1, 0, row);
		grid.add(col2, 1, row);
	}

	private void addCountryRow(Country c, int i) {
		ColorPicker countryColor = new ColorPicker();
		countryColor.setValue(new javafx.scene.paint.Color(c.getColor().getR() / 255.0, c.getColor().getG() / 255.0,
				c.getColor().getB() / 255.0, 1));
		countryColor.getStyleClass().add("button");
		TextField countryName = new TextField();
		countryName.setText(c.getName());

		countryIds.add(c.getId());
		countryСolors.add(countryColor);
		countryNames.add(countryName);
		addRow(grid, i, countryColor, countryName);
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
			countries.add(c);
		}
		return countries;
	}

}
