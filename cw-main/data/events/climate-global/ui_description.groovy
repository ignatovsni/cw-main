def getTitleAndShortDescription(event, languageCode) {
	def title = String.format(data.getMessage('event.title'), event.info.effect);
	def shortDescription = String.format(data.getMessage('event.description.short'), 
		data.game.turn.getDateTexToDisplay(event.createdTurn),
		data.game.turn.howManyYearsHavePassedSinceTurn(event.createdTurn));
	return [title, shortDescription];
}
