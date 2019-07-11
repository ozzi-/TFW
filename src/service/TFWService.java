package service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import helpers.Helpers;
import helpers.Log;
import helpers.PathFinder;
import pojo.Session;
import pojo.Test;
import pojo.User;
import tfw.Testing;

@Path("/")
public class TFWService {

	private static final String headerNameSessionID = "X-TFW-Session-ID";
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/login")
	public Response doLogin(String jsonLogin) throws Exception {
		User user = UserManagement.parseUserLoginJSON(jsonLogin);
		Log.log(Level.INFO, "Login attempt for user '" + user.getUsername() + "'");
		user = UserManagement.checkLogin(user);
		if (user != null) {
			Session session = SessionManagement.createSession(user.getUsername(),user.getRole());
			return Response.status(200).entity(session.toJSONObj().toString()).type("application/json").build();
		}
		return Response.status(401).entity("username or password wrong").build();
	}
	
	@GET
	@Path("/reload")
	public Response reload(@Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,true);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		Log.log(Level.INFO, "User "+userName+" reloading TFW");
		try {
			UserManagement.loadUsers();
		} catch (Exception e) {
			throw new Exception("Cannot load users - "+e.getMessage());
		}
		
		return Response.status(200).entity("reloaded").build();
	}

	@GET
	@Path("/testLogin")
	public Response test(@Context HttpHeaders headers) {
		if (checkLogin(headers,false) != null) {
			return Response.status(200).entity("OK").build();
		} else {
			return Response.status(203).entity("NOK").build();
		}
	}

	private String checkLogin(HttpHeaders headers, boolean requiresWritePrivilege) {
		if (headers != null && headers.getRequestHeader(headerNameSessionID) != null && headers.getRequestHeader(headerNameSessionID).size() > 0) { 
			String sessionIdentifier = headers.getRequestHeader(headerNameSessionID).get(0);
			Session session = SessionManagement.getSessionFormIdentifier(sessionIdentifier);
			if (session != null) {
				if(requiresWritePrivilege &&  !session.getRole().equals("rw")) {
					Log.log(Level.WARNING, "Login check failed - user "+session.getUsername()+" attempted to execute API call which he does not have sufficient privileges for.");
				}else {
					return session.getUsername();					
				}
			}else {
				Log.log(Level.INFO, "Login check failed due to session provided not found in session storage (hs)");
			}
		}else {
			Log.log(Level.INFO, "Login check failed due to missing header");			
		}
		return null;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/logout")
	public Response doLogout(@Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		if (headers != null && headers.getRequestHeader(headerNameSessionID) != null && headers.getRequestHeader(headerNameSessionID).size() > 0) { 
			String sessionIdentifier = headers.getRequestHeader(headerNameSessionID).get(0);
			Session session = SessionManagement.getSessionFormIdentifier(sessionIdentifier);
			if (session != null) {
				SessionManagement.destroySession(session);
			}
		}
		Log.log(Level.FINE, "User "+userName+" logged out");
		return Response.status(204).entity("").build();
	}
	

	@GET
	@Path("/getBasePath") 
	public Response getBasePath(@Context HttpHeaders headers) throws Exception {
		if (checkLogin(headers,false) == null) {
			return unauthorizedResponse();
		}
		return Response.status(200).entity(PathFinder.getBasePath()).build();
	}

	private Response unauthorizedResponse() {
		JsonObject error = new JsonObject();
		error.addProperty("error", "You need to be logged in / you are lacking privileges");
		return Response.status(403).entity(error.toString()).type("application/json").build();
	}

	@GET
	@Path("/run/{testname}")
	public Response runTestByName(@PathParam("testname") String testName, @Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,true);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		Log.log(Level.INFO, "User "+userName+" running test " + testName);
		JSONObject obj = Helpers.loadConfig(PathFinder.getSpecificTestPath(testName));
		Test test = Helpers.parseConfig(obj, testName);

		test.start = System.currentTimeMillis();
		Helpers.createRunningFile(test, false);
		Testing.runTestInThread(test, false);

		JsonObject resp = new JsonObject();
		resp.addProperty("name", test.name);
		resp.addProperty("handle", String.valueOf(test.start));

		return Response.status(200).entity(resp.toString()).type("application/json").build();
	}

	@GET
	@Path("/runGroup/{groupname}")
	public Response runTestGroupByName(@PathParam("groupname") String groupName, @Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,true);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		Log.log(Level.INFO, "User "+userName+" running group test " + groupName);
		JSONObject group = Helpers.loadConfig(PathFinder.getSpecificGroupPath(groupName));
		JSONArray tests = (JSONArray) group.get("tests");
		long curMil = System.currentTimeMillis();
		String handle = String.valueOf(curMil);
		Test test = new Test();
		test.description = "Group Test '" + groupName + "' consisting of tests: ";
		test.name = groupName;
		test.start = curMil;
		// Merging Tests
		for (Object testObj : tests) {
			JSONObject testJObj = (JSONObject) testObj;
			String testName = testJObj.getString("name");
			test.description += testName + ",";
			JSONObject objd = Helpers.loadConfig(PathFinder.getSpecificTestPath(testName));
			Test testD = Helpers.parseConfig(objd, testName);
			test.tasks.addAll(testD.tasks);
		}
		Helpers.createRunningFile(test, true);
		Testing.runTestInThread(test, true);

		JsonObject resp = new JsonObject();
		resp.addProperty("name", groupName);
		resp.addProperty("handle", handle);

		return Response.status(200).entity(resp.toString()).type("application/json").build();
	}

	@GET
	@Path("/getResults/{testname}")
	public Response getResultsByName(@PathParam("testname") String testName, @Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		JsonArray resultsArray = new JsonArray();
		try {
			ArrayList<String> listOfFiles = Helpers.getListOfFiles(PathFinder.getTestResultsPath(testName),
					PathFinder.getDataLabel());
			getResultsInternal(testName, resultsArray, listOfFiles, false);
		} catch (Exception e) {
			throw new Exception("Cannot load or parse test result");
		}
		return Response.status(200).entity(resultsArray.toString()).type("application/json").build();
	}

	@GET
	@Path("/getGroupResults/{groupname}")
	public Response getGroupResultsByName(@PathParam("groupname") String groupName, @Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		JsonArray resultsArray = new JsonArray();
		try {
			ArrayList<String> listOfFiles = Helpers.getListOfFiles(PathFinder.getGroupTestResultsPath(groupName),
					PathFinder.getDataLabel());
			getResultsInternal(groupName, resultsArray, listOfFiles, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Cannot load or parse test result - " + e.getMessage());
		}
		return Response.status(200).entity(resultsArray.toString()).type("application/json").build();
	}

	@GET
	@Path("/getLatestResult/{testname}")
	public Response getLatestResultByName(@PathParam("testname") String testName, @Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		ArrayList<String> listOfFiles = Helpers.getListOfFiles(PathFinder.getTestResultsPath(testName), PathFinder.getDataLabel());
		long newest = getNewest(listOfFiles);
		String handle = String.valueOf(newest);
		String path = PathFinder.getSpecificTestResultPath(testName, handle, false);
		return getResultInternal(path);
	}

	@GET
	@Path("/getLatestGroupResult/{groupname}")
	public Response getLatestGroupResultByName(@PathParam("groupname") String groupName, @Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		ArrayList<String> listOfFiles = Helpers.getListOfFiles(PathFinder.getGroupTestResultsPath(groupName),
				PathFinder.getDataLabel());
		long newest = getNewest(listOfFiles);
		String handle = String.valueOf(newest);
		String path = PathFinder.getSpecificTestGroupResultPath(groupName, handle, false);
		return getResultInternal(path);
	}

	@GET
	@Path("/getResult/{testname}/{handle}")
	public Response getLatestResultByName(@PathParam("testname") String testName, @PathParam("handle") String handle, @Context HttpHeaders headers)
			throws Exception {
		TimeUnit.SECONDS.sleep(2);

		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		String path = PathFinder.getSpecificTestResultPath(testName, handle, false);
		return getResultInternal(path);
	}

	@GET
	@Path("/getGroupResult/{groupname}/{handle}")
	public Response getLatestGroupResultByName(@PathParam("groupname") String groupname,
			@PathParam("handle") String handle, @Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		String path = PathFinder.getSpecificTestGroupResultPath(groupname, handle, false);
		return getResultInternal(path);
	}

	@GET
	@Path("/getStatus/{testname}/{handle}")
	public Response getStatusByName(@PathParam("testname") String testName, @PathParam("handle") String handle, @Context HttpHeaders headers)
			throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		String path = PathFinder.getSpecificTestResultPath(testName, handle, false);
		String pathRunning = PathFinder.getSpecificTestResultStatusPath(testName, handle, false);

		return getStatusInternal(path, pathRunning);
	}

	@GET
	@Path("/getGroupStatus/{groupname}/{handle}")
	public Response getGroupStatusByName(@PathParam("groupname") String groupName, @PathParam("handle") String handle, @Context HttpHeaders headers)
			throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		String path = PathFinder.getSpecificTestGroupResultPath(groupName, handle, false);
		String pathRunning = PathFinder.getSpecificTestGroupResultStatusPath(groupName, handle, false);

		return getStatusInternal(path, pathRunning);
	}

	@GET
	@Path("/getTestList")
	public Response getTestList(@Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		JsonArray testsArray = new JsonArray();
		ArrayList<String> listOfFiles = Helpers.getListOfFiles(PathFinder.getTestsPath(), PathFinder.getTestLabel());
		for (String name : listOfFiles) {
			JsonObject test = new JsonObject();
			test.addProperty("name", name.substring(0, name.length() - PathFinder.getTestLabel().length()));
			testsArray.add(test);
		}
		return Response.status(200).entity(testsArray.toString()).type("application/json").build();
	}

	@GET
	@Path("/getTestGroupList")
	public Response getGroupList(@Context HttpHeaders headers) throws Exception {
		String userName = checkLogin(headers,false);
		if ( userName == null) {
			return unauthorizedResponse();
		}
		JsonArray groupsArray = new JsonArray();
		ArrayList<String> listOfFiles = Helpers.getListOfFiles(PathFinder.getGroupsPath(), PathFinder.getGroupLabel());
		for (String name : listOfFiles) {
			String content = Helpers.readFile(PathFinder
					.getSpecificGroupPath(name.substring(0, name.length() - PathFinder.getGroupLabel().length())));
			JsonObject tests = parseTestGroup(name, content);
			groupsArray.add(tests);
		}
		return Response.status(200).entity(groupsArray.toString()).type("application/json").build();
	}

	private Response getResultInternal(String path) throws Exception {
		String result = "";
		try {
			result = Helpers.readFile(path);
		} catch (IOException e) {
			throw new Exception("Cannot find test result!");
		}
		return Response.status(200).entity(result).type("application/json").build();
	}

	private Response getStatusInternal(String path, String pathRunning) throws Exception {
		File runningFile;
		File dataFile;
		runningFile = new File(pathRunning);
		dataFile = new File(path);

		String state = "";
		if (runningFile.exists()) {
			state = "running";
		} else if (dataFile.exists()) {
			state = "done";
		} else {
			throw new Exception("Could not find test!"); 
		}
		JsonObject test = new JsonObject();
		test.addProperty("state", state);
		return Response.status(200).entity(test.toString()).type("application/json").build();
	}

	private void getResultsInternal(String testName, JsonArray resultsArray, ArrayList<String> listOfFiles,
			boolean group) throws Exception {
		for (String handle : listOfFiles) {
			handle = handle.substring(0, handle.length() - PathFinder.getDataLabel().length());
			String resultPath;
			if (group) {
				resultPath = PathFinder.getSpecificTestGroupResultPath(testName, handle, false);
			} else {
				resultPath = PathFinder.getSpecificTestResultPath(testName, handle, false);
			}
			JsonObject result = new JsonObject();
			JsonElement resultJson = new JsonParser().parse(Helpers.readFile(resultPath));
			result.addProperty("handle", handle);

			result.add("result", resultJson);
			resultsArray.add(result);
		}
	}

	private JsonObject parseTestGroup(String name, String content) {
		JsonObject testGroup = new JsonObject();
		String propertyName = name.substring(0, name.length() - PathFinder.getGroupLabel().length());
		testGroup.addProperty("name", propertyName);

		JsonArray testGroupTests = new JsonArray();
		JsonObject test = new JsonParser().parse(content).getAsJsonObject();
		JsonArray tests = (JsonArray) test.get("tests");
		for (Object testObj : tests) {
			JsonObject testO = (JsonObject) testObj;
			testGroupTests.add(testO);
		}
		testGroup.add("tests", testGroupTests);
		return testGroup;
	}

	private long getNewest(ArrayList<String> listOfFiles) {
		long old = 0;
		long newest = 0;
		for (String handle : listOfFiles) {
			long current = Long.valueOf(handle.substring(0, handle.length() - PathFinder.getDataLabel().length()));
			if (current > old) {
				newest = current;
			}
		}
		return newest;
	}
}