function removeLoader(){
	if(document.getElementById("loader")){
		document.getElementById("loader").remove();					
	}
}

function listGroups(groups){
	var groupsSpan = document.getElementById("testGroups");
	var groupCount = groups.length;
	removeLoader();
	if(sessionStorage.getItem('TFW_Role')==="rw"){
		var innerHTML = "<table><tr><td><b>Test Group</b></td><td><b>Tests</b></td><td><b>Run</b></td></tr>";
	}else{
		var innerHTML = "<table><tr><td><b>Test Group</b></td><td><b>Tests</b></td><td></td></tr>";
	}
	for (var i = 0; i < groupCount; i++) {
		var tests ="";
		for(var j = 0; j < groups[i].tests.length; j++){
			tests+=groups[i].tests[j].name+",";
		}
		tests = tests.slice(0, -1);
		innerHTML += "<tr><td style=\"padding-right:20px;\"><a href=\"index.html?page=results&groupname="+
		groups[i].name+"\">"+groups[i].name+"</a></td><td style=\"padding-right:20px;\">"+tests;
		if(sessionStorage.getItem('TFW_Role')==="rw"){
			innerHTML += "</td><td><a style=\"text-decoration:none;\" href=\"index.html?page=run&groupname="+groups[i].name+"\">&#9658;</a></td></tr>";			
		}else{
			innerHTML += "</td><td></td></tr>";			
		}
	}
	innerHTML += "</table>";
	groupsSpan.innerHTML = innerHTML;
}

function sha512(str) {
	return crypto.subtle.digest("SHA-512", new TextEncoder("utf-8").encode(str)).then(buf => {
		return Array.prototype.map.call(new Uint8Array(buf), x=>(('00'+x.toString(16)).slice(-2))).join('');
	});
}

function reload(){
	window.location.href="index.html";
}

function back(){
	var param = "name";
	var name = getQueryParams(document.location.search).name;
	if(name === undefined || name == "undefined"){
		name = getQueryParams(document.location.search).groupname;
		param = "groupname";
	}
	window.location.href='index.html?page=results&'+param+'='+name;
}

function listResults(results,paramName) {
	removeLoader();
	results = results.reverse(); 
	var resultCount = results.length;
	var resultsUL = document.getElementById("resultsUL");
	if(resultCount==0){
		resultsUL.innerHTML = "<i>This test has not been run yet.</i>";
	}else{
		for (var i = 0; i < resultCount; i++) {
			var li = document.createElement("li");
			var a = document.createElement("a");
			var passed = true;
			for (var j = 0; j < results[i].result.results.length; j++) {
				passed = results[i].result.results[j].passed == "false" ? false : true;
				if(!passed){
					break;
				}
			}
			a.innerHTML  = (passed==false ? " &#10005;": " &#10003;") 
				+ " " + results[i].result.testStartString;
			
			a.setAttribute('href', "index.html?page=result&"+paramName+"="
					+ results[i].result.testName + "&handle="
					+ results[i].handle);
			li.appendChild(a);
			resultsUL.appendChild(li);
		}
	}
}

function runTestGroup(res,name,paramName) {
	handle = res.handle;
	doRequest("GET", "../getGroupStatus/" + name + "/" + handle, poll, [name,paramName], true);
	poller = setInterval(function() {
		doRequest("GET", "../getGroupStatus/" + name + "/" + handle, poll, [name,paramName], true);
	}, 1200);
}

function poll(res,name,paramName) {
	var finished = false;
	document.getElementById("state").innerHTML=res.state;
	if(res.state=="done" && finished==false){
		finished=true;
		var a = document.createElement("a");
		a.innerHTML = "Result";
		a.setAttribute('href', "index.html?page=result&"+paramName+"="
			+ name + "&handle="
			+ handle);
		document.getElementById("resultLink").appendChild(a);
		clearInterval(poller);
	}
}

function runTest(res,name,paramName) {
	handle = res.handle;
	doRequest("GET", "../getStatus/" + name + "/" + handle, poll, [name,paramName], true);
	poller = setInterval(function() {
		doRequest("GET", "../getStatus/" + name + "/" + handle, poll, [name,paramName], true);
	}, 1200);
}


function listResult(result) {
	removeLoader();
	var style = ' style="color:green;" ';
	for (var i = 0; i < result.results.length; i++) {
		if(result.results[i].passed == "false"){
			style=' style="color:red;" ';
		}
	}
	
	var infoSpan = document.getElementById("info");
	infoSpan.innerHTML = ("<h3"+style+">" + result.testName + " - "
			+ result.testStartString + "</h3>");
	infoSpan.innerHTML += "<b>Description</b>: "+ escapeHtml(result.description) + "<br><hr>";

	var resultsSpan = document.getElementById("results");
	for (var i = 0; i < result.results.length; i++) {
		resultsSpan.innerHTML += "<h3>"+result.results[i].name + " " + (result.results[i].passed == "false" ? "&#10005;": "&#10003;") + "</h3>"
				+ "<b>Result</b>: <i>"+ escapeHtml(result.results[i].description) + "</i><br>"
				+ "<b>Output:</b> "+ escapeHtml(result.results[i].output) + " <br> " 
				+ "<b>Error Output:</b> " + escapeHtml(result.results[i].errorOutput) + "<br>"
				+ "<b>Runtime: </b> "+ escapeHtml(result.results[i].runTimeInMS) + " ms<br> "
				+ "<br><br>";
	}
}

