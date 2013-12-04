package mutandis.exectionTracer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mutandis.analyser.VariableTraceAnalyser;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.util.Helper;


public class JSVarExecutionTracer extends ExecutionTracer
	//	implements PreStateCrawlingPlugin, /*OnNewStatePlugin,*/ PostCrawlingPlugin, PreCrawlingPlugin, GeneratesOutput {
	{


	private static final Logger LOGGER = LoggerFactory.getLogger(JSVarExecutionTracer.class.getName());

	public static final String EXECUTIONTRACEDIRECTORY = "varexecutiontrace/";

	/**
	 * @param filename
	 *            How to name the file that will contain the assertions after execution.
	 */
	public JSVarExecutionTracer(String filename) {
		super(filename);
	}

	/**
	 * Initialize the plugin and create folders if needed.
	 * 
	 * @param browser
	 *            The browser.
	 */
	@Override
	public void preCrawling(EmbeddedBrowser browser) {
		try {
			Helper.directoryCheck(getOutputFolder());
			Helper.directoryCheck(getOutputFolder() + EXECUTIONTRACEDIRECTORY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the JavaScript instrumentation array from the webbrowser and writes its contents in
	 * to a file.
	 * 
	 * @param session
	 *            The crawling session.
	 * @param candidateElements
	 *            The candidate clickable elements.
	 */

	@Override
	public void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements) {
        
	
		String filename = getOutputFolder() + EXECUTIONTRACEDIRECTORY + "jsexecutiontrace-";
		
		filename += session.getCurrentState().getName();

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		filename += dateFormat.format(date) + ".txt";

		try {

			LOGGER.info("Reading execution trace");

			LOGGER.info("Parsing JavaScript execution trace");

			
			session.getBrowser().executeJavaScript("sendReally();");
			Thread.sleep(ONE_SEC);

			VarTrace trace = new VarTrace();

			PrintWriter file = new PrintWriter(filename);
			file.write(trace.parse(points));
			file.write('\n');
			file.close();
			
			LOGGER.info("Saved execution trace as " + filename);

			points = new JSONArray();
		} catch (CrawljaxException we) {
			we.printStackTrace();
			LOGGER.error("Unable to get instrumentation log from the browser");
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
/*	@Override
	public void onNewState(CrawlSession session) {
        
	
		String filename = getOutputFolder() + EXECUTIONTRACEDIRECTORY + "jsexecutiontrace-";
		
		filename += session.getCurrentState().getName();

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		filename += dateFormat.format(date) + ".txt";

		try {

			LOGGER.info("Reading execution trace");

			LOGGER.info("Parsing JavaScript execution trace");

			
			session.getBrowser().executeJavaScript("sendReally();");
			Thread.sleep(ONE_SEC);

			VarTrace trace = new VarTrace();

			PrintWriter file = new PrintWriter(filename);
			file.write(trace.parse(points));
			file.write('\n');
			file.close();
			
			LOGGER.info("Saved execution trace as " + filename);

			points = new JSONArray();
		} catch (CrawljaxException we) {
			we.printStackTrace();
			LOGGER.error("Unable to get instrumentation log from the browser");
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	*/
	
	
	@Override
	public List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(getOutputFolder() + EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(getOutputFolder() + EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}

	@Override
	public void postCrawling(CrawlSession session) {
		try {
			PrintStream output = new PrintStream(getOutputFolder() + getInstrumentationFilename());

		
//			List<String> tracefiles = allTraceFiles();
//			String resultFileAndPath=getOutputFolder() + EXECUTIONTRACEDIRECTORY + "..\\" + "variableTraceResult.txt";
			VariableTraceAnalyser varTraceAnalyser=new VariableTraceAnalyser(getOutputFolder(), getJsFilesFolder());
			
			varTraceAnalyser.startAnalysingTraceFiles();
		
			//	arguments.add(getOutputFolder());

			/* Restore the old system.out */
		

			/* close the output file */
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}





}
