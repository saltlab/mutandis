package mutandis.astModifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mutandis.analyser.JSCyclCompxCalc;
import mutandis.exectionTracer.JSFuncExecutionTracer;
import mutandis.exectionTracer.JSVarExecutionTracer;
import mutandis.mutator.BranchVisitor;
import mutandis.mutator.DomJsCodeLevelVisitor;
import mutandis.mutator.FunctionNodeVisitor;
import mutandis.mutator.JSSpecificVisitor;
import mutandis.mutator.NodeMutator;
import mutandis.mutator.VariableVisitor;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.BrowserCache;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import com.crawljax.util.Helper;



public class JSModifyProxyPlugin extends ProxyPlugin {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JSModifyProxyPlugin.class.getName());

	private List<String> excludeFilenamePatterns;

	private JSASTModifier modifier;
	private JSCyclCompxCalc cyclCompxCalc;
	private FunctionNodeVisitor funcNodeVisitor;
	private DomJsCodeLevelVisitor domJsVisitor;
	private JSSpecificVisitor jsSpecVisitor;
	private String outputfolder;
	private int indexOfJsDomToVisit=0;
	private int indexOfJsSpecToVisit=0;
	private boolean singleMutationDone=false;
	
	private boolean shouldStartNonJsMutatations=false;
	private boolean shouldStartJsSpecMutations=false;
	private boolean shouldStartDomJsMutations=false;
	private boolean shouldGetInfoFromCode=false;

	/**
	 * Construct without patterns.
	 * 
	 * @param modify
	 *            The JSASTModifier to run over all JavaScript.
	 */
	public JSModifyProxyPlugin(JSASTModifier modify, JSCyclCompxCalc cyclCompxCalc) {
		
		excludeFilenamePatterns = new ArrayList<String>();
		modifier = modify;
		this.cyclCompxCalc=cyclCompxCalc;
		shouldGetInfoFromCode=true;
		shouldStartNonJsMutatations=false;
		shouldStartJsSpecMutations=false;
		shouldStartDomJsMutations=false;
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param modify
	 *            The JSASTModifier to run over all JavaScript.
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public JSModifyProxyPlugin(JSASTModifier modify, List<String> excludes,JSCyclCompxCalc cyclCompxCalc) {
		excludeFilenamePatterns = excludes;
		this.cyclCompxCalc=cyclCompxCalc;
		modifier = modify;
		shouldGetInfoFromCode=true;
		shouldStartNonJsMutatations=false;
		shouldStartJsSpecMutations=false;
		shouldStartDomJsMutations=false;
	}
	
	public JSModifyProxyPlugin(String outputfolder){
		excludeFilenamePatterns = new ArrayList<String>();
		this.outputfolder=outputfolder;
	}
	public void setJSModifyProxyPluginForFuncVis(FunctionNodeVisitor fnv) {
	
		funcNodeVisitor=fnv;
		shouldStartNonJsMutatations=true;
		shouldStartJsSpecMutations=false;
		shouldStartDomJsMutations=false;
		shouldGetInfoFromCode=false;
		singleMutationDone=false;
			
	}

	
	public void setJSModifyProxyPluginForDOMJSVis(DomJsCodeLevelVisitor domVis, int indexOfDom){
		
		shouldStartNonJsMutatations=false;
		shouldGetInfoFromCode=false;
		shouldStartJsSpecMutations=false;
		shouldStartDomJsMutations=true;
		domJsVisitor=domVis;
		indexOfJsDomToVisit=indexOfDom;
		singleMutationDone=false;

			
	}
	public void setJSModifyProxyPluginForJsSpecVis(JSSpecificVisitor jsVis, int indexOfjsSpec){
		
		shouldStartNonJsMutatations=false;
		shouldGetInfoFromCode=false;
		shouldStartJsSpecMutations=true;
		shouldStartDomJsMutations=false;
		jsSpecVisitor=jsVis;
		indexOfJsSpecToVisit=indexOfjsSpec;
		singleMutationDone=false;

			
	}
	/**
	 * Adds some defaults to the list of files that should be excluded from modification. These
	 * include:
	 * <ul>
	 * <li>jQuery</li>
	 * <li>Prototype</li>
	 * <li>Scriptaculous</li>
	 * <li>MooTools</li>
	 * <li>Dojo</li>
	 * <li>YUI</li>
	 * <li>All kinds of Google scripts (Adwords, Analytics, etc)</li>
	 * <li>Minified JavaScript files with min, compressed or pack in the URL.</li>
	 * </ul>
	 */
	public void excludeDefaults() {
		excludeFilenamePatterns.add(".*jquery[-0-9.]*.js?.*");
		excludeFilenamePatterns.add(".*jquery.*.js?.*");
	//	excludeFilenamePatterns.add(".*same-game.*.htm?.*");
		excludeFilenamePatterns.add(".*prototype.*js?.*");
		excludeFilenamePatterns.add(".*scriptaculous.*.js?.*");
		excludeFilenamePatterns.add(".*mootools.js?.*");
		excludeFilenamePatterns.add(".*dojo.xd.js?.*");
		excludeFilenamePatterns.add(".*yuiloader.js?.*");
		excludeFilenamePatterns.add(".*google.*");
		excludeFilenamePatterns.add(".*min.*.js?.*");
		excludeFilenamePatterns.add(".*pack.*.js?.*");
		excludeFilenamePatterns.add(".*compressed.*.js?.*");
		excludeFilenamePatterns.add(".*rpc.*.js?.*");
		excludeFilenamePatterns.add(".*o9dKSTNLPEg.*.js?.*");
		excludeFilenamePatterns.add(".*gdn6pnx.*.js?.*");
		excludeFilenamePatterns.add(".*show_ads.*.js?.*");
	//	excludeFilenamePatterns.add(".*ga.*.js?.*");
		//The following 10 excluded files are just for Tudu
		excludeFilenamePatterns.add(".*builder.js");
		excludeFilenamePatterns.add(".*controls.js");
		excludeFilenamePatterns.add(".*dragdrop.js");
		excludeFilenamePatterns.add(".*effects.js");
		excludeFilenamePatterns.add(".*prototype.js");
		excludeFilenamePatterns.add(".*scriptaculous.js");
		excludeFilenamePatterns.add(".*slider.js");
		excludeFilenamePatterns.add(".*unittest.js");
	//	excludeFilenamePatterns.add(".*engine.js");
		excludeFilenamePatterns.add(".*util.js");
		excludeFilenamePatterns.add(".*cycle.js");
		///////
		excludeFilenamePatterns.add(".*qunit.js");
		excludeFilenamePatterns.add(".*filesystem.js");
		excludeFilenamePatterns.add(".*functional.js");
		excludeFilenamePatterns.add(".*test.core.js");
		excludeFilenamePatterns.add(".*inject.js");
		 
	}
	
	public boolean getSingleMutationDone(){
		return singleMutationDone;
	}

	@Override
	public String getPluginName() {
		return "JSInstrumentPlugin";
	}

	@Override
	public HTTPClient getProxyPlugin(HTTPClient in) {
		return new Plugin(in);
	}

	private boolean shouldModify(String name) {
		/* try all patterns and if 1 matches, return false */
		for (String pattern : excludeFilenamePatterns) {
			if (name.matches(pattern)) {
				LOGGER.info("Not modifying response for " + name);
				return false;
			}
		}

		LOGGER.info("Modifying response for " + name);

		return true;
	}

	/**
	 * This method tries to add instrumentation code to the input it receives. The original input is
	 * returned if we can't parse the input correctly (which might have to do with the fact that the
	 * input is no JavaScript because the server uses a wrong Content-Type header for JSON data)
	 * 
	 * @param input
	 *            The JavaScript to be modified
	 * @param scopename
	 *            Name of the current scope (filename mostly)
	 * @return The modified JavaScript
	 */
	private synchronized String modifyJS(String input, String scopename) {
		
		/*this line should be removed when it is used for collecting exec
		 * traces, mutating, and testing jquery library*/
		input = input.replaceAll("[\r\n]","\n\n");
		if (!shouldModify(scopename)) {
			return input;
		}
		
		/* this line is just for collecting exec traces from jquery while it is being used by
		 * some other application--remove it when you want to collect traces from the application itself */
	/*	if(!scopename.equals("http://localhost:8080/jquery/dist/jquery.js"))
			return input;
	*/	
		else
			if(singleMutationDone)
				return input;
		
		try {
		
			AstRoot ast = null;	
			
			/* initialize JavaScript context */
			Context cx = Context.enter();

			/* create a new parser */
			Parser rhinoParser = new Parser(new CompilerEnvirons(), cx.getErrorReporter());
			
			/* parse some script and save it in AST */
			ast = rhinoParser.parse(new String(input), scopename, 0);

			if(shouldGetInfoFromCode){
			
				cyclCompxCalc.setScopeName(scopename);
				ast.visit(cyclCompxCalc);
				cyclCompxCalc.finish();
			
				modifier.setScopeName(scopename);
				modifier.start();

			/* recurse through AST */
				modifier.shouldTrackFunctionNodes=true;
				ast.visit(modifier);
			
				if(modifier.shouldTrackFunctionCalls){
					modifier.shouldTrackFunctionNodes=false;
					ast.visit(modifier);
				}
				

				modifier.finish(ast);
			}
			else if(shouldStartNonJsMutatations){
				
				funcNodeVisitor.setScopeName(scopename);
				ast.visit(funcNodeVisitor);
				/* mutating variables */
				if(funcNodeVisitor.getIsVariableMut()){
					VariableVisitor varvis=funcNodeVisitor.getVariableVisitor();
					if(varvis.getVariableMap().size()!=0){
						List<Object> desiredList=varvis.getRandomVariableMap();
	
						NodeMutator nm=new NodeMutator(outputfolder,scopename);
						singleMutationDone=nm.mutateVariable(desiredList);
		
					}
					else{
						NodeMutator nm=new NodeMutator(outputfolder,scopename);
						StringBuffer stb=new StringBuffer();
						stb.append("no changes made to the code"+"\n");
						stb.append("================"+"\n");
						nm.writeResultsToFile(stb.toString());
					}
					
				}
				/* mutating branches */
				else if(!funcNodeVisitor.getIsVariableMut()){
					BranchVisitor brvis=funcNodeVisitor.getBranchVisitor();
					if(brvis.getBranchMap().size()!=0){
						List<Object> desiredList=brvis.getRandomBranchMap();
						NodeMutator nm=new NodeMutator(outputfolder,scopename);
						singleMutationDone=nm.mutateBranchStatements(desiredList);
					}
					else{
						NodeMutator nm=new NodeMutator(outputfolder,scopename);
						StringBuffer stb=new StringBuffer();
						stb.append("no changes made to the code"+"\n");
						stb.append("================"+"\n");
						nm.writeResultsToFile(stb.toString());
					}
				}
			}
			else if(shouldStartJsSpecMutations){
				jsSpecVisitor.setScopeName(scopename);
				ast.visit(jsSpecVisitor);
				jsSpecVisitor.setJsSpecList();
				List<Object> desiredList=jsSpecVisitor.getElementfromJsSpecList(indexOfJsSpecToVisit);
				if(desiredList!=null){
					NodeMutator nm=new NodeMutator(outputfolder,scopename);
					singleMutationDone=nm.mutateJsSpecfic(desiredList);
				}
				else{
					NodeMutator nm=new NodeMutator(outputfolder,scopename);
					StringBuffer stb=new StringBuffer();
					stb.append("no changes made to the code"+"\n");
					stb.append("================"+"\n");
					nm.writeResultsToFile(stb.toString());
				}
				
			}
			else if(shouldStartDomJsMutations){
			
				domJsVisitor.setScopeName(scopename);
				ast.visit(domJsVisitor);
				domJsVisitor.setJsDomList();
				List<Object> desiredList=domJsVisitor.getElementfromJsDomList(indexOfJsDomToVisit);
				if(desiredList!=null){
					NodeMutator nm=new NodeMutator(outputfolder,scopename);
					singleMutationDone=nm.mutateDomJsCodeLevel(desiredList);
				}
				else{
					NodeMutator nm=new NodeMutator(outputfolder,scopename);
					StringBuffer stb=new StringBuffer();
					stb.append("no changes made to the code"+"\n");
					stb.append("================"+"\n");
					nm.writeResultsToFile(stb.toString());
				}
			}
			
			
			/* clean up */
			Context.exit();
			return ast.toSource();
		} catch (RhinoException re) {
			System.err.println(re.getMessage());
			LOGGER.warn("Unable to instrument. This might be a JSON response sent"
			        + " with the wrong Content-Type or a syntax error.");
		} catch (IllegalArgumentException iae) {
			
			LOGGER.warn("Invalid operator exception catched. Not instrumenting code.");
		}
		LOGGER.warn("Here is the corresponding buffer: \n" + input + "\n");

		return input;
	}

	/**
	 * This method modifies the response to a request.
	 * 
	 * @param response
	 *            The response.
	 * @param request
	 *            The request.
	 * @return The modified response.
	 */
	private Response createResponse(Response response, Request request) {
		String type = response.getHeader("Content-Type");

		if (request.getURL().toString().contains("?thisisavarexectracingcall")) {
			LOGGER.info("Execution trace request " + request.getURL().toString());
			JSVarExecutionTracer.addPoint(new String(request.getContent()));
			return response;
		}
		if (request.getURL().toString().contains("?thisisafuncexectracingcall")){
			
			LOGGER.info("Execution trace request " + request.getURL().toString());
			JSFuncExecutionTracer.addPoint(new String(request.getContent()));
			return response;
		}

		if (type != null && type.contains("javascript")) {

			/* instrument the code if possible */
			response.setContent(modifyJS(new String(response.getContent()),
			        request.getURL().toString()).getBytes());
		} else if (type != null && type.contains("html")) {
			try {
				Document dom = Helper.getDocument(new String(response.getContent()));
				/* find script nodes in the html */
				NodeList nodes = dom.getElementsByTagName("script");

				for (int i = 0; i < nodes.getLength(); i++) {
					Node nType = nodes.item(i).getAttributes().getNamedItem("type");
					/* instrument if this is a JavaScript node */
					if ((nType != null && nType.getTextContent() != null && nType
					        .getTextContent().toLowerCase().contains("javascript"))) {
						String content = nodes.item(i).getTextContent();
						if (content.length() > 0) {
							String js = modifyJS(content, request.getURL() + "script" + i);
							nodes.item(i).setTextContent(js);
							continue;
						}
					}

					/* also check for the less used language="javascript" type tag */
					nType = nodes.item(i).getAttributes().getNamedItem("language");
					if ((nType != null && nType.getTextContent() != null && nType
					        .getTextContent().toLowerCase().contains("javascript"))) {
						String content = nodes.item(i).getTextContent();
						if (content.length() > 0) {
							String js = modifyJS(content, request.getURL() + "script" + i);
							nodes.item(i).setTextContent(js);
						}

					}
				}
				/* only modify content when we did modify anything */
				if (nodes.getLength() > 0) {
					/* set the new content */
					response.setContent(Helper.getDocumentToByteArray(dom));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/* return the response to the webbrowser */
		return response;
	}


	private class Plugin implements HTTPClient {

		private HTTPClient client = null;

		/**
		 * Constructor for this plugin.
		 * 
		 * @param in
		 *            The HTTPClient connection.
		 */
		public Plugin(HTTPClient in) {
			client = in;
		}

		@Override
		public Response fetchResponse(Request request) throws IOException {
			Response response = client.fetchResponse(request);
			return createResponse(response, request);
		}
	}
	



}
