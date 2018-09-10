import groovy.transform.Field;
import com.cwsni.world.model.engine.modifiers.*

@Field final String EVENT_TYPE = 'EPIDEMIC';
@Field final double EVENT_EPIDEMIC_PROBABILITY = 0.01;
@Field final double EVENT_EPIDEMIC_STOP_MIN_PROBABLILITY = 0.1;
@Field final double EVENT_EPIDEMIC_STOP_MAX_PROBABLILITY = 0.9;
@Field final double EVENT_EPIDEMIC_SPREAD_PROBABILITY = 0.5;
@Field final double EVENT_EPIDEMIC_SPREAD_PROBABILITY_DECREASING = 0.001;
@Field final double EVENT_EPIDEMIC_DEATH_RATE = 0.4;
@Field final double EVENT_EPIDEMIC_RESISTANCE_STOP_PROBABLILITY = 0.01;

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
			// We need to replace collection by Set, because after loading it is just a list.
			event.info.provinces = new HashSet(event.info.provinces);
			event.info.resistant = new HashSet(event.info.resistant);
			activateEvent(event);
    	}
	}       
}

def log(msg) {
	//println msg;
}

def checkNewEvent() {
	log 'checkNewEvent';
	//return;
	def probablility = data.game.turn.probablilityPerYear(EVENT_EPIDEMIC_PROBABILITY);
	if (data.rnd.nextDouble() > probablility ) {return null;}
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
	event.info.contagiousness = data.rnd.nextDouble() * EVENT_EPIDEMIC_SPREAD_PROBABILITY;
	event.info.deathRate = data.rnd.nextDouble() * EVENT_EPIDEMIC_DEATH_RATE;
	event.info.stopProbability = EVENT_EPIDEMIC_STOP_MIN_PROBABLILITY 
			+ data.rnd.nextDouble() * (EVENT_EPIDEMIC_STOP_MAX_PROBABLILITY - EVENT_EPIDEMIC_STOP_MIN_PROBABLILITY);
	event.info.provinces = [] as Set;
	event.info.resistant = [] as Set;
	event.info.provinces << core.id;
	activateEvent(event);
	
	log 'created new event ' + event;
	return event;
}

def activateEvent(event) {
	activateEventForProvinces(event, event.info.provinces);
}

def activateEventForProvinces(event, provinces) {
	provinces.each {pId ->
		def province = data.game.map.findProvinceById(pId); 
		data.events.addModifier(province, ProvinceModifier.POPULATION_AMOUNT, ModifierType.MULTIPLY,
					data.game.turn.multiplyPerYear(1 - event.info.deathRate * medicineImpact(province)), event)
	};
}

def updateModifiers(event) {
	event.provinceModifiers.entrySet().forEach({entry ->
			def province = data.game.map.findProvinceById(entry.key);
			def modifiers = entry.value;
			modifiers.forEach({modifier -> 
				province.modifiers.update(modifier, 
					data.game.turn.multiplyPerYear(1 - event.info.deathRate * medicineImpact(province)))
			});
		});
}

def medicineImpact(province) {
	return Math.min(1, Math.max(0.1, 1 - province.diseaseResistanceLevel / 10)); 
}

def processExistingEvent(event) {
	log 'processExistingEvent';
	def stopProbability = data.game.turn.probablilityPerYear(event.info.stopProbability);
	def spreadProbability = data.game.turn.probablilityPerYear(event.info.contagiousness);
	def newProvinces = [] as Set;
	def iter = event.info.provinces.iterator();	
	while (iter.hasNext()) {
		def provinceId = iter.next();
		def province = data.game.map.findProvinceById(provinceId);
		// check expiring
		if (data.rnd.nextDouble() < stopProbability) {
			province.modifiers.removeByEvent(event); 
			iter.remove();
			// add temporary resistance to this province
			event.info.resistant << provinceId;
		} 
		// check spreading to neighbors
		province.neighbors.stream()
			.filter({n -> n.getPopulationAmount() > 0 && !event.info.provinces.contains(n.id) && !event.info.resistant.contains(n.id)})
			.forEach({n -> 
				if (data.rnd.nextDouble() < spreadProbability) { 
					newProvinces << n.id;
				}
		});		
	}
	event.info.provinces.addAll(newProvinces);
	activateEventForProvinces(event, newProvinces);

	if (event.info.provinces.isEmpty()) {
		removeEvent(event);
	} else {
		// check resistane expiring
		stopProbability = data.game.turn.probablilityPerYear(EVENT_EPIDEMIC_RESISTANCE_STOP_PROBABLILITY);
		iter = event.info.resistant.iterator();	
		while (iter.hasNext()) {
			def provinceId = iter.next();
			if (data.rnd.nextDouble() < stopProbability) {
				iter.remove();	
			}
		}
		event.info.contagiousness -= data.game.turn.addPerYear(EVENT_EPIDEMIC_SPREAD_PROBABILITY_DECREASING); 
		updateModifiers(event);
	}
}

def removeEvent(event) {
	log 'removeEvent ' + event;
	event.removeFromGame();
}



