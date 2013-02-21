window.xhr = new XMLHttpRequest();
window.buffer = new Array();

function send(value) {
	window.buffer.push(value);
	if(window.buffer.length == 200) {
		sendReally();	
	}
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisafuncexectracingcall', false);
	window.xhr.send(JSON.stringify(window.buffer));
	window.buffer = new Array();
}

function addFunctionCallTrack(funcCallerName, funcCalleeName) {
	
	return new Array(funcCallerName, funcCalleeName);
				
};