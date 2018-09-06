import groovy.transform.Field;
import com.cwsni.world.model.engine.modifiers.*

@Field final String EVENT_TYPE = 'EPIDEMIC';
@Field final double eventEpidemicProbability = 0.01;
@Field final double eventEpidemicContagiousness = 0.5;
@Field final double eventEpidemicDeathRate = 0.4;
@Field final int eventEpidemicDuration = 10;
@Field final int eventEpidemicProtectionDuration = 40;

def processNewTurn() {
	log 'processNewTurn ' + EVENT_TYPE;
	def events =  data.events.findEventsByThisType();
	if (events.isEmpty()) {
    	checkNewEvent();  
	} else {
		events.each { event ->    	
    		event.markAsProcessed();
    		processExistingEvent(event);
    	}
	}
}

def prepareGameAfterLoading() {
	log 'prepareGameAfterLoading ' + EVENT_TYPE;
	def events =  data.events.findEventsByThisType();
	if (!events.isEmpty()) {
    	events.each { event ->    	
    		event.markAsProcessed();
    		activateEvent(event);
    	}
	}       
}

def getTitleAndShortDescription(event, languageCode, target) {	
	return ['111', '222'];
}

def log(msg) {
	println msg;
}

def checkNewEvent() {
	log 'checkNewEvent';
	//if (data.rnd.nextDouble() > data.game.turn.probablilityPerYear(eventEpidemicProbability) ) {return null;}
	def core = null;
	def counter = 0;
	while (counter++ < 1000 && (core == null || core.populationAmount == 0)) {
		core = data.game.map.findProvById(data.rnd.nextInt(data.game.map.provinces.size()));
	}
	if (core==null) {
		return;
	}
	log 'core: ' + core;
	log 'creating new event';
	def event = data.events.createAndAddNewEvent();
	event.info.contagiousness = data.rnd.nextDouble() * eventEpidemicContagiousness;
	event.info.deathRate = data.rnd.nextDouble() * eventEpidemicDeathRate;
	event.info.provinces = [];
	def provinceInfo = [:] as HashMap;
	provinceInfo[core.id] = data.game.turn.calculateFutureTurnAfterYears(data.rnd.nextInt(eventEpidemicDuration));
	event.info.provinces << provinceInfo;
	activateEvent(event);	
	log 'created new event ' + event;
	return event;
}

def activateEvent(event) {
	event.info.provinces.entrySet().each {entry ->
		data.events.addModifier(p, ProvinceModifier.SOIL_FERTILITY, ModifierType.MULTIPLY, event.info.effect, event);
	}
}

def updateModifiers(event) {
}

def processExistingEvent(event) {
	log 'processExistingEvent';
	log event.info.effect
}

def removeEvent(event) {
	log 'removeEvent ' + event;
	event.removeFromGame();
}



