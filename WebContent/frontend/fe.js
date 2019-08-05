'use strict';
var sun = "&#9728";
var cloud = "&#127785;";
var htmlFormatter = function(cell, formatterParams){
    var data = cell.getData();
    return cell.getValue();
}

// ************
// * Settings *
// ************

function changeOthersPassword(){
	var username = document.getElementById("changeUsername").value;
	var password = document.getElementById("changePassword").value;
	var changeObj = new Object();
	changeObj.username = username;
	changeObj.password  = password;
	doRequestBody("POST", JSON.stringify(changeObj), "application/json", "../changePasswordForUser/", proccessPasswordChangeForUser, true, true);
	return false;
}

function createUser(){
	var username = document.getElementById("createUsername").value;
	var password = document.getElementById("createPassword").value;
	var role = document.getElementById("createRole").value;
	var changeObj = new Object();
	changeObj.username = username;
	changeObj.password  = password;
	changeObj.role  = role;
	doRequestBody("POST", JSON.stringify(changeObj), "application/json", "../createUser/", processCreateUser, true, true);
	return false;
}

function deleteUser(){
	var username = document.getElementById("deleteUsername").value;
	var deleteObj = new Object();
	deleteObj.username = username;
	doRequestBody("POST", JSON.stringify(deleteObj), "application/json", "../deleteUser/", processDeleteUser, true, true);
	return false;
}


function changeMyPassword(){
	var password = document.getElementById("myPassword").value;
	var passwordObj = new Object();
	passwordObj.password  = password;
	doRequestBody("POST", JSON.stringify(passwordObj), "application/json", "../changePassword/", proccessPasswordChange, true, true);
	return false;
}

function proccessPasswordChange(response){
	if(response.status!=200){
		alert(JSON.parse(response.responseText).error);
	}else{
		alert("Password changed, you will be logged out");
		doLogout();
	}
}

function processCreateUser(response){
	if(response.status!=200){
		alert(JSON.parse(response.responseText).error);
	}else{
		alert("User created");
		location.reload();
	}
}


function processDeleteUser(response){
	if(response.status!=200){
		alert(JSON.parse(response.responseText).error);
	}else{
		alert("User deleted");
		location.reload();
	}
}

function proccessPasswordChangeForUser(response){
	if(response.status!=200){
		alert(JSON.parse(response.responseText).error);
	}else{
		alert("Password changed");
		location.reload();
	}
}
// *************
// * Login/out *
// *************
function doLogin(){
	var username = document.getElementById("username").value;
	var password = document.getElementById("password").value;
	var loginObj = new Object();
	loginObj.username = username;
	loginObj.password  = password;
	doRequestBody("POST", JSON.stringify(loginObj), "application/json", "../login/", processLoginResult, true, false);
	return false;
}

function processLoginResult(response){
	if(response.status!=200){
		alert(response.responseText);
		document.getElementById("password").value="";
	}else{
		var session = JSON.parse(response.responseText);
		localStorage.setItem('TFW_Token',session.sessionIdentifier);
		localStorage.setItem('TFW_Role',session.role);
		window.location.replace("index.html");
	}
}

function doLogout() {
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
	var handle = res.handle;
	function doPoll() {
		doRequest("GET", "../"+call+"/" + name + "/" + handle, poll, [name,paramName, poller, handle], true);
	}
	doPoll();
	var poller;
	poller = setInterval(doPoll, pollTime);
}

function poll(res,name,paramName, poller, handle) {
	document.getElementById("state").innerHTML=res.state;
	if(res.state=="done"){
		removeLoader();
		var a = document.createElement("a");
		a.innerHTML = "Result";
		a.setAttribute('href', "index.html?page=result&"+paramName+"="+ name + "&handle="+ handle);
		document.getElementById("resultLink").appendChild(a);
		if(poller === undefined || poller == "undefined" || poller != null){
			clearInterval(poller);			
		}
	}
}


// ********
// * List *
// ********

