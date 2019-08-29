
package helpers;

import java.util.logging.Level;

public class LogEntry {
	private Level level;
	private String message;

	public LogEntry(Level level, String message) {
		this.setLevel(level);
		this.setMessage(message);
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
