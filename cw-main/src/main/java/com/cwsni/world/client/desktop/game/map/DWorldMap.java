package com.cwsni.world.client.desktop.game.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.ProvinceBorder;
import com.cwsni.world.model.WorldMap;

import javafx.scene.Group;
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
	private MapMode mapMode = MapMode.GEO;

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

	void mouseClickOnProvince(DProvince dProvince, MouseEvent e) {
		if (e.getButton() == MouseButton.PRIMARY) {
			// selectProvince(dProvince) can be removed, because it is invoking by
			// GameScene::selectProvince
			// But I keep it for consistency
			selectProvince(dProvince);
			gameScene.selectProvince(dProvince.getProvince());
		}
	}

	public void selectProvince(Integer provId) {
		selectProvince(provincesById.get(provId));
	}

	private void selectProvince(DProvince dProvince) {
		if (selectedProvince == dProvince) {
			return;
		}
		if (selectedProvince != null) {
			selectedProvince.selectProvince(false);
			// refresh borders because polygon.stroke can overlap them
			selectedProvince.getProvince().getNeighbors()
					.forEach(n -> provincesById.get(n.getId()).drawCountryBorder());
		}
		this.selectedProvince = dProvince;
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

	public MapMode getMapMode() {
		return mapMode;
	}

	public void setMapModeAndRedraw(MapMode mapMode) {
		this.mapMode = mapMode;
		provinces.forEach(p -> p.draw());
		drawCountryBorders();
	}

	private void drawCountryBorders() {
		provinces.forEach(p -> p.resetCountriesBorders());
		Set<ProvinceBorder> countriesBorders = game.getMap().getCountriesBorders();
		countriesBorders.forEach(pb -> {
			provincesById.get(pb.getFirst()).addCountryBorderWith(pb.getSecond());	
			provincesById.get(pb.getSecond()).addCountryBorderWith(pb.getFirst());
		});
		provinces.forEach(p -> p.drawCountryBorder());
	}

}