function listTests(tests) {
	var testCount = tests.length;
	removeLoader();

	if(localStorage.getItem('TFW_Role')==="rw" || localStorage.getItem('TFW_Role')==="a"){
		var table = new Tabulator("#testsTable", {
		    layout:"fitDataFill",
		    columns:[
		    {title:"Test", field:"test", minWidth:170, formatter:htmlFormatter},
		    {title:"Run", field:"run",  minWidth:70, formatter:htmlFormatter},
		    {title:"Status", field:"status", minWidth:70, formatter:htmlFormatter},
		    {title:"Last Run", field:"lastRun" , minWidth:150},
		    {title:"Last Run Time", field:"lastRunTime"},
		    ],
		});
	}else{
		var table = new Tabulator("#testsTable", {
		    layout:"fitDataFill",
		    columns:[
		    {title:"Test", field:"test", minWidth:170, formatter:htmlFormatter},
		    {title:"Status", field:"status", minWidth:70, formatter:htmlFormatter},
		    {title:"Last Run", field:"lastRun" , minWidth:150},
		    {title:"Last Run Time", field:"lastRunTime"},
		    ],
		});		
	}
	
	if(testCount==0){
		table.addRow([{test:"No tests defined yet", run:"", status:"", lastRun: "", lastRunTime:""}], false);
	}
	for (var i = 0; i < testCount; i++) {
		var testLink = "<a href=\"index.html?page=results&name="+tests[i].name+"\">"+tests[i].name+"</a>";
		var runLink = "<a style=\"text-decoration:none;\" href=\"index.html?page=run&name="+tests[i].name+"\"> &#9654; </a>";
		var runState = tests[i].lastRunPassed ? sun : cloud;
		runState = tests[i].lastRunDate.length==0 ? "-" : runState;
		var lastRunTime = timeConversion(tests[i].totalRunTimeInMS);
		if(localStorage.getItem('TFW_Role')==="rw" || localStorage.getItem('TFW_Role')==="a"){
			table.addRow([{test:testLink, run:runLink, status:runState, lastRun: tests[i].lastRunDate, lastRunTime:lastRunTime}], false);
		}else{
			table.addRow([{test:testLink, status:runState, lastRun: tests[i].lastRunDate, lastRunTime:lastRunTime}], false);			
		}
	}
}

