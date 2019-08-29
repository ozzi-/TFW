package helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pojo.Task;
import pojo.Test;

public class Helpers {

	private static final ArrayList<String> allowedChars = new ArrayList<String>();

	public static ArrayList<String> getAllowedSessionChars() {
		if (allowedChars.size() == 0) {
			allowedChars.addAll(getASCIICharacters(48, 57)); // 0-9
			allowedChars.addAll(getASCIICharacters(65, 90)); // A-Z
			allowedChars.addAll(getASCIICharacters(97, 122)); // a-z
		}
		return allowedChars;
	}

	private static ArrayList<String> getASCIICharacters(int from, int to) {
		ArrayList<String> chars = new ArrayList<String>();
		for (int i = from; i <= to; i++) {
			chars.add(String.valueOf((char) i));
		}
		return chars;
	}

	public static SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String sanitizeFilename(String inputName) {
		return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
	}

	public static File[] getListOfFiles(String path) {
		File folder = new File(path);
		return folder.listFiles();
	}

	public static ArrayList<String> getListOfFiles(String path, String endsWith) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		ArrayList<String> listOfFilesFiltered = new ArrayList<String>();
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(endsWith)) {
					listOfFilesFiltered.add(listOfFiles[i].getName());
				}
			}
		}
		return listOfFilesFiltered;
	}

	public static String readFile(String path) throws Exception {
		Log.log(Level.FINEST, "Reading file " + path);
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (Exception e) {
			throw new Exception("Could not read file: " + path);
		}
		return new String(encoded, "UTF-8");
	}

	public static JSONObject loadConfig(String path) throws Exception {
		String json = "";
		try {
			json = readFile(path);
		} catch (IOException e) {
			throw new Exception("Cannot load test json file in loadConfig!");
		}
		final JSONObject obj;
		try {
			obj = new JSONObject(json);
			return obj;
		} catch (Exception e) {
			throw new Exception(
					"Error parsing test json file (" + path + ")   \"" + e.getMessage() + "\" in loadConfig");
		}
	}

	public static String getDateFromUnixTimestamp(long timestamp) {
		Date date = new Date(timestamp);
		return dtf.format(date);
	}

	public static String getDateTime() {
		return dtf.format(System.currentTimeMillis());
	}

	public static Test parseConfig(JSONObject cfg, String name) throws Exception {
		JSONObject settings;
		JSONObject test;
		Test testElem = new Test();
		Log.log(Level.FINE, "Parsing config file for " + name);
		try {
			settings = (JSONObject) cfg.get("settings");
			try {
				testElem.successHook = settings.getString("successhook");
			} catch (Exception e) {
			}
			try {
				testElem.failureHook = settings.getString("failurehook");
			} catch (Exception e) {
			}
			test = (JSONObject) cfg.get("test");
			testElem.name = name;
			testElem.description = test.getString("description");
			JSONArray tasks = (JSONArray) test.get("tasks");
			for (Object taskObj : tasks) {
				JSONObject task = (JSONObject) taskObj;
				Task taskElem = new Task();
				taskElem.name = task.getString("name");
				taskElem.path = task.getString("path");
				if(task.has("args")) {
					JSONArray args = task.getJSONArray("args");
					for (Object argObj : args) {
						taskElem.args.add((String)argObj);
					}
				}
				if (task.has("timeout")) {
					taskElem.timeoutInSeconds = task.getInt("timeout");
				}
				testElem.tasks.add(taskElem);
			}
		} catch (Exception e) {
			if (e instanceof JSONException) {
				throw new Exception("Could not load test due to JSON error: " + e.getMessage());
			} else {
				throw new Exception("Could not load settings and / or tests from test json");
			}
		}
		return testElem;
	}

	public static String[] getStringArray(ArrayList<String> arr) {
		String str[] = new String[arr.size()];
		for (int j = 0; j < arr.size(); j++) {
			str[j] = arr.get(j);
		}
		return str;
	}

	/**
	 * Creates a running file which indicates a test still being executed This file
	 * is deleted as soon as the test is done, which creates a .data result file
	 * 
	 * @param test
	 * @throws Exception
	 * @throws IOException
	 */
	public static void createRunningFile(Test test, boolean group) throws Exception {
		String basePath;
		if (group) {
			basePath = PathFinder.getSpecificTestGroupResultStatusPath(test.name, String.valueOf(test.start), true);
		} else {
			basePath = PathFinder.getSpecificTestResultStatusPath(test.name, String.valueOf(test.start), true);
		}
		File runningFile = new File(basePath);
		try {
			runningFile.createNewFile();
		} catch (IOException e) {
			throw new Exception("Could not create running file: " + e.getMessage() + " - " + basePath);
		}
	}
}
