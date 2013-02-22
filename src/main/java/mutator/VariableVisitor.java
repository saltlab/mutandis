package mutator;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WhileLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import systemProperties.SystemProps;

import com.crawljax.core.CrawljaxController;

public class VariableVisitor implements NodeVisitor{
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
	
	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;
	private static Random rnd=new Random(100);
	private String functionNodeName;
	private String variableName;
	/* forexample for variable x we have: typeOfCode -> <sourceCode> i.e: 
	 * functionCall -> <[test1(x,y)],[test2(x,u)],...> */
	private HashMap<String,ArrayList<AstNode>> variableMap;
	
	private NodeFinder nodeFinder;
	
	public VariableVisitor(String functionName,String variableName){
		
		functionNodeName=functionName;
		this.variableName=variableName;
		nodeFinder=new NodeFinder(variableName);
		variableMap=new HashMap<String,ArrayList<AstNode>>();
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
	
	public HashMap<String,ArrayList<AstNode>> getVariableMap(){
		return variableMap;
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
			List<AstNode> args=((FunctionNode) node).getParams();
			for(int i=0;i<args.size();i++){
				args.get(i).visit(nodeFinder);
				if(nodeFinder.getVariableExists()){
					break;
				}
			}
	//		node.visit(nodeFinder);
			if(nodeFinder.getVariableExists()){
				
				setVariableMap(node,"FunctionNode");					
			}
			nodeFinder.setVariableExists(false);
		}
		else if(node instanceof FunctionCall){
			node.visit(nodeFinder);
			if(nodeFinder.getVariableExists()){
				
				setVariableMap(node,"FunctionCall");					
			}
			nodeFinder.setVariableExists(false);
		}
				
		
		else if(node instanceof ReturnStatement){
			node.visit(nodeFinder);
			if(nodeFinder.getVariableExists()){
				
				setVariableMap(node,"ReturnStatement");					
			}
			nodeFinder.setVariableExists(false);
		}
		

		else if(node instanceof VariableDeclaration){
			if (node.getParent() instanceof ExpressionStatement){
				node.visit(nodeFinder);
				if(nodeFinder.getVariableExists()){			
					setVariableMap(node.getParent(), "VariableDeclaration");			
				}
				nodeFinder.setVariableExists(false);
			}
			else{
				node.visit(nodeFinder);
				if(nodeFinder.getVariableExists()){			
					setVariableMap(node, "VariableDeclaration");			
				}
				nodeFinder.setVariableExists(false);
			}
		}
		
		else if(node instanceof Assignment) {
				if (node.getParent() instanceof ExpressionStatement){
					((Assignment)node).getLeft().visit(nodeFinder);
					if(nodeFinder.getVariableExists()){			
						setVariableMap(node.getParent(), "Assignment");			
					}
					nodeFinder.setVariableExists(false);
				}
				else{
					((Assignment)node).getLeft().visit(nodeFinder);
					if(nodeFinder.getVariableExists()){			
						setVariableMap(node, "Assignment");			
					}
					nodeFinder.setVariableExists(false);
				}
		}
		
		else if(node instanceof UnaryExpression){
			if(((UnaryExpression)node).getOperator()==Token.INC
					|| ((UnaryExpression)node).getOperator()==Token.DEC)
				if (node.getParent() instanceof ExpressionStatement){
					((UnaryExpression)node).visit(nodeFinder);
					if(nodeFinder.getVariableExists()){			
						setVariableMap(node.getParent(), "Assignment");			
				}
					nodeFinder.setVariableExists(false);
			}
			else{
				((UnaryExpression)node).visit(nodeFinder);
				if(nodeFinder.getVariableExists()){			
					setVariableMap(node, "Assignment");			
				}
				nodeFinder.setVariableExists(false);
			}
					
			
		}
		
			
		return true;
	}
	
	private void setVariableMap(AstNode node,String codeType){

		ArrayList<AstNode> list=new ArrayList<AstNode>();
		if(variableMap.get(codeType)!=null){
				
			list=variableMap.get(codeType);
								
		}				
		list.add(node);
		variableMap.put(codeType, list);					
		
	}
	
	public List<Object> getRandomVariableMap(){
		List<String> keys=new ArrayList<String>(variableMap.keySet());
		String randkey=keys.get(rnd.nextInt(keys.size()));
		List<AstNode> values=variableMap.get(randkey);
		AstNode randValue=values.get(rnd.nextInt(values.size()));
		List <Object> selectedForMutation=new ArrayList<Object>();
		selectedForMutation.add(randkey);
		selectedForMutation.add(randValue);
		selectedForMutation.add(nodeFinder.getVariableNode());
		return selectedForMutation;
		
	}
	




	

}
