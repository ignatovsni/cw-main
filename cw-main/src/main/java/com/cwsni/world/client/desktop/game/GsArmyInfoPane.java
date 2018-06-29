package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Province;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsArmyInfoPane extends InternalInfoPane {

	private GameScene gameScene;

	private RowValue valuesNameLabel;
	private RowValue valuesProvincesLabel;
	private RowValue valuesPopulationLabel;
	private RowValue valuesArmiesLabel;
	private RowValue valuesArmiesSoldiersLabel;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.country.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();
		setInfoRows(new ArrayList<>());

		int idx = 0;
		valuesNameLabel = add2AllRows("info.pane.country.name", grid, idx++);
		valuesProvincesLabel = add2AllRows("info.pane.country.provinces", grid, idx++);
		valuesPopulationLabel = add2AllRows("info.pane.country.population", grid, idx++);
		valuesArmiesLabel = add2AllRows("info.pane.country.armies", grid, idx++);
		valuesArmiesSoldiersLabel = add2AllRows("info.pane.country.armies.soldiers", grid, idx++);

		return grid;
	}

	protected void refreshInfoInternal() {
		Province prov = gameScene.getSelectedProvince();
		Country country = prov.getCountry();
		setLabelText(valuesNameLabel, country.getName());
		setLabelText(valuesProvincesLabel, DataFormatter.toLong(country.getProvinces().size()));
		setLabelText(valuesPopulationLabel,
				DataFormatter.toLong(country.getProvinces().stream().mapToLong(p -> p.getPopulationAmount()).sum()));
		setLabelText(valuesArmiesLabel, DataFormatter.toLong(country.getArmies().size()));
		setLabelText(valuesArmiesSoldiersLabel,
				DataFormatter.toLong(country.getArmies().stream().mapToLong(a -> a.getSoldiers()).sum()));
	}

	@Override
	protected boolean hasDataForUser() {
		// TODO Auto-generated method stub
		return false;
	}

}
