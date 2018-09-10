import groovy.transform.Field;
import com.cwsni.world.model.engine.modifiers.*

@Field final String EVENT_TYPE = 'RISE_AND_FALL';
@Field final double BASE_PROBABILITY = 0.0001;
@Field final double NEW_COUNTRY_PROBABILITY = 0.01;
@Field final double RESTORED_COUNTRY_PROBABILITY = 0.02;
@Field final double STOP_PROBABILITY = 0.001;
@Field final double MIN_GOAL = 0.8;
@Field final double MAX_GOAL = 3;
@Field final double MIN_STEP = 0.0001;
@Field final double MAX_STEP = 0.01;

def processNewTurn() {
	log 'processNewTurn ' + EVENT_TYPE;
	def events = new ArrayList(data.events.findEventsByThisType());
	def usedCountriesIds = [] as Set;
	events.each { event ->    	
		event.markAsProcessed();
		usedCountriesIds << event.info.country;
		processExistingEvent(event);
	}
	checkNewEvents(usedCountriesIds);  
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
	//println msg;
}

def checkNewEvents(usedCountriesIds) {
	log 'checkNewEvent';
	//return;
	double baseProbability = data.game.turn.probablilityPerYear(BASE_PROBABILITY);
	double newCountryProbability = data.game.turn.probablilityPerYear(NEW_COUNTRY_PROBABILITY);
	double restoredCountryProbability = data.game.turn.probablilityPerYear(RESTORED_COUNTRY_PROBABILITY);
	data.game.countries.stream().filter({c -> !usedCountriesIds.contains(c.id)}).forEach({ country ->		
		if(country.turnOfCreation >= data.game.turn.dateTurn-1 && data.rnd.nextDouble() < newCountryProbability) {
			log "newCountryProbability ${newCountryProbability}";
			createNewBaseEvent(country);
		} else if(country.turnOfRestoring >= data.game.turn.dateTurn-1 && data.rnd.nextDouble() < restoredCountryProbability) {
			log "restoredCountryProbability ${restoredCountryProbability}";
			createNewBaseEvent(country);
		} else if(data.rnd.nextDouble() < baseProbability) {
			log "baseProbability ${baseProbability}";
			createNewBaseEvent(country);
		}
	});
}

def createNewBaseEvent(country) {
	log 'creating new event';
	def event = data.events.createAndAddNewEvent();

	event.info.country = country.id;
	event.info.evolve = true;
	event.info.effect = 1;
	event.info.goal = data.rnd.nextDouble(MIN_GOAL, MAX_GOAL);
	event.info.step = createRandomStep();
	if (event.info.goal < 1) {
		event.info.step = - event.info.step;  
	}	
	
	event.info.focus = event.info.effect;
	event.info.army = event.info.effect;
	event.info.government_influence_distance = event.info.effect;
	event.info.government_influence = event.info.effect;
	event.info.loyalty = event.info.effect;
	event.info.tax_effectiveness = event.info.effect;	

	activateEvent(event);
	log 'created new event ' + event;
	return event;
}

def createRandomStep() {
	return data.rnd.nextDouble(MIN_STEP, MAX_STEP);
}

def activateEvent(event) {
	def country = data.game.findCountryById(event.info.country);
	if (country == null) {
		return;
	}
	if (event.info.focus != null) {
		data.events.addModifier(country, CountryModifier.FOCUS, ModifierType.MULTIPLY, event.info.focus, event);
	}
	if (event.info.army != null) {
		data.events.addModifier(country, CountryModifier.ARMY_EFFECTIVENESS, ModifierType.MULTIPLY, event.info.army, event);
	}
	if (event.info.government_influence_distance != null) {
		data.events.addModifier(country, CountryModifier.GOVERNMENT_INFLUENCE_DISTANCE, ModifierType.MULTIPLY, event.info.government_influence_distance, event);
	}
	if (event.info.government_influence != null) {
		data.events.addModifier(country, CountryModifier.PROVINCE_GOVERNMENT_INFLUENCE, ModifierType.MULTIPLY, event.info.government_influence, event);
	}
	if (event.info.loyalty != null) {
		data.events.addModifier(country, CountryModifier.PROVINCE_LOYALTY, ModifierType.MULTIPLY, event.info.loyalty, event);
	}
	if (event.info.tax_effectiveness != null) {
		data.events.addModifier(country, CountryModifier.PROVINCE_TAX_EFFECTIVENESS, ModifierType.MULTIPLY, event.info.tax_effectiveness, event);
	}
}

def updateModifiers(event, country) {
	country.modifiers.findByEvent(event).each {modifier ->
			switch(modifier.feature) {
		    case CountryModifier.FOCUS:
		    	country.modifiers.update(modifier, event.info.focus);
		    	break;
	    	case CountryModifier.ARMY_EFFECTIVENESS:
		    	country.modifiers.update(modifier, event.info.army);
		    	break;
	    	case CountryModifier.GOVERNMENT_INFLUENCE_DISTANCE:
		    	country.modifiers.update(modifier, event.info.government_influence_distance);
		    	break;
	    	case CountryModifier.PROVINCE_GOVERNMENT_INFLUENCE:
		    	country.modifiers.update(modifier, event.info.government_influence);
		    	break;
	    	case CountryModifier.PROVINCE_LOYALTY:
		    	country.modifiers.update(modifier, event.info.loyalty);
		    	break;
	    	case CountryModifier.PROVINCE_TAX_EFFECTIVENESS:
		    	country.modifiers.update(modifier, event.info.tax_effectiveness);
		    	break;
			}
		};
}

def processExistingEvent(event) {
	//log 'processExistingEvent';
	def country = data.game.findCountryById(event.info.country);
	if (country == null) {
		// the country could be destroyed
		removeEvent(event);
		return;
	}
	if (event.info.evolve 
		&& (data.rnd.nextDouble() < data.game.turn.probablilityPerYear(STOP_PROBABILITY))
			|| (event.info.effect >= event.info.goal && event.info.step >= 0)
			|| (event.info.effect <= event.info.goal && event.info.step <= 0)) {
		// start moving back to normal power
		log 'start moving back to normal power';
		event.info.evolve = false;
		event.info.step = Math.abs(createRandomStep());
		if (event.info.effect > 1) {
			event.info.step = - event.info.step;
		}
	}
	double step = data.game.turn.addPerYear(event.info.step);
	event.info.effect += step;
	if (!event.info.evolve && (event.info.effect >= 1 && step >= 0 
							|| event.info.effect <= 1 && step <= 0 || step == 0 ) ) {
		removeEvent(event);
	} else {
		if (event.info.effect > MAX_GOAL) {
			event.info.effect =  MAX_GOAL;
			step = 0;
		} else if (event.info.effect < MIN_GOAL) {
			event.info.effect = MIN_GOAL;
			step = 0;
		}
		if (event.info.focus != null) {	
			event.info.focus += step;
		}
		if (event.info.army != null) {	
			event.info.army += step;
		}
		if (event.info.government_influence_distance != null) {	
			event.info.government_influence_distance += step;
		}
		if (event.info.government_influence != null) {	
			event.info.government_influence += step;
		}
		if (event.info.loyalty != null) {	
			event.info.loyalty += step;
		}
		if (event.info.tax_effectiveness != null) {	
			event.info.tax_effectiveness += step;
		}
		updateModifiers(event, country);
	}
}

def removeEvent(event) {
	log 'removeEvent ' + event;
	event.removeFromGame();
}



