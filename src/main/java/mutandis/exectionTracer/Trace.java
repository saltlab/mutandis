package mutandis.exectionTracer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public abstract class Trace {




	public abstract FuncCallProgramPoint addFuncCallProgramPoint(String name, String lineNo);
	public abstract ReadVarProgramPoint addReadVarProgramPoint(String name, String lineNo);

	public abstract String parse(JSONArray jsonObject) throws JSONException, CrawljaxException; 
	

	

}
