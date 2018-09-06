import groovy.transform.Field;
import com.cwsni.world.model.engine.modifiers.*

// EVENT_TYPE is unique code for event.
// The application does not use it now, but can use in the future. Please, define it.
@Field final String EVENT_TYPE = 'GLOBAL_CLIMATE_CHANGE';

// Constants for events
@Field final double climateChangeProbability = 0.01;
@Field final double climateChangeContinueProbability = 0.8;
@Field final double climateChangeBadProbability = 0.7;
@Field final double climateChangeStep = 0.01;
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
// The application invokes this method to get description for the user.
// Must return the list with 2 elements: [title, short description].
// target - the object for which was invoked this method (e.g. the province with the modifiers of this event).
def getTitleAndShortDescription(event, languageCode, target) {
	// Just the example how you can invoke methods of the another script.
	return data.scriptEventsWrapper.invoke('ui_description', 'getTitleAndShortDescription', [event, languageCode]);
}

def log(msg) {
	//println msg;
}

def checkNewEvent() {
	log 'checkNewEvent';
	if (data.rnd.nextDouble() > data.game.turn.probablilityPerYear(climateChangeProbability) ) {return null;}
	log 'creating new event';
	def event = data.events.createAndAddNewEvent();
	event.info.endTurn = data.game.turn.calculateFutureTurnAfterYears(climateChangeDuration);
	event.info.step = data.rnd.nextDouble() < climateChangeBadProbability	
				? - climateChangeStep
				: + climateChangeStep;
	event.info.effect = 1 + data.game.turn.addPerYear(event.info.step);
	event.info.evolve = true;	
	activateEvent(event);	
	log 'created new event ' + event;
	return event;
}

def activateEvent(event) {
	data.game.map.provinces.stream().filter({p -> p.getTerrainType().isSoilPossible()}).forEach({p -> 
		data.events.addModifier(p, ProvinceModifier.SOIL_FERTILITY, ModifierType.MULTIPLY, event.info.effect, event)});
}

def updateModifiers(event) {
	event.provinceModifiers.entrySet().forEach({entry ->
			def province = entry.key;
			def modifiers = entry.value;
			modifiers.forEach({modifier -> province.modifiers.update(modifier, event.info.effect)});
		});
}

def processExistingEvent(event) {
	log 'processExistingEvent';
	if (event.info.endTurn < data.game.turn.dateTurn) {
		if (event.info.evolve && data.rnd.nextDouble() < data.game.turn.probablilityPerYear(climateChangeContinueProbability)) {
			// start moving back to normal climate
			log 'start moving back to normal climate';
			event.info.evolve = false;
			event.info.step = event.info.effect > 1	? - climateChangeStep : climateChangeStep;
		}
		event.info.endTurn = data.game.turn.calculateFutureTurnAfterYears(climateChangeDuration);
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



