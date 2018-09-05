import groovy.transform.Field;
import com.cwsni.world.model.engine.modifiers.*

@Field final String EVENT_TYPE = 'GLOBAL_CLIMATE_CHANGE';
@Field final double climateChangeProbability = 0.01;
@Field final double climateChangeContinueProbability = 0.8;
@Field final double climateChangeBadProbability = 0.7;
@Field final double climateChangeStep = 0.01;
@Field final double climateMaxImpact = 1.3;
@Field final double climateMinImpact = 0.8;
@Field final int climateChangeDuration = 10;

def processNewTurn() {
	log 'processNewTurn ' + EVENT_TYPE;
	def events =  data.eventCollection.findEventsByType(EVENT_TYPE);
	if (events.isEmpty()) {
    	checkNewEvent();  
	} else {
    	def copyEvents = new ArrayList(events);
    	// only one event can be active
    	for(int i=1; i<copyEvents.size(); i++) {
			removeEvent(copyEvents[i]);
		}
    	processExistingEvent(copyEvents[0]);
	}
}

def prepareGameAfterLoading() {
	log 'prepareGameAfterLoading ' + EVENT_TYPE;
	def events =  data.eventCollection.findEventsByType(EVENT_TYPE);
	if (!events.isEmpty()) {
    	def copyEvents = new ArrayList(events);
    	// only one event can be active
    	for(int i=1; i<copyEvents.size(); i++) {
			removeEvent(copyEvents[i]);
		}
    	activateEvent(copyEvents[0]);
	}       
}

def log(msg) {
	//println msg;
}

def checkNewEvent() {
	log 'checkNewEvent';
	if (data.rnd.nextDouble() > data.game.turn.probablilityPerYear(climateChangeProbability) ) {return null;}
	log 'creating new event';
	def event = data.eventCollection.createAndAddNewEvent(EVENT_TYPE);
	event.data.endTurn = data.game.turn.calculateFutureTurnAfterYears(climateChangeDuration);
	event.data.step = data.rnd.nextDouble() < climateChangeBadProbability	
				? - climateChangeStep
				: + climateChangeStep;
	event.data.effect = 1 + data.game.turn.addPerYear(event.data.step);
	event.data.evolve = true;	
	activateEvent(event);	
	log 'created new event ' + event;
	return event;
}

def activateEvent(event) {
	data.game.map.provinces.stream().filter({p -> p.getTerrainType().isSoilPossible()}).forEach({p -> p.modifiers.add(
		Modifier.createModifierByEvent(ProvinceModifier.SOIL_FERTILITY, ModifierType.MULTIPLY, event.data.effect, event))});
}

def updateModifiers(event) {
	data.game.map.provinces.stream().filter({p -> p.getTerrainType().isSoilPossible()}).forEach(
		{ p -> 
			def modifiers = p.modifiers.findByEvent(event);
			p.modifiers.update(modifiers[0], event.data.effect);
		});
}

def processExistingEvent(event) {
	log 'processExistingEvent';
	if (event.data.endTurn < data.game.turn.dateTurn) {
		if (event.data.evolve && data.rnd.nextDouble() < data.game.turn.probablilityPerYear(climateChangeContinueProbability)) {
			// start moving back to normal climate
			log 'start moving back to normal climate';
			event.data.evolve = false;
			event.data.step = event.data.effect > 1	? - climateChangeStep : climateChangeStep;
		}
		event.data.endTurn = data.game.turn.calculateFutureTurnAfterYears(climateChangeDuration);
	}
	event.data.effect = event.data.effect + data.game.turn.addPerYear(event.data.step);
	if (!event.data.evolve && (event.data.effect >= 1 && event.data.step >= 0 
							|| event.data.effect <= 1 && event.data.step <= 0 || event.data.step == 0 ) ) {
		removeEvent(event);
	} else {
		if (event.data.effect > climateMaxImpact) {
			event.data.effect =  climateMaxImpact;
		} else if (event.data.effect < climateMinImpact) {
			event.data.effect = climateMinImpact;
		}
		updateModifiers(event)
	}
	log event.data.effect
}

def removeEvent(event) {
	log 'removeEvent ' + event;
	data.game.map.provinces.stream().filter({p -> p.getTerrainType().isSoilPossible()}).forEach(
		{ p -> p.modifiers.removeByEvent(event) });
	data.eventCollection.removeEvent(event);	
}