function doLogin(){
	var username = document.getElementById("username").value;
	var password = document.getElementById("password").value;
	var loginObj = new Object();
	loginObj.username = username;
	loginObj.password  = password;
	doRequestBody("POST", JSON.stringify(loginObj), "application/json", "../login/", processLoginResult, true);
	return false;
}

function processLoginResult(response){
	if(response.status==401){
		alert(response.responseText);
	}else{
		var session = JSON.parse(response.responseText);
		sessionStorage.setItem('TFW_Token',session.sessionIdentifier);
		sessionStorage.setItem('TFW_Role',session.role);
		window.location.replace("index.html");
	}
}

function basePath(basePath) { 
	var basePathSpans = document.getElementsByClassName("basePath");
	for(let i = 0; i < basePathSpans.length; i++){
		basePathSpans[i].innerHTML = basePath;
	}		
}

function listTests(tests) {
	removeLoader();
	var testsSpan = document.getElementById("tests");
	var testCount = tests.length;
	if(sessionStorage.getItem('TFW_Role')==="rw"){
		var innerHTML = "<table><tr><td><b>Test</b></td><td><b>Run</b></td></tr>";
	}else{
		var innerHTML = "<table><tr><td><b>Test</b></td><td></td></tr>";
	}
	for (var i = 0; i < testCount; i++) {
		if(sessionStorage.getItem('TFW_Role')==="rw"){
			innerHTML += "<tr><td style=\"padding-right:20px;\"><a href=\"index.html?page=results&name="
				+ tests[i].name + "\">" + tests[i].name 
				+ "</a></td><td><a style=\"text-decoration:none;\" href=\"index.html?page=run&name="+tests[i].name+"\">&#9658;</a></td></tr>";
		}else{
			innerHTML += "<tr><td style=\"padding-right:20px;\"><a href=\"index.html?page=results&name="
				+ tests[i].name + "\">" + tests[i].name + "</a></td><td></td></tr>";
		}
	}
	innerHTML+="</table>";
	testsSpan.innerHTML = innerHTML;
}


doRequestBody = function(method, data, type, url, callback, params) {
	var request = new XMLHttpRequest();
	request.timeout = 5000;
	request.ontimeout = function() {
		alert("The request for " + url + " timed out.");
	};

	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200) {
				try { 
					params = [request].concat(params);
					callback.apply(this,params);
				} catch (e) {
					alert("Unknown error");
				}
			} else {
				try { 
					callback(request);
				} catch (e) {
					alert("Unknown error");
				}
			}
		}
	};
	request.open(method, url);
	request.setRequestHeader("Content-Type", type);
	request.send(data);
}

doLogout = function() {
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			sessionStorage.removeItem("TFW_Token");
			sessionStorage.removeItem("TFW_Role");
			window.location.replace("index.html?page=login");
		}
	};
	request.open("POST", "../logout");
	request.setRequestHeader('X-TFW-Session-ID', sessionStorage.getItem('TFW_Token'));
	request.send();
}


doRequest = function(method, url, callback, params) {
	var request = new XMLHttpRequest();

	request.timeout = 5000;
	request.ontimeout = function() {
		alert("The request for " + url + " timed out.");
	};

	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200) {
				var response = "";
				try { 
					var responseJSON = JSON.parse(request.responseText);
					response = responseJSON;
				} catch (e) {
					response = request.responseText;
				}
				params = [response].concat(params);
				callback.apply(this,params);
			} else {
				try { 
					response = JSON.parse(request.responseText);
					if(request.status==403){
						window.location.replace("index.html?page=login");
					}
					alert("Error: " + response.error);
				} catch (e) {
					alert("Unknown error");
				}
			}
		}
	};
	request.open(method, url);
	request.setRequestHeader('X-TFW-Session-ID', sessionStorage.getItem('TFW_Token'));
	request.send();
}

// helpers

function getQueryParams(qs) {
	qs = qs.split('+').join(' ');
	var params = {}, tokens, re = /[?&]?([^=]+)=([^&]*)/g;
	while (tokens = re.exec(qs)) {
		params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
	}
	return params;
}

function escapeHtml(stn) {
	stn = String(stn);
	return stn.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g,"&gt;")
		.replace(/"/g, "&quot;")
		.replace(/'/g, "&#039;");
}