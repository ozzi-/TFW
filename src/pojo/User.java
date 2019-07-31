package pojo;

import helpers.Crypto;

public class User {
	private String username;
	private String password;
	private int pwLength;
	private String role;

	public String toString() {
		return username;
	}

	public String getPassword() {
		return password.toUpperCase();
	}
	
	public void setPassword(String password) {
		this.pwLength=password.length();
		this.password = Crypto.saltHashString(password);
	}

	public void setPasswordHashed(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String toJSON() {
		return "{ "+System.lineSeparator()
				+ "	\"username\" : \""+getUsername()+"\","+System.lineSeparator()
				+ "	\"password\" : \""+getPassword()+"\","+System.lineSeparator()
				+ "	\"role\" : \""+getRole()+"\""+System.lineSeparator()
				+"}";
	}

	public int getPwLength() {
		return pwLength;
	}

}
