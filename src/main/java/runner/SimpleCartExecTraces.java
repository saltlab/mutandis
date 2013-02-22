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
public class SimpleCartExecTraces {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* tracing function calls or variables? */
		boolean traceFunc=false;
		String outputdir = "simplecart-output2";
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setOutputFolder(outputdir);
		CrawlSpecification crawler;
		crawler = new CrawlSpecification("localhost:8080/simplecart/test/index.html");

		crawler.setWaitTimeAfterReloadUrl(2000);
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
