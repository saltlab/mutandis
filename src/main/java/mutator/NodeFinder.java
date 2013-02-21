package mutator;



import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;

import com.crawljax.core.CrawljaxController;

public class NodeFinder implements NodeVisitor{
	
	
	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;

	private String variableName;
	private AstNode variableNode;
	private boolean variableExists=false;
	public NodeFinder(String variableName){
				
		this.variableName=variableName;
		variableExists=false;
	}
	
	/**
	 * @param scopeName
	 *            the scopeName to set
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}
	
	
	
	private void setVariableName(String varName){
		
		variableName=varName;
	}
	public String getVariableName(){
		
		return variableName;
	}
	/**
	 * @return the scopeName
	 */
	public String getScopeName() {
		return scopeName;
	}
	
	public boolean getVariableExists(){
		return variableExists;
	}
	public void setVariableExists(boolean varExists){
		variableExists=varExists;
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
	
	public AstNode getVariableNode(){
		return variableNode;
	}
	
	
	@Override
	public boolean visit(AstNode node) {
		
		if(node instanceof Name){
			
			if(variableName.equals(((Name)node).getIdentifier())){
				variableNode=node;
				variableExists=true;
			}
		}
		
		
		
		return true;
	}
	

	




	

}
