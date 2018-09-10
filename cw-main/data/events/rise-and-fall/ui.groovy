import groovy.transform.Field;

@Field final NONE_COLOR = [(double)0.0, (double)0.0, (double)0.0, (double)1.0];
@Field final HIGH_POWER_COLOR = [(double)1.0, (double)0.0, (double)0.0, (double)1.0];
@Field final LOW_POWER_COLOR = [(double)0.0, (double)0.8, (double)0.8, (double)1.0];

def getTitleAndShortDescription(event, languageCode, target) {
	return ['', ''];
}

def getMapModes() {
	def modes = [:];
	modes['power'] = [data.getMessage('map.mode.label'), data.getMessage('map.mode.description')];
	return modes;
}

def getProvinceColor(province, mode) {
	if (!province.getTerrainType().isPopulationPossible()) {
		return null;
	}
	if (province.country == null) {
		return NONE_COLOR;
	}
	def status = checkStatus(findEvent(province.country));
	if (status == 1) {
		return HIGH_POWER_COLOR;
	} else if (status == 2) {
		return LOW_POWER_COLOR;
	} else {
		return NONE_COLOR;
	}
}

def checkStatus(event) {
	if (event == null) {
		return 0;
	} else if (event.info.effect > 1) {
	    return 1;
	} else if (event.info.effect < 1) {
	    return 2;
	} else {
		return 0;
	}	      
}

def findEvent(country) {
	def events = data.events.findEventsByThisType();
	for (event in events) {
		if (event.info.country == country.id) {
			return event;  
		}
	}
	return null;
}