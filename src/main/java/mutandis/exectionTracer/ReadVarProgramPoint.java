package mutandis.exectionTracer;


import org.json.JSONArray;
import org.json.JSONException;
import com.crawljax.core.CrawljaxException;

public class ReadVarProgramPoint extends ProgramPoint {
	
	






	public ReadVarProgramPoint(String name, String lineNo) {
		super(name, lineNo);

		
	}



	@Override
	public String getTraceRecord(JSONArray data) throws CrawljaxException, JSONException {
		
		StringBuffer result = new StringBuffer();

		result.append(name + "::" + lineNo);

			for (int i = 0; i < data.length(); i++) {
				result.append("\n");
				JSONArray item = data.getJSONArray(i);

				result.append(item.get(0) + "::" +
									item.get(1) + "::" + item.get(2) + "::" + item.get(3));
					
				
			}
				
		result.append("\n");
		result.append("================================================");	
		result.append("\n");

		return result.toString();
	}

}
