package mutandis.exectionTracer;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import com.crawljax.core.CrawljaxException;


public class VarTrace extends Trace {

	private ArrayList<ReadVarProgramPoint> programPoints;

	
	public VarTrace() {
		programPoints = new ArrayList<ReadVarProgramPoint>();
	}


	@Override
	public ReadVarProgramPoint addReadVarProgramPoint(String name, String lineNo) {

		ReadVarProgramPoint p = new ReadVarProgramPoint(name,lineNo);
		programPoints.add(p);
		return p;
	}

	@Override
	public FuncCallProgramPoint addFuncCallProgramPoint(String name, String lineNo){
		return null;
	}

	@Override
	public String parse(JSONArray jsonObject) throws JSONException, CrawljaxException {
	
		StringBuffer result = new StringBuffer();
		for (int j = 0; j < jsonObject.length(); j++) {
			
			JSONArray value = jsonObject.getJSONArray(j);
			String programPointName = value.getString(0);
			String lineNo = value.getString(1);
			ReadVarProgramPoint prog = addReadVarProgramPoint(programPointName,lineNo);
			
			/* output all the variable values */
			result.append(prog.getTraceRecord(value.getJSONArray(2)));
		
		}

		return result.toString();
	}



}