function listGroups(groups){
	var groupCount = groups.length;
	removeLoader();
	
	if(localStorage.getItem('TFW_Role')==="rw" || localStorage.getItem('TFW_Role')==="a"){
		var table = new Tabulator("#testGroupsTable", {
		    layout:"fitDataFill",
		    columns:[
		    {title:"Group", field:"group", minWidth:170, formatter:htmlFormatter},
		    {title:"Tests", field:"tests"},
		    {title:"Run", field:"run", minWidth:70, formatter:htmlFormatter},
		    {title:"Status", field:"status", formatter:htmlFormatter},
		    {title:"Last Run", field:"lastRun", minWidth:150},
		    {title:"Last Run Time", field:"lastRunTime"},
		    ],
		});
	}else{
		var table = new Tabulator("#testGroupsTable", {
		    layout:"fitDataFill",
		    columns:[
			    {title:"Group", field:"group", minWidth:170, formatter:htmlFormatter},
			    {title:"Tests", field:"tests"},
			    {title:"Status", field:"status", formatter:htmlFormatter},
			    {title:"Last Run", field:"lastRun" , minWidth:150},
			    {title:"Last Run Time", field:"lastRunTime"},
		    ],
		});		
	}
	if(groupCount==0){
		table.addRow([{group:"No Groups defined yet", tests:"", run:"", status:"", lastRun: "", lastRunTime:""}], false);
	}
	for (var i = 0; i < groupCount; i++) {
		var tests ="";
		for(var j = 0; j < groups[i].tests.length; j++){
			tests+=groups[i].tests[j].name+",";
		}
		tests = tests.slice(0, -1);
		
		var groupLink = "<a href=\"index.html?page=results&groupname="+groups[i].name+"\">"+groups[i].name+"</a>";
		var runLink = "<a style=\"text-decoration:none;\" href=\"index.html?page=run&groupname="+groups[i].name+"\"> &#9654; </a>";
		var runState = groups[i].lastRunPassed ? sun : cloud;
		runState = groups[i].lastRunDate.length==0 ? "-" : runState;
		var lastRunTime = timeConversion(groups[i].totalRunTimeInMS);
		if(localStorage.getItem('TFW_Role')==="rw" || localStorage.getItem('TFW_Role')==="a"){
			table.addRow([{group:groupLink, tests:tests, run:runLink, status:runState, lastRun: groups[i].lastRunDate, lastRunTime:lastRunTime}], false);
		}else{
			table.addRow([{group:groupLink, tests:tests, status:runState, lastRun: groups[i].lastRunDate, lastRunTime:lastRunTime}], false);			
		}
	}
}
function listResults(results,paramName) {
	removeLoader();
	var testName = document.getElementById("testName");
	var name = getQueryParams(document.location.search).name;
	if(name === undefined || name == "undefined"){
		name = getQueryParams(document.location.search).groupname;
	}
	testName.innerHTML = escapeHtml(name);
	
	results = results.sort((a, b) => (a.handle < b.handle) ? 1 : -1);
	
	var resultCount = results.length;
	var resultsSpan = document.getElementById("resultsSpan");
	if(resultCount==0){
		resultsSpan.innerHTML = "<i>This test has not been run yet.</i>";
	}else{
		
		var table = new Tabulator("#resultsSpan", {
		    layout:"fitDataFill",
		    columns:[
		    {title:"Status", field:"status", formatter:htmlFormatter},
		    {title:"Last Run", field:"lastRun", formatter:htmlFormatter},
		    ],
		});
		
		
		for (var i = 0; i < resultCount; i++) {
			var a = document.createElement("a");
			var passed = true;
			for (var j = 0; j < results[i].result.results.length; j++) {
				passed = results[i].result.results[j].passed;
				if(!passed){
					break;
				}
			}
			var status = (passed==true ? " "+sun: " "+cloud);
			var lastRun = "<a href=\"index.html?page=result&"+paramName+"="+results[i].result.testName+"&handle="+ results[i].handle+"\">"+results[i].result.testStartString+"</a>";
			table.addRow([{status:status, lastRun: lastRun}], false);
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
	infoSpan.innerHTML += "<b>Run by</b>: "+result.testRunBy+"&nbsp;&nbsp; <b>Description</b>: "+ escapeHtml(result.description) + "<br><hr>";

	var resultsSpan = document.getElementById("results");
	for (var i = 0; i < result.results.length; i++) {
		resultsSpan.innerHTML += "<h3>"+result.results[i].name + " " + (result.results[i].passed == false ? cloud : sun) + "</h3>"
				+ "<b>Result</b>: <i>"+ escapeHtml(result.results[i].description) + "</i><br>"
				+ "<b>Output:</b> "+ escapeHtml(result.results[i].output).replace(/\n/g, "<br />") + " <br> " 
				+ "<b>Error Output:</b> " + escapeHtml(result.results[i].errorOutput) + "<br>"
				+ "<b>Runtime: </b> "+ escapeHtml(result.results[i].runTimeInMS) + " ms<br> "
				+ "<br><hr><br>";
	}
}

function listTestContent(result){
	var form = createTestMask(result);
	var testContent = document.getElementById("testContent");
	testContent.append(form);
}

// ***********
// * Network *
// ***********

function doRequestBody(method, data, type, url, callback, params, sendAuth) {
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
	if(sendAuth){
		request.setRequestHeader('X-TFW-Session-ID', localStorage.getItem('TFW_Token'));
	}
	request.setRequestHeader("Content-Type", type);
	request.send(data);
}



function doRequest(method, url, callback, params) {
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
						return;
					}
					alert("Error: " + response.error);
				} catch (e) {
					if(request.status==404){
						window.location.replace("index.html");
					}else{
						alert("Unknown error");						
					}
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


	for (var i = 0; i < json.test.tasks.length ; i++) {
		var task = json.test.tasks[i];
		var taskDiv = createTaskDiv(task);
		maskSpan.append(taskDiv);
	}
	
	return maskSpan;
}

function displaySettings(){
	if(localStorage.getItem('TFW_Role')!=="a"){
		document.getElementById("settingsAdmin").remove();		
	}
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

function timeConversion(millisec) {
    var seconds = (millisec / 1000).toFixed(1);
    var minutes = (millisec / (1000 * 60)).toFixed(1);
    var hours = (millisec / (1000 * 60 * 60)).toFixed(1);
    var days = (millisec / (1000 * 60 * 60 * 24)).toFixed(1);
    
    if (millisec < 1000) {
    	return millisec +" ms"
    } else if (seconds < 60) {
        return seconds + " sec";
    } else if (minutes < 60) {
        return minutes + " min";
    } else if (hours < 24) {
        return hours + " hrs";
    } else {
        return days + " days"
    }
}