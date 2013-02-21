package runner;

import analyser.JSCyclCompxCalc;
import astModifier.JSModifyProxyPlugin;
import com.crawljax.condition.UrlCondition;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
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
public class SymbolExecTraces {


	private static final int MAX_STATES = 30;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* tracing function calls or variables? */
		boolean traceFunc=true;
		String outputdir = "symbol-output2";
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setOutputFolder(outputdir);
		CrawlSpecification crawler;
		crawler = new CrawlSpecification("http://localhost:8080/symbol/Symbol.html");
		
		crawler.addCrawlCondition("Only crawl symbol game", new UrlCondition("symbol"));
		crawler.click("button");
//		crawler.click("button").withAttribute("id", "bS");
//		crawler.click("div").withAttribute("id", "tiles");

		crawler.setClickOnce(false);

		crawler.setMaximumStates(MAX_STATES);
		crawler.setDepth(4);

		config.setCrawlSpecification(crawler);	
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
		config.setProxyConfiguration(prox);
		

		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
