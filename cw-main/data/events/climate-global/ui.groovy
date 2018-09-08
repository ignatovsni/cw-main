// The application invokes this method to get description for the user.
// Must return the list with 2 elements: [title, short description].
// target - the object for which was invoked this method (e.g. the province with the modifiers of this event).
def getTitleAndShortDescription(event, languageCode, target) {
	def title = String.format(data.getMessage('event.title'), event.info.effect*100);
	def shortDescription = String.format(data.getMessage('event.description.short'), 
		data.game.turn.getDateTexToDisplay(event.createdTurn),
		data.game.turn.howManyYearsHavePassedSinceTurn(event.createdTurn));
	return [title, shortDescription];
}


// The application invokes this method to get the list of possible map modes.
// Must return Map.key = the mode code, Map.value = List<String> with 2 elements: [title, tooltip description].
def getMapModes() {
	return [:];
}
