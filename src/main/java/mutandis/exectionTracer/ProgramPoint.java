package mutandis.exectionTracer;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public abstract class ProgramPoint {
	


	protected String name;
	protected String lineNo;



	public ProgramPoint(String name, String lineNo) {
		this.name = name;
		this.lineNo=lineNo;

		
	}


	

	public String getName() {
		return name;
	}

	public String getLineNo(){
		return lineNo;
	}


	public abstract String getTraceRecord(JSONArray data) throws CrawljaxException, JSONException;

}
