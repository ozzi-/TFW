var sun = "&#9728";
var cloud = "&#127785;";

// *************
// * Login/out *
// *************
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
		localStorage.setItem('TFW_Token',session.sessionIdentifier);
		localStorage.setItem('TFW_Role',session.role);
		window.location.replace("index.html");
	}
}

doLogout = function() {
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			localStorage.removeItem("TFW_Token");
			localStorage.removeItem("TFW_Role");
			window.location.replace("index.html?page=login");
		}
	};
	request.open("POST", "../logout");
	request.setRequestHeader('X-TFW-Session-ID', localStorage.getItem('TFW_Token'));
	request.send();
}

// *******
// * Run *
// *******

function runTest(res,name,paramName) {
	runInternal(res, name ,paramName, "getStatus");
}

function runTestGroup(res,name,paramName) {
	runInternal(res, name ,paramName, "getGroupStatus");
}

function runInternal(res, name, paramName, call){
	var pollTime = 1200;
	
	handle = res.handle;
	doRequest("GET", "../"+call+"/" + name + "/" + handle, poll, [name,paramName], true);
	poller = setInterval(function() {
		doRequest("GET", "../"+call+"/" + name + "/" + handle, poll, [name,paramName], true);
	}, pollTime);
}

function poll(res,name,paramName) {
	document.getElementById("state").innerHTML=res.state;
	if(res.state=="done"){
		removeLoader();
		var a = document.createElement("a");
		a.innerHTML = "Result";
		a.setAttribute('href', "index.html?page=result&"+paramName+"="+ name + "&handle="+ handle);
		document.getElementById("resultLink").appendChild(a);
		clearInterval(poller);
	}
}


// ********
// * List *
// ********

function listTests(tests) {
	removeLoader();
	var testsSpan = document.getElementById("tests");
	var testCount = tests.length;
	if(localStorage.getItem('TFW_Role')==="rw"){
		var innerHTML = "<table><tr><td><b>Test</b></td><td style=\"width: 50px;\"><b>Run</b></td><td><b>Last run</b></td></tr>";
	}else{
		var innerHTML = "<table><tr><td><b>Test</b></td><td></td><td>Last run</td></tr>";
	}
	for (var i = 0; i < testCount; i++) {
		var runState = tests[i].lastRunPassed ? sun: cloud ;
		innerHTML += "<tr><td style=\"padding-right:20px;\"><a href=\"index.html?page=results&name="+ tests[i].name + "\">" + tests[i].name;
		if(localStorage.getItem('TFW_Role')==="rw"){
			innerHTML += "</a></td><td><a style=\"text-decoration:none; width: 50px;\" href=\"index.html?page=run&name="+tests[i].name+"\">&#9658;</a></td><td>"+tests[i].lastRunDate+runState+"</td></tr>";
		}else{
			innerHTML += "</a></td><td>"+tests[i].lastRunDate+runState+"</td></tr>";
		}
	}
	innerHTML+="</table>";
	testsSpan.innerHTML = innerHTML;
}

function listGroups(groups){
	var groupsSpan = document.getElementById("testGroups");
	var groupCount = groups.length;
	removeLoader();
	if(localStorage.getItem('TFW_Role')==="rw"){
		var innerHTML = "<table><tr><td><b>Test</b></td><td><b>Tests</b></td><td style=\"width: 50px;\"><b>Run</b></td><td><b>Last run</b></td></tr>";
	}else{
		var innerHTML = "<table><tr><td><b>Test</b></td><td>Tests</td><td></td><td>Last run</td></tr>";
	}
	for (var i = 0; i < groupCount; i++) {
		var tests ="";
		for(var j = 0; j < groups[i].tests.length; j++){
			tests+=groups[i].tests[j].name+",";
		}
		tests = tests.slice(0, -1);
		innerHTML += "<tr><td style=\"padding-right:20px;\"><a href=\"index.html?page=results&groupname="+
		groups[i].name+"\">"+groups[i].name+"</a></td><td style=\"padding-right:20px;\">"+tests;
		var runState = groups[i].lastRunPassed ? sun : cloud;
		if(localStorage.getItem('TFW_Role')==="rw"){
			innerHTML += "</td><td><a style=\"text-decoration:none;\" href=\"index.html?page=run&groupname="+groups[i].name+"\">&#9658;</a></td><td>"+groups[i].lastRunDate+runState+"</td></tr>";			
		}else{
			innerHTML += "</td><td></td><td>"+groups[i].lastRunDate+runState+"</td></tr>";			
		}
	}
	innerHTML += "</table>";
	groupsSpan.innerHTML = innerHTML;
}
function listResults(results,paramName) {
	removeLoader();
	var testName = document.getElementById("testName");
	var name = getQueryParams(document.location.search).name;
	if(name === undefined || name == "undefined"){
		name = getQueryParams(document.location.search).groupname;
	}
	testName.innerHTML = name;
	
	results = results.reverse(); 
	var resultCount = results.length;
	var resultsSpan = document.getElementById("resultsSpan");
	if(resultCount==0){
		resultsSpan.innerHTML = "<i>This test has not been run yet.</i>";
	}else{
		for (var i = 0; i < resultCount; i++) {
			var a = document.createElement("a");
			var passed = true;
			for (var j = 0; j < results[i].result.results.length; j++) {
				passed = results[i].result.results[j].passed;
				if(!passed){
					break;
				}
			}
			a.innerHTML  = (passed==true ? " "+sun: " "+cloud) 
				+ " " + results[i].result.testStartString;
			
			a.setAttribute('href', "index.html?page=result&"+paramName+"="
					+ results[i].result.testName + "&handle="
					+ results[i].handle);
			resultsSpan.appendChild(a);
			resultsSpan.appendChild(document.createElement("br"));
		}
	}
}


