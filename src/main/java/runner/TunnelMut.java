package runner;

import org.openqa.selenium.WebDriver;

import mutator.DomJsCodeLevelVisitor;
import mutator.FunctionNodeVisitor;
import mutator.JSSpecificVisitor;
import mutator.TestResults;
import analyser.FunctionSelector;
import analyser.VariableSelector;
import astModifier.JSModifyProxyPlugin;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.plugins.webscarabwrapper.WebScarabWrapper;

public class TunnelMut {
	
	private static final int MAX_STATES = 5;
	/**sample runner for actual mutation phase
	 * @param args
	 */
	public static void main(String[] args) {
		boolean varMutation=true;
		boolean branchMutation=false;
		boolean jsSpecificMutation=false;
		boolean DomJsMuttaion=false;
		

//		crawler.setWaitTimeAfterReloadUrl(30000);
		String outputdir="tunnel1-output2";
		String htmlAdd="http://localhost:8080/tunnel/tunnel.htm";
		if(varMutation){
			varMutStarter(outputdir, htmlAdd);
		}
		else if(branchMutation){
			branchMuStarter(outputdir, htmlAdd);		
		}
		else if(jsSpecificMutation){
			jsSpecMutStarter(outputdir, htmlAdd);
		}
		else if(DomJsMuttaion){
			jsDomMutStarter(outputdir, htmlAdd);
		}
		
		




	}
	
	public static void varMutStarter(String outputdir, String htmlAdd){
		
		ProxyConfiguration prox = new ProxyConfiguration();
		JSModifyProxyPlugin proxyPlugin = new JSModifyProxyPlugin(outputdir);
		for(int i=0;i<20;i++){
			try{
	//		System.setProperty("webdriver.firefox.bin" ,"/home/shabnam/program-files/firefox/firefox");
				CrawljaxConfiguration config = new CrawljaxConfiguration();
				config.setOutputFolder(outputdir);
				CrawlSpecification crawler = new CrawlSpecification(htmlAdd);	
			
/*				crawler.click("p").withAttribute("id", "welcome");
				crawler.setClickOnce(true);
				crawler.setMaximumStates(MAX_STATES);
				crawler.setDepth(2);
				crawler.setWaitTimeAfterEvent(1000);
*/				
				WebScarabWrapper web = new WebScarabWrapper();
				FunctionSelector funcSel=new FunctionSelector(outputdir);
				VariableSelector varselec=new VariableSelector(outputdir);		
				String funcname=funcSel.getSelectedFunctionNameandPath();
				String varname=varselec.getSelectedVariable(funcname);
				while(varname==""){
					funcname=funcSel.getSelectedFunctionNameandPath();
					varname=varselec.getSelectedVariable(funcname);
				}
//				funcname=funcname.split("::")[1];
				FunctionNodeVisitor funcNodeVis=new FunctionNodeVisitor(funcname,varname);
				proxyPlugin.setJSModifyProxyPluginForFuncVis(funcNodeVis);
				proxyPlugin.excludeDefaults();
				web.addPlugin(proxyPlugin);
		//		TestResults results=new TestResults(outputdir);
		//		config.addPlugin(results);
				config.addPlugin(web);
				
				
				config.setProxyConfiguration(prox);
				config.setCrawlSpecification(crawler);
		
				CrawljaxController crawljax = new CrawljaxController(config);	
				crawljax.run();
	
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void branchMuStarter(String outputdir, String htmlAdd){

		ProxyConfiguration prox = new ProxyConfiguration();
		JSModifyProxyPlugin proxyPlugin = new JSModifyProxyPlugin(outputdir);
		
		for(int i=0;i<20;i++){
			try{
		
				CrawljaxConfiguration config = new CrawljaxConfiguration();
				config.setOutputFolder(outputdir);
				CrawlSpecification crawler = new CrawlSpecification(htmlAdd);	
				WebScarabWrapper web = new WebScarabWrapper();
				FunctionSelector funcSel=new FunctionSelector(outputdir);	
				String funcname=funcSel.getCycloRankSelectedfunction();
		//		funcname=funcname.split("::")[1];
				FunctionNodeVisitor funcNodeVis=new FunctionNodeVisitor(funcname);
				proxyPlugin.setJSModifyProxyPluginForFuncVis(funcNodeVis);
				proxyPlugin.excludeDefaults();
				web.addPlugin(proxyPlugin);
		//		TestResults results=new TestResults(outputdir);
		//		config.addPlugin(results);
				config.addPlugin(web);
				config.setProxyConfiguration(prox);
				
			
		//		crawler.setWaitTimeAfterReloadUrl(1000);
				
				config.setCrawlSpecification(crawler);
				CrawljaxController crawljax = new CrawljaxController(config);
				crawljax.run();
			}
			
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void jsSpecMutStarter(String outputdir, String htmlAdd){
	
		ProxyConfiguration prox = new ProxyConfiguration();
		JSModifyProxyPlugin proxyPlugin = new JSModifyProxyPlugin(outputdir);
		
		
		for(int i=0;i<20;i++){
			try{
				CrawljaxConfiguration config = new CrawljaxConfiguration();
				CrawlSpecification crawler = new CrawlSpecification(htmlAdd);	
				prox = new ProxyConfiguration();
				WebScarabWrapper web = new WebScarabWrapper();
				JSSpecificVisitor jsVis=new JSSpecificVisitor();
				proxyPlugin.setJSModifyProxyPluginForJsSpecVis(jsVis, i);
				proxyPlugin.excludeDefaults();
				web.addPlugin(proxyPlugin);
				config.addPlugin(web);
				config.setProxyConfiguration(prox);
				
		
		//		crawler.setWaitTimeAfterReloadUrl(1000);
				
				config.setCrawlSpecification(crawler);
				CrawljaxController crawljax = new CrawljaxController(config);
				crawljax.run();
			}	
			catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void jsDomMutStarter(String outputdir, String htmlAdd){
	
		ProxyConfiguration prox = new ProxyConfiguration();
		JSModifyProxyPlugin proxyPlugin = new JSModifyProxyPlugin(outputdir);
		for(int i=0;i<20;i++){
			try{
				CrawljaxConfiguration config = new CrawljaxConfiguration();
				CrawlSpecification crawler = new CrawlSpecification(htmlAdd);
				prox = new ProxyConfiguration();
				WebScarabWrapper web = new WebScarabWrapper();
				DomJsCodeLevelVisitor domJsVis=new DomJsCodeLevelVisitor();
				proxyPlugin.setJSModifyProxyPluginForDOMJSVis(domJsVis, i);
				proxyPlugin.excludeDefaults();
				web.addPlugin(proxyPlugin);
				config.addPlugin(web);
				config.setProxyConfiguration(prox);
				
		//		crawler.setWaitTimeAfterReloadUrl(1000);
				
				config.setCrawlSpecification(crawler);
				CrawljaxController crawljax = new CrawljaxController(config);
				crawljax.run();
			}	
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	

}
