package exectionTracer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.util.Helper;

public abstract class ExecutionTracer
				implements PreStateCrawlingPlugin, /*OnNewStatePlugin,*/PostCrawlingPlugin, PreCrawlingPlugin, GeneratesOutput{
	
	
	protected static final int ONE_SEC = 1000;

	protected static String outputFolder;
	protected static String instrumentationFilename;

	protected static JSONArray points = new JSONArray();





	/**
	 * @param filename
	 *            How to name the file that will contain the assertions after execution.
	 */
	public ExecutionTracer(String filename) {
		instrumentationFilename = filename;
	}

	/**
	 * Initialize the plugin and create folders if needed.
	 * 
	 * @param browser
	 *            The browser.
	 */
	@Override
	public abstract void preCrawling(EmbeddedBrowser browser);

	

	/**
	 * Retrieves the JavaScript instrumentation array from the webbrowser and writes its contents in
	 *  to a file.
	 * 
	 * @param session
	 *            The crawling session.
	 * @param candidateElements
	 *            The candidate clickable elements.
	 */

	@Override
	public abstract void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements);
	
	
	
	
	
	
/*	@Override
	public abstract void onNewState(CrawlSession session);
  */     
	
		
	
	
	
	
	
	
	
	
	
	
	/**
	 * Get a list with all trace files in the executiontrace directory.
	 * 
	 * @return The list.
	 */
	public abstract List<String> allTraceFiles();

	@Override
	public abstract void postCrawling(CrawlSession session);
/*		try {
			PrintStream output = new PrintStream(getOutputFolder() + getInstrumentationFilename());

		
			PrintStream oldOut = System.out;
	
			System.setOut(output);


			List<String> arguments = allTraceFiles();

		
			arguments.add(getOutputFolder());



	
			System.setOut(oldOut);

	
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
*/
	/**
	 * @return Name of the assertion file.
	 */
	public String getInstrumentationFilename() {
		return instrumentationFilename;
	}

	@Override
	public String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	@Override
	public void setOutputFolder(String absolutePath) {
		outputFolder = absolutePath;
	}

	/**
	 * 
	 * @param string
	 *            The JSON-text to save.
	 */
	public static void addPoint(String string) {
		JSONArray buffer = null;
		try {
			buffer = new JSONArray(string);
			for (int i = 0; i < buffer.length(); i++) {
				points.put(buffer.get(i));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}


}
