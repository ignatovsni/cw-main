import groovy.transform.Field;
import com.cwsni.world.model.engine.modifiers.*

// EVENT_TYPE is unique code for event.
// The application does not use it now, but can use in the future. Please, define it.
@Field final String EVENT_TYPE = 'GLOBAL_CLIMATE_CHANGE';

// Constants for events
@Field final double climateChangeProbability = 0.01;
@Field final double climateChangeStopProbability = 0.005;
@Field final double climateChangeBadProbability = 0.7;
@Field final double climateChangeStep = 0.01;
@Field final double climateChangeMinStep = 0.001;
@Field final double climateMaxImpact = 1.3;
@Field final double climateMinImpact = 0.8;
@Field final int climateChangeDuration = 10;

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
	if (data.rnd.nextDouble() > data.game.turn.probablilityPerYear(climateChangeProbability) ) {return null;}
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
	def step = Math.max(climateChangeMinStep, data.rnd.nextDouble() * climateChangeStep); 
	if (data.rnd.nextDouble() < climateChangeBadProbability) {
		step = - step;
	}
	return step;
}

def activateEvent(event) {
	def currentEffect = 1 + data.game.turn.addPerYear(event.info.effect - 1);
	data.game.map.provinces.stream().filter({p -> p.getTerrainType().isSoilPossible()}).forEach({p -> 
		data.events.addModifier(p, ProvinceModifier.SOIL_FERTILITY, ModifierType.MULTIPLY, 
								currentEffect, event)});
}

def updateModifiers(event) {
	def currentEffect = 1 + data.game.turn.addPerYear(event.info.effect - 1);
	event.provinceModifiers.entrySet().forEach({entry ->
			def province = entry.key;
			def modifiers = entry.value;
			modifiers.forEach({modifier -> province.modifiers.update(modifier, currentEffect)});
		});
}

def processExistingEvent(event) {
	log 'processExistingEvent';
	if (event.info.evolve && data.rnd.nextDouble() < data.game.turn.probablilityPerYear(climateChangeStopProbability)) {
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
		if (event.info.effect > climateMaxImpact) {
			event.info.effect =  climateMaxImpact;
		} else if (event.info.effect < climateMinImpact) {
			event.info.effect = climateMinImpact;
		}
		updateModifiers(event)
	}
	log event.info.effect
}

def removeEvent(event) {
	log 'removeEvent ' + event;
	event.removeFromGame();
}



