package com.cwsni.world.client.desktop.game.map;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.cwsni.world.client.desktop.game.GameScene;
import com.cwsni.world.model.Province;
import com.cwsni.world.model.WorldMap;

import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class DWorldMap {
	
	private List<DProvince> provinces;
	private WorldMap map;
	private double provinceRadius;
	private Group mapGroup;
	private DProvince selectedProvince;
	private GameScene gameScene;
	
	private DWorldMap(WorldMap map, double provinceRadius) {
		this.map = map;
		this.provinceRadius = provinceRadius;
		fillMap(map);
	}

	private void fillMap(WorldMap map) {
		Stream<Province> pvs = map.getProvinces();
		provinces = new ArrayList<>(map.getNumberOfProvinces());
		pvs.forEach(p -> provinces.add(DProvince.createDProvince(this, p, provinceRadius)));
		mapGroup = new Group();
		mapGroup.getChildren().addAll(provinces);
	}

	public static DWorldMap createDMap(WorldMap map, double provinceRadius) {
		return new DWorldMap(map, provinceRadius);
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

}
