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
public class TuduExecTraces {


	private static final int MAX_STATES = 40;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* tracing function calls or variables? */
		boolean traceFunc=true;
		String outputdir = "tudu-output2";
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setOutputFolder(outputdir);
		CrawlSpecification crawler;
		crawler = new CrawlSpecification("http://localhost:8080/tudu1/tudu1.html");
		
/*		Form form=new Form();
		Form addList=new Form();
		form.field("j_username").setValue("shabnam");
		form.field("j_password").setValue("123456");
		form.field("dueDate").setValue("10/10/2010");
		form.field("priority").setValue("10");
		InputSpecification input = new InputSpecification();
		input.setValuesInForm(form).beforeClickElement("input").withAttribute("type", "submit");
		input.setValuesInForm(addList).beforeClickElement("a").withAttribute("href", "javascript:addTodo();");
		crawler.setInputSpecification(input);
*/		crawler.click("a");
	//	crawler.click("img").withAttribute("id", "add_trigger_calendar");
	//	crawler.click("img").withAttribute("id", "edit_trigger_calendar");
		crawler.dontClick("a").withAttribute("title", "My info");
		crawler.dontClick("a").withAttribute("title", "Log out");
		crawler.dontClick("a").withAttribute("title", "My Todos");
		crawler.dontClick("a").withAttribute("text", "Cancel");
		crawler.dontClick("a").withAttribute("href", "http://localhost:8080/tudu/secure/backupTodoList.action?listId=ff8081813559c732013559c7709e0005");
		crawler.dontClick("a").withAttribute("href","/tudu/register.action");
		crawler.dontClick("a").withAttribute("href", "http://localhost:8080/tudu/secure/restoreTodoList.action?listId=ff8081813559c732013559c7709e0005");
		crawler.dontClick("a").withAttribute("href", "http://localhost:8080/tudu/secure/myInfo.action");
		crawler.dontClick("a").withAttribute("href","javascript:renderTableListId('ff8081813559c732013559c7709e0005')");
		crawler.setClickOnce(true);
		   
		crawler.setMaximumStates(MAX_STATES);
		crawler.setDepth(4);	
		
		
		config.setCrawlSpecification(crawler);
				
//		System.setProperty("webdriver.firefox.bin" ,"/home/shabnam/program-files/firefox/firefox");
		

	
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
