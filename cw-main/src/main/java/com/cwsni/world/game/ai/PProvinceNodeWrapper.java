package com.cwsni.world.game.ai;

import java.util.Collection;
import java.util.stream.Collectors;

import com.cwsni.world.model.player.interfaces.IPProvince;
import com.cwsni.world.services.algorithms.Node;

public class PProvinceNodeWrapper extends Node {

	private IPProvince p;

	PProvinceNodeWrapper(IPProvince p) {
		this.p = p;
	}

	@Override
	public Object getKey() {
		return p.getId();
	}

	@Override
	public Collection<Node> getNeighbors() {
		return p.getNeighbors().stream().map(n -> new PProvinceNodeWrapper(n)).collect(Collectors.toList());
	}

}
