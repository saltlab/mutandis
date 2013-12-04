package mutandis.mutator;



import mutandis.astModifier.JSASTModifier;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.LabeledStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.crawljax.core.CrawljaxController;

public class FunctionNodeVisitor implements NodeVisitor{
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
	
	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;
	private String functionNodeName;
	private String variableName;
	private VariableVisitor variableVisitor;
	private BranchVisitor branchVisitor;
	/* true for variable mutation and false for branch mutation*/
	private boolean isVariableMut;
	
	/* for mutating variables */
	public FunctionNodeVisitor(String functionName, String variableName){
		
		functionNodeName=functionName;
		this.variableName=variableName;
		variableVisitor=new VariableVisitor(functionName, variableName);
		isVariableMut=true;
	}
	
	/* for mutating branches */
	public FunctionNodeVisitor(String functionName){
		
		functionNodeName=functionName;
		branchVisitor=new BranchVisitor(functionName);
		isVariableMut=false;
	}
	
	public boolean getIsVariableMut(){
		return isVariableMut;
	}
	
	public VariableVisitor getVariableVisitor(){
		return variableVisitor;
	}
	
	
	public BranchVisitor getBranchVisitor(){
		return branchVisitor;
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
		
		if(node instanceof FunctionNode){
			String funcNodeName=getFunctionName((FunctionNode)node);
			if(funcNodeName.equals(functionNodeName)){
				if(isVariableMut)
					node.visit(variableVisitor);
				else
					node.visit(branchVisitor);
				
			}
		}
		else if(node instanceof LabeledStatement){
			if(((LabeledStatement)node).getStatement() instanceof FunctionNode
					&& ((LabeledStatement)node).getLabels().size()==1){
				
				FunctionNode funcNode=(FunctionNode) ((LabeledStatement)node).getStatement();
				String funcNodeName=((LabeledStatement)node).getLabels().get(0).getName();
				if(funcNodeName.equals(functionNodeName)){
					if(isVariableMut)
						funcNode.visit(variableVisitor);
					else
						funcNode.visit(branchVisitor);
					
				}
				
			}
		}
		else if(functionNodeName.equals("NoFunctionNode")){
			AstNode nodeToVisit=node.getAstRoot();
			if(isVariableMut)
				nodeToVisit.visit(variableVisitor);
		}
		
		return true;
	}
	




	

}
