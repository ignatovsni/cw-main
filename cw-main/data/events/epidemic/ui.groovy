import groovy.transform.Field;

@Field final NONE_COLOR = [(double)0.0, (double)0.0, (double)0.0, (double)1.0];
@Field final EPIPEMIC_COLOR = [(double)1.0, (double)0.0, (double)0.0, (double)1.0];
@Field final EPIPEMIC_PROTECTION_COLOR = [(double)0.0, (double)1.0, (double)0.0, (double)1.0];

def getTitleAndShortDescription(event, languageCode, target) {	
	def status = checkStatus(event);
	if (status == 1) {
		return [String.format(data.getMessage('event.epidemic.title'), event.info.contagiousness*100, event.info.deathRate*100), 
				String.format(data.getMessage('event.epidemic.description.short'), event.info.contagiousness*100, event.info.deathRate*100)];
	} else if (status == 2) {
		return [data.getMessage('event.epidemic-resistance.titlel'), 
				data.getMessage('event.epidemic-resistance.description.shortl')];
	} else {
		return ['You must not see this', 'If you see this, there is an error'];
	}
}

def getMapModes() {
	def modes = [:];
	modes['epidemic'] = [data.getMessage('map.mode.label'), data.getMessage('map.mode.description')];
	return modes;
}

def getProvinceColor(province, mode) {
	if (!province.getTerrainType().isPopulationPossible()) {
		return null;
	}
	def status = checkStatus(findEvent(province));
	if (status == 1) {
		return EPIPEMIC_COLOR;
	} else if (status == 2) {
		return EPIPEMIC_PROTECTION_COLOR;
	} else {
		return NONE_COLOR;
	}
}

def checkStatus(event) {
	if(event != null) {
	    return 1;
	} else if(1==2) {
	    return 2;
	} else {
		return 0;
	}	      
}

def findEvent(province) {
	def events = data.events.findEventsByThisType();
	for (event in events) {
		if (event.info.provinces.contains(province.id)) {
			return event;  
		}
	}
	return null;
}
