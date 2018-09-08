import groovy.transform.Field;
import com.cwsni.world.model.engine.modifiers.*

// EVENT_TYPE is unique code for event.
// The application does not use it now, but can use in the future. Please, define it.
@Field final String EVENT_TYPE = 'GLOBAL_CLIMATE_CHANGE';

// Constants for events
@Field final double CLIMATE_CHANGE_PROBABILITY = 0.01;
@Field final double CLIMATE_CHANGE_STOP_PROBABILITY = 0.005;
@Field final double CLIMATE_CHANGE_BAD_PROBABILITY = 0.7;
@Field final double CLIMATE_CHANGE_STEP = 0.01;
@Field final double CLIMATE_CHANGE_MIN_STEP = 0.001;
@Field final double CLIMATE_IMPACT_MAX = 1.3;
@Field final double CLIMATE_IMPACT_MIN = 0.8;

// The application invokes this method each turn. 
def processNewTurn() {
	log 'processNewTurn ' + EVENT_TYPE;
	def events =  data.events.findEventsByThisType();
	if (events.isEmpty()) {
    	checkNewEvent();  
	} else {
    	// only one event can be active
    	events[0].markAsProcessed();
    	processExistingEvent(events[0]);
	}
}

// The application invokes this method after loading to restore the events impact (usualy to create modifiers).
def prepareGameAfterLoading() {
	log 'prepareGameAfterLoading ' + EVENT_TYPE;
	def events =  data.events.findEventsByThisType();
	if (!events.isEmpty()) {
		events[0].markAsProcessed();
    	activateEvent(events[0]);
	}       
}

// Just the example how you can invoke the method of the another script.
def externalInvocation(event, languageCode, target) {
	return data.scriptEventsWrapper.invoke('ui', 'getTitleAndShortDescription', [event, languageCode]);
}

def log(msg) {
	//println msg;
}

def checkNewEvent() {
	log 'checkNewEvent';
	if (data.rnd.nextDouble() > data.game.turn.probablilityPerYear(CLIMATE_CHANGE_PROBABILITY) ) {return null;}
	log 'creating new event';
	def event = data.events.createAndAddNewEvent();
	event.info.step = createRandomStep();
	event.info.effect = 1 + event.info.step;
	event.info.evolve = true;	
	activateEvent(event);	
	log 'created new event ' + event;
	return event;
}

def createRandomStep() {
	def step = Math.max(CLIMATE_CHANGE_MIN_STEP, data.rnd.nextDouble() * CLIMATE_CHANGE_STEP); 
	if (data.rnd.nextDouble() < CLIMATE_CHANGE_BAD_PROBABILITY) {
		step = - step;
	}
	return step;
}

def activateEvent(event) {
	def currentEffect = event.info.effect;
	data.game.map.provinces.stream().filter({p -> p.getTerrainType().isSoilPossible()}).forEach({p -> 
		data.events.addModifier(p, ProvinceModifier.SOIL_FERTILITY, ModifierType.MULTIPLY, 
								currentEffect, event)});
}

def updateModifiers(event) {
	def currentEffect = event.info.effect;
	event.provinceModifiers.entrySet().forEach({entry ->
			def province = entry.key;
			def modifiers = entry.value;
			modifiers.forEach({modifier -> province.modifiers.update(modifier, currentEffect)});
		});
}

def processExistingEvent(event) {
	log 'processExistingEvent';
	if (event.info.evolve && data.rnd.nextDouble() < data.game.turn.probablilityPerYear(CLIMATE_CHANGE_STOP_PROBABILITY)) {
		// start moving back to normal climate
		log 'start moving back to normal climate';
		event.info.evolve = false;
		event.info.step = Math.abs(createRandomStep());
		if (event.info.effect > 1) {
			event.info.step = - event.info.step;
		}
	}		
	event.info.effect = event.info.effect + data.game.turn.addPerYear(event.info.step);
	if (!event.info.evolve && (event.info.effect >= 1 && event.info.step >= 0 
							|| event.info.effect <= 1 && event.info.step <= 0 || event.info.step == 0 ) ) {
		removeEvent(event);
	} else {
		if (event.info.effect > CLIMATE_IMPACT_MAX) {
			event.info.effect =  CLIMATE_IMPACT_MAX;
		} else if (event.info.effect < CLIMATE_IMPACT_MIN) {
			event.info.effect = CLIMATE_IMPACT_MIN;
		}
		updateModifiers(event)
	}
	log event.info.effect
}

def removeEvent(event) {
	log 'removeEvent ' + event;
	event.removeFromGame();
}



