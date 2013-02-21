package mutator;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.WhileLoop;

import systemProperties.SystemProps;

import com.crawljax.core.CrawljaxController;

public class BranchVisitor implements NodeVisitor{
	
	protected static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());
	
	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;
	private String functionNodeName;
	private static Random rnd=new Random(100);

	/* forexample for variable x we have: typeOfCode -> <sourceCode> i.e: 
	 * IfStatement -> <[if(...)...],[if(...)...],...> */
	private HashMap<String,ArrayList<AstNode>> branchMap;
	
	public BranchVisitor(String functionName){
		
		functionNodeName=functionName;
		branchMap=new HashMap<String,ArrayList<AstNode>>();
	}
	
	/**
	 * @param scopeName
	 *            the scopeName to set
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}
	
	public String getfunctionNodeName(){
		
		return functionNodeName;
	}
	

	public HashMap<String,ArrayList<AstNode>> getBranchMap(){
		return branchMap;
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
	/*	else if(f.getParent() instanceof LabeledStatement){
			return ((LabeledStatement)f.getParent()).shortName();
		}
	*/	else if(f.getParent() instanceof ObjectProperty){
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
		
		if(node instanceof IfStatement){
			setBranchMap(node,"IfStatement");					

		}
		else if(node instanceof ForLoop){
			setBranchMap(node,"ForLoop");					

		}
		else if(node instanceof WhileLoop){			
			setBranchMap(node,"WhileLoop");					

		}	
		
		else if(node instanceof ReturnStatement){
			setBranchMap(node,"ReturnStatement");					
			
		}
		else if(node instanceof SwitchStatement){			
			setBranchMap(node,"SwitchStatement");						
			
		}
		else if(node instanceof BreakStatement){
			setBranchMap(node,"BreakStatement");					
	
		}
		
		else if(node instanceof ContinueStatement){		
			setBranchMap(node,"ContinueStatement");					
		
		}
			
		return true;
	}
	
	private void setBranchMap(AstNode node,String codeType){
			
		ArrayList<AstNode> list=new ArrayList<AstNode>();
		if(branchMap.get(codeType)!=null){
				
			list=branchMap.get(codeType);
								
		}				
		list.add(node);
		branchMap.put(codeType, list);					
		
	}
	
	public List<Object> getRandomBranchMap(){
		List<String> keys=new ArrayList<String>(branchMap.keySet());
		String randkey=keys.get(rnd.nextInt(keys.size()));
		List<AstNode> values=branchMap.get(randkey);
		AstNode randValue=values.get(rnd.nextInt(values.size()));
		List<Object> selectedForMutation=new ArrayList<Object>();
		selectedForMutation.add(randkey);
		selectedForMutation.add(randValue);
		return selectedForMutation;
		
	}
		

}

