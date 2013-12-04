package mutandis.exectionTracer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mutandis.astModifier.JSASTModifier;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.UnaryExpression;



import com.crawljax.core.CrawljaxController;
import com.crawljax.util.Helper;

public class AstVarInstrumenter extends JSASTModifier {

	public static final String JSINSTRUMENTLOGNAME = "window.jsVarExecTrace";
	private List<String> excludeVariableNamesList= new ArrayList<String>();
	private List<String> jsSpecificPropGetNames= new ArrayList<String>();
	

	/**
	 * Construct without patterns.
	 */
	public AstVarInstrumenter() {
		super(false);
		excludeVariableNamesList = new ArrayList<String>();
		jsSpecificPropGetNames.add("length");
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable/function patterns to exclude.
	 */


	public AstVarInstrumenter(List<String> excludes, List<String> functionsToLogVars) {
		super(functionsToLogVars, false);
	
	}

	
	@Override
	public void finish(AstRoot node) {
		/* add initialization code for the function and logging array */
		node.addChildToFront(jsLoggingFunctions());
	}

	@Override
	public void start() {
		/* nothing to do here */
	}
	
	/* If we observe an assignment node, we pass the whole assignment
	 *  as an AstNode to this method. 
	 *  statement category can be: computation; (for, while, if, switch) condition; function call; return statement */
	@Override
	public AstNode createNode(FunctionNode function, AstNode nodeForVarLog, String statementCategory){
		String functionName=getFunctionName(function);
		int lineNo=nodeForVarLog.getLineno();
		List<String>varName=new ArrayList<String>();
		List<String> varType=new ArrayList<String>();
		List<String> varCategory=new ArrayList<String>();
		String code="/* empty */";
		List<AstNode> varsInNode=new ArrayList<AstNode>();
		varsInNode=getVaribalesInNode(nodeForVarLog);
		for (int i=0;i<varsInNode.size();i++){
			if(jsSpecificPropGetNames.contains(getVarName(varsInNode.get(i)))
					&& getVarType(varsInNode.get(i)).equals("PropertyGet")){
				continue;
			}
			varName.add(getVarName(varsInNode.get(i)));
			varType.add(getVarType(varsInNode.get(i)));
			varCategory.add(getVarCategory(varsInNode.get(i), functionName));
		}
		
		if (varsInNode.size()==0)
			code = "/* empty */";
		else{
			String nodeForVarLogSource;
			
			if (nodeForVarLog instanceof Name)
				nodeForVarLogSource=nodeForVarLog.getParent().toSource();
			else
				nodeForVarLogSource=nodeForVarLog.toSource();
				
			/* make sure it fits on 1 line by removing new line chars */
			nodeForVarLogSource = Helper.removeNewLines(nodeForVarLogSource);
			/* escape quotes */
			nodeForVarLogSource = nodeForVarLogSource.replaceAll("\\\"", "\\\\\"");
			nodeForVarLogSource = nodeForVarLogSource.replaceAll("\\\'", "\\\\\'");
			code=
				"send(new Array('" + getScopeName() + "::" + functionName + "::" + "nodeForVarLogSource" + "', '" +
				lineNo + "', new Array(";
		
		
			String vars = "";
			for (int i = 0; i < varName.size(); i++) {
				/* only instrument variables that should not be excluded */
				if (shouldInstrument(varName.get(i))) {
					vars += "addVariable('" + varName.get(i) + "'" + ", " + "'" + 
					varType.get(i) + "'" + ", " + "'" + varCategory.get(i) + "'" 
					 + ", " + "'" + statementCategory + "'" + "),";
				}
			}
		
			if (vars.length() > 0) {
				/* remove last comma */
				vars = vars.substring(0, vars.length() - 1);
				code += vars + ")));";
			} else {
				/* no variables to instrument here, return an empty node */
				code = "/* empty */";
			}
		}


		return parse(code);
		
	}
	
	
	private AstNode jsLoggingFunctions() {
		String code;

		File js = new File(this.getClass().getResource("/addvariable.js").getFile());
		code = Helper.getContent(js);
		return parse(code);
	}
	

	
	private List<AstNode> getVaribalesInNode(AstNode node){
		
		if (node instanceof ParenthesizedExpression){
			node=((ParenthesizedExpression) node).getExpression();
		}
		if (node instanceof ExpressionStatement){
			node=((ExpressionStatement) node).getExpression();
		}

		if (node instanceof Name){
			return Arrays.asList(node);
		}
		else
			if (node instanceof Assignment){
			
				ArrayList<AstNode> namesInNode=new ArrayList<AstNode>();
				return searchNodeForNames(((Assignment)node).getRight(), namesInNode);
			
			}
		else 
			if (node instanceof InfixExpression){
				ArrayList<AstNode> namesInNode=new ArrayList<AstNode>();
				return searchNodeForNames(node, namesInNode);
			
			
			}
		else
			if (node instanceof UnaryExpression){
				ArrayList<AstNode> unaryExpInNode=new ArrayList<AstNode>();
				return searchNodeForUnaryExp(node, unaryExpInNode);
			}
		return Arrays.asList();
		
	}
	
	/*
	 * The AstNode is of type Name
	 */
//	ArrayList<Name> namesInConditionStatm=new ArrayList<Name>();
	private List<AstNode> searchNodeForNames(AstNode node, ArrayList<AstNode> namesInNode){
		
		if (node instanceof ParenthesizedExpression){
			node= ((ParenthesizedExpression) node).getExpression();
			searchNodeForNames(node, namesInNode);
		}
		if (node instanceof UnaryExpression){
			node= ((UnaryExpression) node).getOperand();
			searchNodeForNames(node, namesInNode);
		}
		
		if (node instanceof InfixExpression){
		
			if (((InfixExpression)node).getLeft() instanceof InfixExpression){
				searchNodeForNames(((InfixExpression)node).getLeft(), namesInNode);
			}
			
			else
				if (((InfixExpression)node).getLeft() instanceof ParenthesizedExpression)
					searchNodeForNames(((ParenthesizedExpression)((InfixExpression)node).getLeft()).getExpression(), namesInNode);
			

			else
				if (((InfixExpression)node).getLeft() instanceof UnaryExpression)
					searchNodeForNames(((UnaryExpression)((InfixExpression)node).getLeft()).getOperand(), namesInNode);		
			
			if (((InfixExpression)node).getRight() instanceof InfixExpression){
				searchNodeForNames(((InfixExpression)node).getRight(), namesInNode);
			}
			
			else
				if (((InfixExpression)node).getRight() instanceof ParenthesizedExpression)
					searchNodeForNames(((ParenthesizedExpression)((InfixExpression)node).getRight()).getExpression(), namesInNode);
		

			else
				if (((InfixExpression)node).getRight() instanceof UnaryExpression)
					searchNodeForNames(((UnaryExpression)((InfixExpression)node).getRight()).getOperand(), namesInNode);	
			if (((InfixExpression)node).getLeft() instanceof Name){
				Name name=(Name) ((InfixExpression)node).getLeft();
				namesInNode.add(name);
			}
		
			if (((InfixExpression)node).getRight() instanceof Name){
				Name name=(Name) ((InfixExpression)node).getRight();
				namesInNode.add(name);
			}
			
			
		}
		
		else
			if (node instanceof PropertyGet){
				if (((PropertyGet)node).getRight() instanceof PropertyGet)
						searchNodeForNames(((PropertyGet)((PropertyGet)node).getRight()), namesInNode);
				if (((PropertyGet)node).getLeft() instanceof PropertyGet)
					searchNodeForNames(((PropertyGet)((PropertyGet)node).getLeft()), namesInNode);
			}
		
		else
			if (node instanceof Name)
				namesInNode.add((Name)node);;
		
		return namesInNode;
		
	}
	
	/*
	 * the AstNode can be of type Name or ElementGet
	 */
	//ArrayList<AstNode> unaryExpInNode=new ArrayList<AstNode>();
	private List<AstNode> searchNodeForUnaryExp(AstNode node, ArrayList<AstNode> unaryExpInNode){
		
		if (node instanceof ParenthesizedExpression){
			node= ((ParenthesizedExpression) node).getExpression();
			searchNodeForUnaryExp(node, unaryExpInNode);
		}

		
		if (node instanceof InfixExpression){
		
			if (((InfixExpression)node).getLeft() instanceof InfixExpression){
				searchNodeForUnaryExp(((InfixExpression)node).getLeft(), unaryExpInNode);
			}
			
			else
				if (((InfixExpression)node).getLeft() instanceof ParenthesizedExpression){
					searchNodeForUnaryExp(((ParenthesizedExpression)((InfixExpression)node).getLeft()).getExpression(), unaryExpInNode);
				}
			if (((InfixExpression)node).getRight() instanceof InfixExpression){
				searchNodeForUnaryExp(((InfixExpression)node).getRight(), unaryExpInNode);
			}
			
			else
				if (((InfixExpression)node).getRight() instanceof ParenthesizedExpression){
					searchNodeForUnaryExp(((ParenthesizedExpression)((InfixExpression)node).getRight()).getExpression(), unaryExpInNode);
				}
			if (((InfixExpression)node).getLeft() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((InfixExpression)node).getLeft();
				unaryExpInNode.add(unaryExp.getOperand());
			}
	/*		else if (((InfixExpression)node).getLeft() instanceof ElementGet 
					&& ((ElementGet)((InfixExpression)node).getLeft()).getElement() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((ElementGet)((InfixExpression)node).getLeft()).getElement();
				unaryExpInNode.add(unaryExp.getOperand());
				
			}
		
	*/		if (((InfixExpression)node).getRight() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((InfixExpression)node).getRight();
				unaryExpInNode.add(unaryExp.getOperand());
			}
			
	/*		else if (((InfixExpression)node).getRight() instanceof ElementGet 
					&& ((ElementGet)((InfixExpression)node).getRight()).getElement() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((ElementGet)((InfixExpression)node).getRight()).getElement();
				unaryExpInNode.add(unaryExp.getOperand());
				
			}
	*/	}
		
		else
			if (node instanceof UnaryExpression){
				unaryExpInNode.add(((UnaryExpression) node).getOperand());
				searchNodeForUnaryExp(((UnaryExpression) node).getOperand(), unaryExpInNode);
			}
		return unaryExpInNode;
		
	}
	/*
	 * returns the type of variable: Name or ElementGet or PropertyGet
	 */
	private String getVarType(AstNode varNode){
		if (varNode instanceof Name){
			if (varNode.getParent() instanceof ElementGet){
				return "ElementGet";
			}
			else if (varNode.getParent() instanceof PropertyGet)
				return "PropertyGet";
						
		}
		return "Name";
		
	}
	/*
	 * returns the category of the variable: variable or function argument
	 */
	private String getVarCategory(AstNode node, String functionName){
		AstNode parent=node.getParent();
		while(parent!=null){
			if (parent instanceof FunctionCall){
				return "argument";
			}
			parent=parent.getParent();
		}
		return "variable";
		
		
	
	
	}
	
	/*
	 * returns the name of the variable
	 */
	
	private String getVarName(AstNode varNode){
		if (varNode instanceof Name){
			return ((Name)varNode).getIdentifier();
		}
		else if (varNode instanceof ElementGet){
			return (((ElementGet)varNode).getTarget().toSource());
		}
		else if (varNode instanceof PropertyGet)
			return varNode.toSource();
	
		
		return "";
	}
	

	
	
	/**
	 * Check if we should instrument this variable by matching it against the exclude variable
	 * regexps.
	 * 
	 * @param name
	 *            Name of the variable.
	 * @return True if we should add instrumentation code.
	 */
	private boolean shouldInstrument(String name) {
		if (name == null) {
			return false;
		}

		/* is this an excluded variable? */
		for (String regex : excludeVariableNamesList) {
			if (name.matches(regex)) {
				LOGGER.debug("Not instrumenting variable " + name);
				return false;
			}
		}

		return true;
	}

	/**
	 * for now do nothing here
	 */
	@Override
	public AstNode createFunctionTrackingNode(FunctionNode calleeFunction) {
	
		return null;
	}

	@Override
	protected AstNode createFunctionTypeNameTrackingNode(
			FunctionNode callerFunc, AstNode node) {
		
		return null;
	}
	

	
	
}
