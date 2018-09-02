def processCountry() {
	//processCountryWithScript(data);
	processCountryWithJava();
	//data.scriptHandler.invoke("test", "recurse", 0);
}


// ------------------------------------------------
def processCountryWithJava() {
	data.javaAIHandler.processCountry(data);
}
// ------------------------------------------------

def processCountryWithScript(data) {
	// TODO
}
