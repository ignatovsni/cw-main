package com.cwsni.world.client.desktop.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.client.desktop.util.InternalInfoPane;
import com.cwsni.world.model.Army;
import com.cwsni.world.model.ComparisonTool;
import com.cwsni.world.model.Country;
import com.cwsni.world.model.Province;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@Component
@Scope("prototype")
public class GsProvArmiesInfoPane extends InternalInfoPane {

	private GameScene gameScene;

	public void init(GameScene gameScene) {
		this.gameScene = gameScene;
		Pane pane = createUI();
		init(getMessage("info.pane.prov.armies.title"), pane);
	}

	private Pane createUI() {
		GridPane grid = createDefaultGrid();
		setInfoRows(new ArrayList<>());
		return grid;
	}

	@Override
	public void refreshInfo() {
		clearPane();
		super.refreshInfo();
	}

	protected void refreshInfoInternal() {
		Province prov = gameScene.getSelectedProvince();
		Map<Integer, List<Army>> armies = prov.getArmies().stream()
				.collect(Collectors.groupingBy(a -> a.getCountry().getId()));
		int idx = 0;
		List<Army> localArmies = armies.get(prov.getCountryId());
		if (localArmies != null) {
			idx = showArmies(prov.getCountry().getName(), localArmies, idx, true);
		}
		for (Map.Entry<Integer, List<Army>> entry : armies.entrySet()) {
			Integer countryId = entry.getKey();
			if (!ComparisonTool.isEqual(countryId, prov.getCountryId())) {
				Country country = gameScene.getGame().findCountryById(countryId);
				idx = showArmies(country != null ? country.getName() : "id=" + countryId, armies.get(countryId), idx,
						false);
			}
		}
	}

	private int showArmies(String country, List<Army> armies, int idx, boolean isOwner) {
		GridPane grid = (GridPane) getInternalPane();
		addRow(getMessage("info.pane.prov.army.country")
				+ (isOwner ? getMessage("info.pane.prov.army.country.owner") : ""), grid, idx++, country);
		for (Army a : armies) {
			// addRow(getMessage("info.pane.prov.army.soldiers"), grid, idx++,
			// DataFormatter.formatLongNumber(a.getSoldiers()));
			RowValue row = addRow(DataFormatter.formatLongNumber(a.getSoldiers()), grid, idx++,
					a.getOrganisation() + "/" + a.getTraining() + "/" + a.getEquipment());
			row.setNameColumnTooltip(getMessage("info.pane.prov.army.soldiers"));
			row.setValueColumnTooltip(getMessage("info.pane.prov.army.effectiveness"));
		}
		RowValue row = addRow("=" + DataFormatter.formatLongNumber(armies.stream().mapToLong(Army::getSoldiers).sum()),
				grid, idx++, "");
		row.setNameColumnTooltip(getMessage("info.pane.prov.army.soldiers"));
		return idx;
	}

	@Override
	protected boolean hasDataForUser() {
		return gameScene.getSelectedProvince() != null && !gameScene.getSelectedProvince().getArmies().isEmpty();
	}

}
