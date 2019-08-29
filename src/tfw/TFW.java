package tfw;

import java.io.File;
import java.util.logging.Level;

import org.glassfish.jersey.server.ResourceConfig;

import helpers.Log;
import helpers.PathFinder;
import service.UserManagement;

public class TFW extends ResourceConfig {
	public TFW() throws Exception {
		String logBasePath = PathFinder.getBasePath()+File.separator+"logs"+File.separator;
		PathFinder.createFolderPath(logBasePath);
		Log.setup(logBasePath+"tfw.log");
		Log.log(Level.INFO, "Starting TFW - 1.3");
		
		UserManagement.loadUsers();
		
		packages("services");
	}
}
