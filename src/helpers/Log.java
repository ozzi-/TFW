package helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	static Logger logger;
	private static boolean pathSet = false;
	private static ArrayList<LogEntry> backLog = new ArrayList<LogEntry>();

	public static void setup(String path) {
		pathSet = true; 
		logger = Logger.getLogger(Log.class.getName());
		logger.setLevel(Level.INFO);

		FileHandler fileH;
		try {
			fileH = new FileHandler(path, true);
			SimpleFormatter formatterTxt = new SimpleFormatter();
			fileH.setFormatter(formatterTxt);
			logger.addHandler(fileH);
		} catch (SecurityException e) {
			e.printStackTrace();
			System.err.println("Security Exception while creating logger. Exiting (2)");
			System.exit(2);
		} catch (java.nio.file.NoSuchFileException e) {
			e.printStackTrace();
			System.err.println(
					"Could not write log file, have you set a invalid logPath in config.properties? Exiting (2)");
			System.exit(2);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("IO Exception while creating logger. Exiting (2)");
			System.exit(2);
		}
	}

	public static void setLogPath(String path) {
		pathSet = true;
		setup(path);
		for (LogEntry logEntry : backLog) {
			log(logEntry.getLevel(), logEntry.getMessage());
		}
	}

	public static void log(Level level, String log) {
		if (pathSet) {
			logger.log(level, log);
			if (level.equals(Level.SEVERE)) {
				System.err.println(level.getName() + ": " + log);
			} else {
				System.out.println(level.getName() + ": " + log);
			}
		} else {
			backLog.add(new LogEntry(level, log));
		}
	}
}
