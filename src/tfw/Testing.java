package tfw;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.logging.Level;

import org.buildobjects.process.ExternalProcessFailureException;
import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.ProcResult;
import org.buildobjects.process.StartupException;
import org.buildobjects.process.TimeoutException;

import helpers.Helpers;
import helpers.Log;
import helpers.PathFinder;
import persistence.Persistence;
import pojo.Result;
import pojo.Results;
import pojo.Task;
import pojo.Test;

public class Testing {
	
	public static Results runTest(Test test) {
		Results results = new Results();
		results.test = test;
		for (Task task : test.tasks) {
			Result result = runTask(task);
			results.results.add(result);
		}
		
		return results;
	}
	
	public static void runTestInThread(Test test, boolean group, String userName) throws Exception {
		new Thread() {
			public void run() {
				Results results = Testing.runTest(test);
				boolean allPassed = true;
				for (Result result : results.results) {
					System.out.println(result.toJSONString());
					if(!result.pass) {
						allPassed=false;
					}
				}
				if(allPassed && test.successHook != null) {
					Log.log(Level.INFO, "Running success hook '"+test.successHook+"' for test '"+test.name+"'");
					runPostHook(test.successHook);
				}
				if(!allPassed && test.failureHook != null) {
					Log.log(Level.INFO, "Running failure hook '"+test.failureHook+"' for test '"+test.name+"'");
					runPostHook(test.failureHook);
				}
				try {
					Persistence.persistAsJSONFile(test, results, group, userName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private static void runPostHook(String hook) {
		ProcBuilder builder = null;
		try {
			String procPath = isAbsolute(hook)?hook:PathFinder.getTestsPath()+hook;
			builder = new ProcBuilder(procPath).withTimeoutMillis(5*1000);
			builder.run();
		} catch (Exception e) {
			Log.log(Level.WARNING, "Exception running post hook '"+hook+"' - "+e.getMessage());
		}
	}
	
	private static boolean isAbsolute(String path) {
		return new File(path).isAbsolute();
	}

	private static Result runTask(Task task) {
		Log.log(Level.INFO, "Starting test "+task.name);

		Result result = new Result();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream errout = new ByteArrayOutputStream();
		long startTime = System.nanoTime();
		result.timestampStart=System.currentTimeMillis();
		ProcBuilder builder = null;
		try {		
			String procPath = isAbsolute(task.path)?task.path:PathFinder.getTestsPath()+task.path;
			builder = new ProcBuilder(procPath)
					.withArgs(Helpers.getStringArray(task.args))
					.withTimeoutMillis(task.timeoutInSeconds*1000)
					.withOutputStream(out)
					.withErrorStream(errout);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long endTime = 0;

		try {
			ProcResult res = builder.run();
			endTime = System.nanoTime();
			result.returnCode = res.getExitValue();
			result.text = "Script executed successfully";
			result.pass = true;
		} catch (TimeoutException ex) {
			endTime = System.nanoTime();
			result.text = "Script execution longer than timeout";
		} catch (StartupException ex) {
			result.text = "StartupException: "+ex.getMessage();
		} catch (ExternalProcessFailureException ex) {
			result.text = "Script returned non zero return code \""+ex.getExitValue()+"\"";
		}
		
		endTime = System.nanoTime();
		result.output = out.toString();
		result.errorOutput = errout.toString();
		result.timestampEnd=System.currentTimeMillis();
		long timeElapsed = (endTime - startTime) / 1000000;
		result.runTimeInMS = timeElapsed;
		result.name = task.name;
		
		Log.log(Level.INFO, "Finished test "+task.name+" in "+timeElapsed+" ms - pass = "+result.pass);

		return result;
	}
}
