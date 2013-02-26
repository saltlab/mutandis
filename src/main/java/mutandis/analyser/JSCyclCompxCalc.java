package mutandis.analyser;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.WhileLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxController;
import com.crawljax.util.Helper;


public class JSCyclCompxCalc implements NodeVisitor{
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
	private String 	resultFilenameAndPath;
	private String outputdir;
	
	
	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;
	
	private HashMap<String,Integer> cycloMap;
	
	public JSCyclCompxCalc(String outputdir){
		cycloMap=new HashMap<String, Integer>();
		this.outputdir=Helper.addFolderSlashIfNeeded(outputdir);
		resultFilenameAndPath=this.outputdir+ "CycloComplxRateResult.txt";
	}
	
	
	/**
	 * @param scopeName
	 *            the scopeName to set
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * @return the scopeName
	 */
	public String getScopeName() {
		return scopeName;
	}

	/**
	 * Parse some JavaScript to a simple AST.
	 * 
	 * @param code
	 *            The JavaScript source code to parse.
	 * @return The AST node.
	 */
	public AstNode parse(String code) {
		Parser p = new Parser(compilerEnvirons, null);
		return p.parse(code, null, 0);
		
	}

	/**
	 * Find out the function name of a certain node and return "anonymous" if it's an anonymous
	 * function.
	 * 
	 * @param f
	 *            The function node.
	 * @return The function name.
	 */
	protected String getFunctionName(FunctionNode f) {
		if (f==null)
			return "NoFunctionNode";
		else if(f.getParent() instanceof ObjectProperty){
			return ((ObjectProperty)f.getParent()).getLeft().toSource();
		}
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}
	
	@Override
	public boolean visit(AstNode node) {
		if(node instanceof IfStatement || node instanceof ForLoop
				|| node instanceof WhileLoop || node instanceof ConditionalExpression
				|| node instanceof ReturnStatement || node instanceof SwitchCase){
			
			String funcName=getFunctionName(node.getEnclosingFunction());
			int cycloNumber=0;
			if(cycloMap.get(funcName)!=null){
				cycloNumber=cycloMap.get(funcName);
			}
			cycloNumber++;
			cycloMap.put(funcName, cycloNumber);
		}
		return true;
	}
	
	public HashMap<String,Double> getCyclCompxRate(){
		try{
			HashMap<String,Double> cycloMapRate=new HashMap<String,Double>();
			BufferedReader input =
				new BufferedReader(new FileReader(resultFilenameAndPath));
			String line="", funcName="";
			double cycloRate=0;
			while ((line = input.readLine()) != null){	  
			  
				if ("".equals(line))
					break;	  
			  
				if (!(line.equals("================================================"))){
					String[] funcAndCycloRate=line.split("::");
			//		funcName=funcAndCycloRate[0]+"::"+funcAndCycloRate[1];
					funcName=funcAndCycloRate[0];
					cycloRate=Double.parseDouble(funcAndCycloRate[1]);
					cycloMapRate.put(funcName, cycloRate);
				}
			  
			}
		  
			return cycloMapRate;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		  
		
	}
	
	private HashMap<String,Integer> getCyclCompxMap(){
		HashMap<String,Integer> cyclMap=new HashMap<String,Integer>();
		cyclMap=(HashMap<String, Integer>) cycloMap.clone();
		Set<String> keySet=cyclMap.keySet();
		Iterator<String> it=keySet.iterator();
		while(it.hasNext()){
			String funcName=it.next();
			int cyclo=cyclMap.get(funcName)+1;
			cyclMap.put(funcName, cyclo);
		}
		return cyclMap;
	}
	
	private HashMap<String,Double> getCyclCompxRateMap(){
		HashMap<String,Double> cyclCompRate=new HashMap<String,Double>();
		int total=getTotalCycloCompx();
		HashMap<String,Integer> cyclCompMap=getCyclCompxMap();
		Set<String> key=cyclCompMap.keySet();
		Iterator<String> it=key.iterator();
		while(it.hasNext()){
			String funcName=it.next();
			int cyclo=cyclCompMap.get(funcName);
			double cycloRate=(double)cyclo/total;
			cyclCompRate.put(funcName, cycloRate);
		}
		return cyclCompRate;
	}
	
	private int getTotalCycloCompx(){
		HashMap<String,Integer> cyclMap=getCyclCompxMap();
		Set<String> keySet=cyclMap.keySet();
		Iterator<String> it=keySet.iterator();
		int total=0;
		while(it.hasNext()){
			String funcName=it.next();
			int cyclo=cyclMap.get(funcName);
			total+=cyclo;
		}
		return total;
	}
	
	public void finish(){
		writingResultsintoFile();
	}
	private void writingResultsintoFile(){
		
		try{
			PrintWriter file = new PrintWriter(resultFilenameAndPath);
			file.write(getResult());
			file.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getResult(){
		StringBuffer result = new StringBuffer();
		HashMap<String,Double> cycloRateMap=getCyclCompxRateMap();
		Set <String> keySet=cycloRateMap.keySet();
		Iterator<String> it=keySet.iterator();
		while(it.hasNext()){
			
			String funcAndPath=it.next();
			double cycloRate=cycloRateMap.get(funcAndPath);
	//		result.append(getScopeName()+"::"+funcAndPath+"::"+cycloRate+"\n");	
			result.append(funcAndPath+"::"+cycloRate+"\n");	
			result.append("================================================" + "\n");
			
		}
		
		return result.toString();
		
	
	}

}
