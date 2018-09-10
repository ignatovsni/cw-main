package com.cwsni.world.client.desktop.game.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.client.desktop.util.DialogUtil;
import com.cwsni.world.game.events.GameEventHandler;
import com.cwsni.world.model.engine.Country;
import com.cwsni.world.model.engine.Game;
import com.cwsni.world.model.engine.Province;
import com.cwsni.world.model.engine.ProvinceBorder;
import com.cwsni.world.model.engine.WorldMap;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;

public class DWorldMap {

	private List<DProvince> provinces;
	private Map<Integer, DProvince> provincesById;
	private Game game;
	private Group mapGroup;
	private DProvince selectedProvince;
	private GameScene gameScene;
	private MapMode mapMode = MapMode.DEFAULT_MODE;
	private Map<ProvinceBorder, Node> cacheBorderLines;
	private Map<ProvinceBorder, Node> activeCountriesBorderLines;

	private Map<String, ImagePattern> textures;

	private DWorldMap(Game game, MapMode mapMode) {
		this.game = game;
		this.mapMode = mapMode;
		textures = new HashMap<>();
		fillMap(game.getMap());
	}

	private void fillMap(WorldMap map) {
		List<Province> pvs = map.getProvinces();
		provinces = new ArrayList<>(pvs.size());
		provincesById = new HashMap<>(pvs.size());
		cacheBorderLines = new HashMap<>();
		activeCountriesBorderLines = new HashMap<>();
		pvs.forEach(p -> {
			DProvince dProvince = DProvince.createDProvince(this, p, game.getGameParams().getProvinceRadius());
			provinces.add(dProvince);
			provincesById.put(p.getId(), dProvince);
		});
		mapGroup = new Group();
		mapGroup.getChildren().addAll(provinces);
		drawCountryBorders();
	}

	public static DWorldMap createDMap(Game game, MapMode mapMode) {
		return new DWorldMap(game, mapMode);
	}

	public Group getMapGroup() {
		return mapGroup;
	}

	Map<String, ImagePattern> getTextures() {
		return textures;
	}

	Province getSelectedProvince() {
		if (gameScene != null) {
			return gameScene.getSelectedProvince();
		} else {
			return null;
		}
	}

	private String getMessage(String code) {
		return gameScene.getMessageSource().getMessage(code);
	}

	void mouseClickOnProvince(DProvince dProvince, MouseEvent e) {
		if (e.getButton() == MouseButton.PRIMARY) {
			// selectProvince(dProvince) can be removed, because it is invoking by
			// GameScene::selectProvince
			// But I keep it for consistency
			selectProvince(dProvince);
			gameScene.selectProvince(dProvince.getProvince());
		} else if (e.getButton() == MouseButton.SECONDARY) {
			Province prov = dProvince.getProvince();
			Country country = prov.getCountry();
			ContextMenu cm = new ContextMenu();

			MenuItem provinceRenameItem = new MenuItem(getMessage("map.province.context-menu.province.rename"));
			provinceRenameItem.setOnAction(event -> {
				// gameScene.getScriptAIHandler().getListOfAvailableScripts();
				String newValue = DialogUtil.showTextInputDialog(provinceRenameItem.getText(),
						getMessage("data-model.province.name.full"), prov.getName());
				if (newValue != null) {
					prov.setName(newValue);
				}
			});
			cm.getItems().add(provinceRenameItem);

			if (country != null) {
				MenuItem countryRenameItem = new MenuItem(getMessage("map.province.context-menu.country.settings"));
				countryRenameItem.setOnAction(event -> {
					gameScene.editCountriesSettings(country);
				});
				SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
				cm.getItems().addAll(separatorMenuItem, countryRenameItem);
			}

			cm.show(dProvince, e.getScreenX(), e.getScreenY());
		}
	}

	public void selectProvince(Integer provId) {
		selectProvince(provincesById.get(provId));
		if (MapModeEnum.DIPLOMACY.equals(mapMode.getMode()) || MapModeEnum.REACHABLE_LANDS.equals(mapMode.getMode())) {
			setMapModeAndRedraw(mapMode);
		}
	}

	private void selectProvince(DProvince newSelectedProvince) {
		if (selectedProvince == newSelectedProvince) {
			return;
		}
		if (selectedProvince != null) {
			selectedProvince.selectProvince(false);
			// refresh borders because polygon.stroke can overlap them
			bordersToFront(selectedProvince.getProvince());
		}
		this.selectedProvince = newSelectedProvince;
		if (selectedProvince != null) {
			selectedProvince.selectProvince(true);
		}
	}

	public void setGameScene(GameScene gameScene) {
		this.gameScene = gameScene;
	}

	public Game getGame() {
		return game;
	}

	public GameEventHandler getGameEventHandler() {
		return gameScene.getGameEventHandler();
	}

	public MapMode getMapMode() {
		return mapMode;
	}

	public void setMapModeAndRedraw(MapMode mapMode) {
		this.mapMode = mapMode;
		provinces.forEach(p -> p.draw());
		drawCountryBorders();
	}

	private void drawCountryBorders() {
		Map<ProvinceBorder, Node> newLines = new HashMap<>();
		Set<ProvinceBorder> countriesBorders = game.getMap().getCountriesBorders();
		for (ProvinceBorder border : countriesBorders) {
			Node line = cacheBorderLines.get(border);
			if (line == null) {
				DProvince p1 = provincesById.get(border.getFirst());
				DProvince p2 = provincesById.get(border.getSecond());
				line = p2.createBorderLine(p1.getProvince());
				cacheBorderLines.put(border, line);
			}
			newLines.put(border, line);
		}
		Iterator<Entry<ProvinceBorder, Node>> iter = activeCountriesBorderLines.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<ProvinceBorder, Node> entry = iter.next();
			if (!newLines.keySet().contains(entry.getKey())) {
				iter.remove();
				mapGroup.getChildren().remove(entry.getValue());
			}
		}
		iter = newLines.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<ProvinceBorder, Node> entry = iter.next();
			if (!activeCountriesBorderLines.keySet().contains(entry.getKey())) {
				mapGroup.getChildren().add(entry.getValue());
				activeCountriesBorderLines.put(entry.getKey(), entry.getValue());
			}
		}
	}

	private void bordersToFront(Province p) {
		for (Province n : p.getNeighbors()) {
			Node border = activeCountriesBorderLines.get(new ProvinceBorder(p.getId(), n.getId()));
			if (border != null) {
				border.toFront();
			}
		}
	}

	public DProvince findProvinceById(int id) {
		return provincesById.get(id);
	}

}
