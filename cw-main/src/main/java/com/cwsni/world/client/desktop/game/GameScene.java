package com.cwsni.world.client.desktop.game;

import com.cwsni.world.client.desktop.map.DWorldMap;
import com.cwsni.world.client.desktop.util.ZoomableScrollPane;
import com.cwsni.world.model.WorldMap;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class GameScene extends Scene {

	private ZoomableScrollPane mapPane;

	public GameScene(Parent root, ZoomableScrollPane mapPane) {
		super(root);
		this.mapPane = mapPane;
	}

	public void createTestMap() {
		DWorldMap worldMap = createTestMap(30, 30, 30);
		mapPane.setTarget(worldMap.getMapGroup());
	}
	
	
	private DWorldMap createTestMap(int rows, int columns, int provinceRadius) {
		WorldMap map = WorldMap.createMap(rows, columns, provinceRadius);
		DWorldMap dMap = DWorldMap.createDMap(map, provinceRadius); 
		return dMap;
	}

}
