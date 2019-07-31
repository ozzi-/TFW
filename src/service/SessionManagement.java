package service;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import helpers.Helpers;
import helpers.Log;
import pojo.Session;

public class SessionManagement {
	private static ArrayList<Session> hs = new ArrayList<Session>();
	private static List<Session> hsSafe = Collections.synchronizedList(hs);
	private static final int sessionIDlength = 32;

	
	public static Session createSession(String username, String role) {
		int cleanUpCounter = 0;
		try { 
			Session sessionCreated = new Session(username, createSessionID(), role);
			for (Iterator<Session> iterator = hsSafe.iterator(); iterator.hasNext();) {
				Session sessionInList = iterator.next();
				// only one session per user / cleanup expired sessions
				if (sessionInList.getUsername().equals(username) || !sessionInList.isValid()) {
					if (!sessionInList.isValid()) {
						cleanUpCounter++;
					}
					iterator.remove();
				}
			}
			if (cleanUpCounter > 0) {
				Log.log(Level.INFO, "Cleaned up " + cleanUpCounter + " expired session(s)");
			}
			hs.add(sessionCreated);
			return sessionCreated;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String createSessionID() {
		String session = "";
		try {
			SecureRandom secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG", "SUN");
			int[] ints = secureRandomGenerator.ints(sessionIDlength, 0, Helpers.getAllowedSessionChars().size()).toArray();
			for (int i = 0; i < ints.length; i++) {		
				session += Helpers.getAllowedSessionChars().get(ints[i]); 
			}
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return session;
	}

	public static Session getSessionFormIdentifier(String sessionIdentifier) {
		for (Iterator<Session> iterator = hsSafe.iterator(); iterator.hasNext();) {
			Session sessionInList = iterator.next();
			if (sessionInList.getSessionIdentifier().equals(sessionIdentifier) && sessionInList.isValid()) {
				return sessionInList;
			}
		} 
		return null;
	}

	public static void destroySession(Session session) {
		String userName = session.getUsername();
		destorySessionByUserName(userName);
	}

	public static void destorySessionByUserName(String userName) {
		for (Iterator<Session> iterator = hsSafe.iterator(); iterator.hasNext();) {
			Session sessionInList = iterator.next();
			if (sessionInList.getUsername().equals(userName)) {
				iterator.remove();
				Log.log(Level.FINE, "Destroyed session for user '"+userName+"'");
				return;
			}
		}
		Log.log(Level.WARNING, "Could not destroy session as none found for user '"+userName+"'");
	}
}
