window.xhr = new XMLHttpRequest();
window.buffer = new Array();

function send(value) {
	
	window.buffer.push(value);
	
	if(window.buffer.length == 200) {
		
		sendReally();	
	}
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisavarexectracingcall', false);
	window.xhr.send(JSON.stringify(window.buffer));
	window.buffer = new Array();
}

function addVariable(name, type, varCategory, sourceCode, statementCategory) {
	
	return new Array(name, type, varCategory, sourceCode, statementCategory);

				
};