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
public class SampleRunner {


	private static final int MAX_STATES = 50;
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
	/*	Form register=new Form();
		register.field("file").setValue("C:\\Users\\shabnam\\Desktop\\jolly0\\patterns\\glider.cells");
		InputSpecification inputRegister = new InputSpecification();
		inputRegister.setValuesInForm(register).beforeClickElement("a").withAttribute("id", "runButton");
		crawler.setInputSpecification(inputRegister);
		crawler.setClickOnce(true);
		crawler.setMaximumStates(MAX_STATES);
		crawler.setDepth(2);
		crawler.click("a").withAttribute("id", "runButton");
	*/	crawler.setWaitTimeAfterReloadUrl(2000);
	//	crawler.setWaitTimeAfterEvent(10000);
	//	crawler.setMaximumRuntime(60);
		config.setCrawlSpecification(crawler);
		
		
		
		
		
		
//		System.setProperty("webdriver.firefox.bin" ,"/home/shabnam/program-files/firefox/firefox");
		
		
	/*	crawler = new CrawlSpecification("http://localhost:8080/same-game/same-game.htm");
		crawler.click("td").withAttribute("class", "clickable");
		crawler.setDepth(2);
		crawler.setMaximumStates(MAX_STATES);
	*/	ProxyConfiguration prox = new ProxyConfiguration();
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
	//		cyclo.getCyclCompxRate();
			JSModifyProxyPlugin proxyPlugin = new JSModifyProxyPlugin(astVarInst,cyclo);
			proxyPlugin.excludeDefaults();
			web.addPlugin(proxyPlugin);
			JSVarExecutionTracer tracer = new JSVarExecutionTracer("varinstrumentation");
			config.addPlugin(tracer);
			tracer.setOutputFolder(outputdir);
			
		}
		
		config.addPlugin(web);
	//	config.addPlugin(new RandomClickable());
		config.setProxyConfiguration(prox);
		

		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
