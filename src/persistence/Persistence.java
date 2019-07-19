package persistence;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import helpers.Helpers;
import helpers.Log;
import helpers.PathFinder;
import pojo.Result;
import pojo.Results;
import pojo.Test;

public class Persistence {
	public static String persistAsJSON(Test test, Results results, String userName) {
		JsonObject res = new JsonObject();
		Log.log(Level.FINE, "Persisting test "+test.name+" as JSON");
		res.addProperty("testName", test.name);
		res.addProperty("testStartTimestamp", test.start);
		res.addProperty("testRunBy", userName);
		res.addProperty("testStartString", Helpers.getDateFromUnixTimestamp(test.start));
		res.addProperty("description", test.description);
			JsonArray resultsArray = new JsonArray();
			for (Result result : results.results) {
				resultsArray.add(result.toJsonObject());
			}
			res.add("results", resultsArray);	
		return res.toString();
	}

	
	public static String persistAsJSONFile(Test test, Results results, boolean group, String userName) throws Exception {
		String json = persistAsJSON(test, results, userName);
		String handle = String.valueOf(test.start);
		String fullPersistencePath;
		if(group) {
			fullPersistencePath = PathFinder.getSpecificTestGroupResultPath(test.name, String.valueOf(test.start),true);
		}else {
			fullPersistencePath = PathFinder.getSpecificTestResultPath(test.name, String.valueOf(test.start),true);			
		}
		Log.log(Level.FINE, "Trying to persist as json file");
        try {
			Files.write(Paths.get(fullPersistencePath), json.getBytes());
			Log.log(Level.FINE, "Success persisting as json file ("+fullPersistencePath+")");
		} catch (IOException e) {
			System.err.println("Error while trying to persist results under '"+fullPersistencePath+"'. Reason: "+e.getClass().getCanonicalName());
			System.exit(4);
		}
        String fullPersistencePathRunning;
        if(group) {
        	fullPersistencePathRunning = PathFinder.getSpecificTestGroupResultStatusPath(test.name, handle, true);        	
        }else {
        	fullPersistencePathRunning = PathFinder.getSpecificTestResultStatusPath(test.name, handle, true);
        }
		File runningFile = new File(fullPersistencePathRunning);
		Log.log(Level.FINE, "Deleting running file");
		runningFile.delete();
		
        return fullPersistencePath;
	}	
}