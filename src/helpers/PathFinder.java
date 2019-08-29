package helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathFinder {
	private static String dataLabel = ".data";
	private static String testLabel = ".test";
	private static String groupLabel = ".group";
	private static String stateLabel = ".running";

	private static String dataFolder = "results";
	private static String testFolder = "tests";
	private static String groupFolder = "groups";

	public static String getDataLabel() {
		return dataLabel;
	}
	public static String getTestLabel() {
		return testLabel;
	}
	public static String getGroupLabel() {
		return groupLabel;
	}
	public static String getDataFolderName() {
		return dataFolder;
	}
	public static String getTestFolderName() {
		return testFolder;
	}
	public static String getGroupFolderName() {
		return groupFolder;
	}
		

	/**
	 * @return i.E: "C:\Users\HIN\Desktop\"
	 */
	public static String getBasePath() throws Exception {
		String basePath ="";
		if(System.getProperty("os.name").toLowerCase().contains("windows")) {
			basePath = System.getenv("APPDATA")+File.separator+"TFW"+File.separator;
		}else {
			basePath = "/var/lib/TFW/";
		}
		createFolderPath(basePath);
		return basePath;
	}

	
	/**
	 * @return "C:\Users\TFW\Desktop\tests\"
	 */
	public static String getTestsPath() throws Exception {
		String pathString = getBasePath()+testFolder+File.separator;
		createFolderPath(pathString);
		return pathString;
	}
	
	/**
	 * @return "C:\Users\TFW\Desktop\groups\"
	 */
	public static String getGroupsPath() throws Exception {
		String pathString = getBasePath()+groupFolder+File.separator;
		createFolderPath(pathString);
		return pathString;
	}

	/**
	 * @return "C:\Users\TFW\Desktop\tests\[name].test"
	 */
	public static String getSpecificTestPath(String testName) throws Exception {
		return getTestsPath() + testName + testLabel;
	}

	/**
	 * @return "C:\Users\TFW\Desktop\groups\[name].group"
	 */
	public static String getSpecificGroupPath(String groupName) throws Exception {
		return getGroupsPath() + groupName + groupLabel;
	}

	
	/**
	 * @return "C:\Users\TFW\Desktop\data\[name]\"
	 */
	public static String getTestResultsPath (String testName) throws Exception {
		String path = getBasePath() + dataFolder + File.separator + testName + File.separator;
		return path;
	}
	
	/**
	 * @return "C:\Users\TFW\Desktop\data\groups\[name]\"
	 */	
	public static String getGroupTestResultsPath(String groupName) throws Exception {
		String path = getBasePath() + dataFolder + File.separator + groupFolder + File.separator + groupName + File.separator;
		return path;
	}
	

	/**
	 * @return "C:\Users\TFW\Desktop\data\[name]\[handle].data"
	 * @throws Exception 
	 */
	public static String getSpecificTestResultPath(String testName, String handle, boolean createPath) throws Exception {
		String path = getTestResultsPath(testName);
		if (createPath) {
			createFolderPath(path);
		}
		return path + handle + dataLabel;
	}
	
	/**
	 * @return "C:\Users\TFW\Desktop\data\groups\[name]\[handle].data"
	 * @throws Exception 
	 */
	public static String getSpecificTestGroupResultPath(String groupName, String handle, boolean createPath) throws Exception {
		String path = getGroupTestResultsPath(groupName);
		if (createPath) {
			createFolderPath(path);
		}
		return path + handle + dataLabel;
	}

	/**
	 * @return "C:\Users\TFW\Desktop\data\[name]\[handle].status"
	 * @throws Exception 
	 */
	public static String getSpecificTestResultStatusPath(String testName, String handle, boolean createPath) throws Exception {
		String path = getTestResultsPath(testName);
		if (createPath) {
			createFolderPath(path);
		}
		return path + handle + stateLabel;
	}
	
	/**
	 * @return "C:\Users\TFW\Desktop\data\groups\[name]\[handle].status"
	 * @throws Exception 
	 */
	public static String getSpecificTestGroupResultStatusPath(String testName, String handle, boolean createPath) throws Exception {		
		String path = getGroupTestResultsPath(testName);
		if (createPath) {
			createFolderPath(path);
		}
		return path + handle + stateLabel;
	}
	
	public static void createFolderPath(String pathString) throws Exception {
		Path path = Paths.get(pathString);
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new Exception("Could not greate path '"+pathString+"' - due to: "+e.getMessage());
		}
	}
}
