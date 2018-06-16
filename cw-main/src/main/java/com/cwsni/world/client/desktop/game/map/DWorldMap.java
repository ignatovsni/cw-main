package com.cwsni.world.client.desktop.game.map;

import java.util.ArrayList;
import java.util.List;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.model.Game;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.WorldMap;

import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class DWorldMap {
	
	private List<DProvince> provinces;
	private Game game;
	private Group mapGroup;
	private DProvince selectedProvince;
	private GameScene gameScene;
	private MapMode mapMode = MapMode.GEO;
	
	private DWorldMap(Game game, MapMode mapMode) {
		this.game = game;
		this.mapMode = mapMode;
		fillMap(game.getMap());
	}

	private void fillMap(WorldMap map) {
		List<Province> pvs = map.getProvinces();
		provinces = new ArrayList<>(pvs.size());
		pvs.forEach(p -> provinces.add(DProvince.createDProvince(this, p, game.getMap().getProvinceRadius())));
		mapGroup = new Group();
		mapGroup.getChildren().addAll(provinces);
	}

	public static DWorldMap createDMap(Game game, MapMode mapMode) {
		return new DWorldMap(game, mapMode);
	}
	
	public Group getMapGroup() {
		return mapGroup;
	}

	public void mouseClickOnProvince(DProvince dProvince, MouseEvent e) {
		if (e.getButton() == MouseButton.PRIMARY) {
			selectProvince(dProvince);
		}
	}

	private void selectProvince(DProvince dProvince) {
		if (selectedProvince != null) {
			selectedProvince.selectProvince(false);
		}
		this.selectedProvince = dProvince;
		if (selectedProvince != null) {
			selectedProvince.selectProvince(true);
		}
		gameScene.selectProvince(dProvince.getProvince());
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
		provinces.forEach(p -> p.reDraw());
	}
	

}
