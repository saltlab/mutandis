package runner;

import org.openqa.selenium.WebDriver;

import mutandis.analyser.FunctionSelector;
import mutandis.analyser.VariableSelector;
import mutandis.astModifier.JSModifyProxyPlugin;
import mutandis.mutator.DomJsCodeLevelVisitor;
import mutandis.mutator.FunctionNodeVisitor;
import mutandis.mutator.JSSpecificVisitor;
import mutandis.mutator.TestResults;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.plugins.proxy.WebScarabWrapper;


public class SameGameMut {
	
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
		String outputdir="sameGame1-output2";
		String htmlAdd="http://localhost:8080/same-game/same-game.htm";
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
			
		/*		crawler.setWaitTimeAfterEvent(20);
				crawler.click("td").withAttribute("class", "clickable");
				crawler.setClickOnce(true);
				crawler.setDepth(2);
				crawler.setMaximumStates(MAX_STATES);
				crawler.setWaitTimeAfterReloadUrl(5000);
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
		//		funcname=funcname.split("::")[1];
				FunctionNodeVisitor funcNodeVis=new FunctionNodeVisitor(funcname,varname);
				proxyPlugin.setJSModifyProxyPluginForFuncVis(funcNodeVis);
				proxyPlugin.excludeDefaults();
				web.addPlugin(proxyPlugin);
		//		TestResults results=new TestResults(outputdir);
		//		config.addPlugin(results);
				config.addPlugin(web);
				
		//		config.addPlugin(new RandomClickable());
				
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
				
		/*		crawler.click("td").withAttribute("class", "clickable");
				crawler.setClickOnce(true);
				crawler.setDepth(2);
				crawler.setMaximumStates(MAX_STATES);
				crawler.setWaitTimeAfterReloadUrl(10000);
				
		*/		config.setCrawlSpecification(crawler);
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
		
		
		for(int i=0;i<10;i++){
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
				
		
		//		crawler.setWaitTimeAfterReloadUrl(2000);
				
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
				
	//			crawler.setWaitTimeAfterReloadUrl(2000);
				
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
