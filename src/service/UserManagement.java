package service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.logging.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import helpers.Helpers;
import helpers.Log;
import helpers.PathFinder;
import pojo.User;

public class UserManagement {
	public static ArrayList<User> users = new ArrayList<User>();
	
	public static User parseUserLoginJSON(String userJson) throws Exception {
		JsonElement userJO;
		try {
			userJO =  new JsonParser().parse(userJson).getAsJsonObject();			
		}catch (Exception e) {
			throw new Exception("Error parsing login json - "+e.getMessage());
		}
		User user = new User();
		try {
			user.setUsername(userJO.getAsJsonObject().get("username").getAsString());
			user.setPassword(userJO.getAsJsonObject().get("password").getAsString());			
		}catch (Exception e) {
			throw new Exception("Error parsing username / password - keys not found");
		}
		return user;
	}
	
	public static void loadUsers() throws Exception {
		users = new ArrayList<User>();
		Log.log(Level.FINE, "Loading users");
		String usersJson;
		String usersFile = PathFinder.getBasePath()+"users.json";
		try {
			usersJson = Helpers.readFile(usersFile);
		} catch (Exception e) {
			throw new Exception("Cannot load users file "+usersFile);
		}
		JsonArray usersJA =  new JsonParser().parse(usersJson).getAsJsonArray();
		for (JsonElement userJO : usersJA) {
			User user = new User();
			user.setUsername(userJO.getAsJsonObject().get("username").getAsString());
			user.setPasswordHashed(userJO.getAsJsonObject().get("password").getAsString());
			String role = userJO.getAsJsonObject().get("role").getAsString();
			if(!role.equals("rw") && !role.equals("r")) {
				Log.log(Level.WARNING, "Unknown role set for user '"+user.getUsername()+"'. Setting 'r'");
				role="r";
			}
			user.setRole(role);
			Log.log(Level.FINE, "Loaded user '"+user.getUsername()+"' with role '"+user.getRole()+"'");
			users.add(user);
		}
	}

	public static User checkLogin(User loginUser) {
		for (User user : users) {
			if(MessageDigest.isEqual(user.getUsername().getBytes(),loginUser.getUsername().getBytes())) {
				Log.log(Level.FINE, "Username '"+loginUser.getUsername()+"' found.");
				if(MessageDigest.isEqual(user.getPassword().getBytes(),loginUser.getPassword().getBytes())) {
					Log.log(Level.INFO, "Password for username '"+loginUser.getUsername()+"' matches.");
					loginUser.setRole(user.getRole());
					return loginUser;
				}else {
					Log.log(Level.WARNING, "Unsuccessful login attempt with username '"+loginUser.getUsername()+"' - wrong password.");				
				}
			}else {
				Log.log(Level.WARNING, "Username '"+loginUser.getUsername()+"' not found.");				
			}
		}
		return null;
	}
}