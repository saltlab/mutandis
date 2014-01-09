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

function addFunctionCallTrack(funcCallerName) {
	
	
/*	if(arguments.callee.caller.funcName==undefined || arguments.callee.caller==null || String(arguments.callee.caller).indexOf("function (data)")==0){
		
		return new Array("NoFunctionNode", funcCalleeName);
	}
	else if(String(arguments.callee.caller).indexOf("function (e)")==0
			|| String(arguments.callee.caller).indexOf("function (event)")==0){
		return new Array("eventHandler", funcCalleeName);
	}
*/	
	
		
	return new Array(funcCallerName, arguments.callee.caller.funcName);
				
};