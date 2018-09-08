import groovy.transform.Field;
import com.cwsni.world.model.engine.modifiers.*

@Field final String EVENT_TYPE = 'EPIDEMIC';
@Field final double eventEpidemicProbability = 0.01;
@Field final double eventEpidemicStopMinProbability = 0.1;
@Field final double eventEpidemicStopMaxProbability = 0.9;
@Field final double eventEpidemicContagiousness = 0.5;
@Field final double eventEpidemicDeathRate = 0.4;
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

def log(msg) {
	println msg;
}

def checkNewEvent() {
	log 'checkNewEvent';
	//return;
	//if (data.rnd.nextDouble() > data.game.turn.probablilityPerYear(eventEpidemicProbability) ) {return null;}
	def core = null;
	def counter = 0;
	while (counter++ < 1000 && (core == null || core.populationAmount == 0)) {
		core = data.game.map.findProvinceById(data.rnd.nextInt(data.game.map.provinces.size()));
	}
	if (core==null) {
		return;
	}
	log 'core: ' + core;
	log 'creating new event';
	def event = data.events.createAndAddNewEvent();
	event.info.contagiousness = data.rnd.nextDouble() * eventEpidemicContagiousness;
	event.info.deathRate = data.rnd.nextDouble() * eventEpidemicDeathRate;
	event.info.stopProbability = eventEpidemicStopMinProbability 
			+ data.rnd.nextDouble() * (eventEpidemicStopMaxProbability - eventEpidemicStopMinProbability);
	event.info.provinces = [];
	event.info.provinces << core.id;
	activateEvent(event);	
	log 'created new event ' + event;
	return event;
}

def activateEvent(event) {
	def currentEffect = data.game.turn.multiplyPerYear(1 - event.info.deathRate);
	event.info.provinces.each {pId ->
		p = data.game.map.findProvinceById(pId); 
		data.events.addModifier(p, ProvinceModifier.POPULATION_AMOUNT, ModifierType.MULTIPLY, currentEffect, event)
	};
}

def updateModifiers(event) {
}

def processExistingEvent(event) {
	log 'processExistingEvent';
	
	// check expiring
	def stopProbability = data.game.turn.probablilityPerYear(event.info.stopProbability);
	def iter = event.info.provinces.iterator();	
	while (iter.hasNext()) {
		def provinceInfo = iter.next();
		if (data.rnd.nextDouble() < stopProbability) {
			iter.remove();
			// TODO remove modifiers from the province
			// TODO add temporary resistance to this province
		}
	}
	// TODO check spreading to neighbors

	if (event.info.provinces.isEmpty()) {
		removeEvent(event);
	}
}

def removeEvent(event) {
	log 'removeEvent ' + event;
	event.removeFromGame();
}



