package exectionTracer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public class FuncCallTrace extends Trace {
	

	private ArrayList<FuncCallProgramPoint> programPoints;

	
	public FuncCallTrace() {
		programPoints = new ArrayList<FuncCallProgramPoint>();
	}


	@Override
	public FuncCallProgramPoint addFuncCallProgramPoint(String name, String lineNo) {
	

		FuncCallProgramPoint p = new FuncCallProgramPoint(name,lineNo);
		programPoints.add(p);
		return p;
	}

	@Override
	public ReadVarProgramPoint addReadVarProgramPoint(String name, String lineNo){
		return null;
	}
	@Override
	public String parse(JSONArray jsonObject) throws JSONException, CrawljaxException {
	
		StringBuffer result = new StringBuffer();
		for (int j = 0; j < jsonObject.length(); j++) {
			
			JSONArray value = jsonObject.getJSONArray(j);
			String programPointName = value.getString(0);
			String lineNo = value.getString(1);
			FuncCallProgramPoint prog = addFuncCallProgramPoint(programPointName,lineNo);
			
			/* output all the variable values */
			result.append(prog.getTraceRecord(value.getJSONArray(2)));
		
		}

		return result.toString();
	}

}
