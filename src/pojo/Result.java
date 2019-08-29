package pojo;
import com.google.gson.JsonObject;

import helpers.Helpers;

public class Result {
	public Result() {
		this.pass = false; // default case is we fail
	}
	
	public String text;
	public String name;
	public String output;
	public String errorOutput;
	public int returnCode;
	public boolean pass;
	public long runTimeInMS;
	public long timestampStart;
	public long timestampEnd;

	@Override
	public String toString() {
		return (pass ? "✓" : "X") + " \"" + name + "\" " + text + " (" + runTimeInMS + " ms)";
	}
	
	public JsonObject toJsonObject() {
		JsonObject res = new JsonObject();
		res.addProperty("name", name);
		res.addProperty("passed", pass);
		res.addProperty("description", text);
		res.addProperty("output", output);
		res.addProperty("errorOutput", errorOutput);
		res.addProperty("runTimeInMS", runTimeInMS);
		res.addProperty("startTimestamp", timestampStart);
		res.addProperty("endTimestamp", timestampEnd);
		res.addProperty("startDate", Helpers.getDateFromUnixTimestamp(timestampStart));
		res.addProperty("endDate", Helpers.getDateFromUnixTimestamp(timestampEnd));
		return res;
	}
	
	public String toJSONString() {
		return toJsonObject().toString();
	}
}
