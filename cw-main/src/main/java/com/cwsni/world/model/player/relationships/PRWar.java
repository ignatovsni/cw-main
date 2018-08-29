package com.cwsni.world.model.player.relationships;

import com.cwsni.world.model.engine.relationships.RWar;
import com.cwsni.world.model.player.interfaces.IPRWar;

public class PRWar extends PRBaseAgreement implements IPRWar {

	public PRWar(RWar war) {
		super(war);
	}

}
