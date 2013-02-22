package runner;


import analyser.JSCyclCompxCalc;
import astModifier.JSModifyProxyPlugin;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.plugins.webscarabwrapper.WebScarabWrapper;

import exectionTracer.AstFunctionCallInstrumenter;
import exectionTracer.AstVarInstrumenter;
import exectionTracer.JSFuncExecutionTracer;
import exectionTracer.JSVarExecutionTracer;

/**
 * sample runner for information extraction phase
 * @author shabnam
 *
 */
public class JqueryExecTraces {


	private static final int MAX_STATES = 10;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* tracing function calls or variables? */
		boolean traceFunc=false;
		String outputdir = "jquery2-output2";
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setOutputFolder(outputdir);
		CrawlSpecification crawler;
		crawler = new CrawlSpecification("http://localhost:8080/same-game/same-game.htm");
		crawler.setWaitTimeAfterEvent(20);
		config.setCrawlSpecification(crawler);
				
//		System.setProperty("webdriver.firefox.bin" ,"/home/shabnam/program-files/firefox/firefox");
		
		crawler.click("td").withAttribute("class", "clickable");
		crawler.setClickOnce(true);
		crawler.setDepth(2);
		crawler.setMaximumStates(MAX_STATES);
		ProxyConfiguration prox = new ProxyConfiguration();
		WebScarabWrapper web = new WebScarabWrapper();
		if(traceFunc){
			AstFunctionCallInstrumenter astfuncCallInst = new AstFunctionCallInstrumenter();
			JSCyclCompxCalc cyclo=new JSCyclCompxCalc(outputdir);
			JSModifyProxyPlugin proxyPlugin = new JSModifyProxyPlugin(astfuncCallInst,cyclo);
			proxyPlugin.excludeDefaults();
			web.addPlugin(proxyPlugin);
			JSFuncExecutionTracer tracer = new JSFuncExecutionTracer("funcinstrumentation");
			config.addPlugin(tracer);
			tracer.setOutputFolder(outputdir);
			
		}
		else{
			AstVarInstrumenter astVarInst = new AstVarInstrumenter();
			JSCyclCompxCalc cyclo=new JSCyclCompxCalc(outputdir);
			JSModifyProxyPlugin proxyPlugin = new JSModifyProxyPlugin(astVarInst,cyclo);
			proxyPlugin.excludeDefaults();
			web.addPlugin(proxyPlugin);
			JSVarExecutionTracer tracer = new JSVarExecutionTracer("varinstrumentation");
			config.addPlugin(tracer);
			tracer.setOutputFolder(outputdir);
			
		}
		
		config.addPlugin(web);
		config.addPlugin(new RandomClickable());
		config.setProxyConfiguration(prox);
		

		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