function listResult(result) {
	removeLoader();
	var style = ' style="color:green;" ';
	for (var i = 0; i < result.results.length; i++) {
		if(! result.results[i].passed){
			style=' style="color:red;" ';
		}
	}
	var infoSpan = document.getElementById("info");
	infoSpan.innerHTML = ("<h3"+style+">" + result.testName + " - "
			+ result.testStartString + "</h3>");
	infoSpan.innerHTML += "<b>Description</b>: "+ escapeHtml(result.description) + "<br><hr>";

	var resultsSpan = document.getElementById("results");
	for (var i = 0; i < result.results.length; i++) {
		resultsSpan.innerHTML += "<h3>"+result.results[i].name + " " + (result.results[i].passed == false ? cloud : sun) + "</h3>"
				+ "<b>Result</b>: <i>"+ escapeHtml(result.results[i].description) + "</i><br>"
				+ "<b>Output:</b> "+ escapeHtml(result.results[i].output) + " <br> " 
				+ "<b>Error Output:</b> " + escapeHtml(result.results[i].errorOutput) + "<br>"
				+ "<b>Runtime: </b> "+ escapeHtml(result.results[i].runTimeInMS) + " ms<br> "
				+ "<br><br>";
	}
}

function listTestContent(result){
	console.log(result);
	var form = createTestMask(result);
	var testContent = document.getElementById("testContent");
//	testContentJSON = JSON.stringify(result , null, 2);
//	console.log(testContentJSON);
	testContent.append(form);
}

// ***********
// * Network *
// ***********

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
	request.setRequestHeader('X-TFW-Session-ID', localStorage.getItem('TFW_Token'));
	request.send();
}

// ********************
// * Helper Functions *
// ********************

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

function removeLoader(){
	if(document.getElementById("loader")){
		document.getElementById("loader").remove();					
	}
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

function basePath(basePath) { 
	removeLoader();
	var basePathSpans = document.getElementsByClassName("basePath");
	for(let i = 0; i < basePathSpans.length; i++){
		basePathSpans[i].innerHTML = basePath;
	}		
}


// ********
// * FORM *
// ********

function createTestMask(json){
	var maskSpan = document.createElement("span");
	
	var successHook = createInput("Success Hook", json.settings.successhook, true);
	maskSpan.append(successHook);
	maskSpan.append(document.createElement("br"));
	
	var failureHook = createInput("Failure Hook", json.settings.failurehook, true);
	maskSpan.append(failureHook);
	maskSpan.append(document.createElement("br"));

	var failureHook = createInput("Description", json.test.description, true);
	maskSpan.append(failureHook);
	maskSpan.append(document.createElement("br"));
	maskSpan.append(document.createElement("br"));

	var tasksTitle = document.createElement("span");
	tasksTitle.innerHTML = "Tasks";
	maskSpan.append(tasksTitle);
	maskSpan.append(document.createElement("br"));


	for (i = 0; i < json.test.tasks.length ; i++) {
		var task = json.test.tasks[i];
		var taskDiv = createTaskDiv(task);
		maskSpan.append(taskDiv);
	}
	
	return maskSpan;
}

function createTaskDiv(task){
	var tasksDiv = document.createElement("div");
	tasksDiv.setAttribute("style","border: 1px solid; border-color: lightgrey; border-radius: 5px; margin-bottom: 4px; padding: 15px;");

	var taskName = createInput("Name", task.name, true);
	tasksDiv.append(taskName);
	tasksDiv.append(document.createElement("br"));
	var taskPath = createInput("Path", task.path, true);
	tasksDiv.append(taskPath);
	tasksDiv.append(document.createElement("br"));
	var taskPath = createInput("Timeout", task.timeout, true);
	tasksDiv.append(taskPath);
	tasksDiv.append(document.createElement("br"));
	
	return tasksDiv;
}

function createInput(title, value, disabled){
	var resultSpan = document.createElement("span");
	
	var descriptionSpan = document.createElement("span");
	descriptionSpan.innerHTML = title;
	resultSpan.append(descriptionSpan);
	
	var descriptionBR = document.createElement("br");
	resultSpan.append(descriptionBR);

	var valueInput = document.createElement("input");
	value = value == undefined? "":value;
	valueInput.setAttribute("value", value);
	valueInput.disabled = disabled;
	valueInput.setAttribute("class","form-control");
	resultSpan.append(valueInput);
	
	return resultSpan;
}