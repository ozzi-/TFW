package pojo;

import com.google.gson.JsonObject;

public class Session {
	private static long sessionLifeTimeInS = 3600;
	private String username;
	private String sessionIdentifier;
	private long created;
	private String role;

	public Session(String username, String sessionIdentifier, String role) {
		this.setUsername(username);
		this.setSessionIdentifier(sessionIdentifier);
		this.setRole(role);
		this.setCreated(System.currentTimeMillis() / 1000L);
	}
	
    @Override
	public boolean equals(Object o) {
        if(o instanceof Session){
            Session other = (Session) o;
            return other.sessionIdentifier.equals(this.sessionIdentifier);
        }
        return false;
	}
    
	public JsonObject toJSONObj() {
		JsonObject session = new JsonObject();
		session.addProperty("username", username);
		session.addProperty("sessionIdentifier", sessionIdentifier);
		session.addProperty("role", role);
		return session;
	}
    
	public boolean isValid() {
		long now = System.currentTimeMillis() / 1000L;
		boolean valid = false;
		if(getCreated()+sessionLifeTimeInS > now) {
			valid = true;
		} 
		return valid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSessionIdentifier() {
		return sessionIdentifier;
	}

	public void setSessionIdentifier(String sessionIdentifier) {
		this.sessionIdentifier = sessionIdentifier;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
