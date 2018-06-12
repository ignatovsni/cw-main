package com.cwsni.world.client.desktop.map;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.cwsni.world.model.Province;
import com.cwsni.world.model.WorldMap;

import javafx.scene.Group;

public class DWorldMap {
	
	private List<DProvince> provinces;
	private WorldMap map;
	private double provinceRadius;
	private Group mapGroup;
	
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

}
