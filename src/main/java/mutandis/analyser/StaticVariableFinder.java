package mutandis.analyser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.WhileLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxController;
import com.crawljax.util.Helper;

public class StaticVariableFinder implements NodeVisitor  {




		protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());


		
		private List<String> jsSpecificPropGetNames= new ArrayList<String>();
		private List<String> excludeVariableNamesList= new ArrayList<String>();
		private String staticGeneratedVarAnalyserString=""; 


		/**
		 * Contains the scopename of the AST we are visiting. Generally this will be the filename
		 */
		private String scopeName = null;
		
		/**
		 * whether to use the visit method of this class for tracking function calls
		 * or for logging variable/function-parameters
		 * shouldTrackFunctionCalls==true means we are tracking function calls
		 * shouldTrackFunctionCalls==false means that we are logging variable/function-parameters
		 */

		

		
		/**
		 * constructor without specifying functions that should be visited
		 */
		protected StaticVariableFinder(){
			
			jsSpecificPropGetNames.add("length");
			
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

		public String getStaticGeneratedVarAnalyserString(){
			return staticGeneratedVarAnalyserString;
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



	
		
		
		
		
		/**
		 * Actual visiting method.
		 * 
		 * @param node
		 *            The node that is currently visited.
		 * @return Whether to visit the children.
		 */
		@Override
		public boolean visit(AstNode node) {
		
				
			if (node instanceof SwitchCase) {
	            //Add block around all statements in the switch case
				SwitchCase sc = (SwitchCase)node;
	            List<AstNode> statements = sc.getStatements();
	            List<AstNode> blockStatement = new ArrayList<AstNode>();
	            Block b = new Block();
	           
	            if (statements != null) {
	                Iterator<AstNode> it = statements.iterator();
	                while (it.hasNext()) {
	                    AstNode stmnt = it.next();
	                    b.addChild(stmnt);
	                }
	               
	                blockStatement.add(b);
	                sc.setStatements(blockStatement);
	            }

			}
			/* we will not log the incremental part of the for loops */
			if (node.getParent() instanceof ForLoop){
				ForLoop forloop=(ForLoop)node.getParent();
				if (forloop.getIncrement().equals(node))
					return false;
			}
				
			else if (node instanceof FunctionCall){
			 
				FunctionNode func=node.getEnclosingFunction();
	            List<AstNode> nodeForVarLog=((FunctionCall) node).getArguments();
	            String statementCategory="FunctionCallArgument";
	            
	            for (int i=0;i<nodeForVarLog.size();i++){
	            	if (!(nodeForVarLog.get(i) instanceof KeywordLiteral)){
	            		staticGeneratedVarAnalyserString+=analyseVariable(func, nodeForVarLog.get(i), statementCategory);
	            	
	            		
			
	            	}
	            }

			 
			}
			else if (node instanceof Assignment){
			 
	            FunctionNode func=node.getEnclosingFunction();
	            String statementCategory="AssignmentComputation";
	            AstNode nodeForVarLog=node;
	            staticGeneratedVarAnalyserString+=analyseVariable(func, nodeForVarLog, statementCategory);
	         
	         
				
			 
			}
			
			else if (node instanceof UnaryExpression){
				 
	            FunctionNode func=node.getEnclosingFunction();
	            String statementCategory="UnaryExpression";
	            AstNode nodeForVarLog=node;
	            staticGeneratedVarAnalyserString+=analyseVariable(func, nodeForVarLog, statementCategory);

	   	
			 
			}
			

			if (node.getParent() instanceof ElementGet){
		            FunctionNode func=node.getEnclosingFunction();
		            String statementCategory="ElementGet";
		            AstNode nodeForVarLog=node;
		            staticGeneratedVarAnalyserString+=analyseVariable(func, nodeForVarLog, statementCategory);
		        
			}

		 
			else if (node instanceof ReturnStatement){
			 
	            FunctionNode func=node.getEnclosingFunction();
	            String statementCategory="ReturnStatementArgument";
	            AstNode nodeForVarLog=((ReturnStatement)node).getReturnValue();
	            if (!(nodeForVarLog instanceof KeywordLiteral) && nodeForVarLog!=null){
	            	staticGeneratedVarAnalyserString+=analyseVariable(func, nodeForVarLog, statementCategory);
	            
	            	
	            }
			 
			}
		 
			else if (node instanceof IfStatement){
			 
	            FunctionNode func=node.getEnclosingFunction();
	            String statementCategory="IfStatementCondition";
	            AstNode nodeForVarLog=((IfStatement) node).getCondition();
	            if (!(nodeForVarLog instanceof KeywordLiteral)){
	            	staticGeneratedVarAnalyserString+=analyseVariable(func, nodeForVarLog, statementCategory);
	            

	            }
			 
			}
		 
			else if (node instanceof ForLoop){
			 
	            FunctionNode func=node.getEnclosingFunction();
	            String statementCategory="ForLoopCondition";
	            AstNode nodeForVarLog=((ForLoop) node).getCondition();
	            if (!(nodeForVarLog instanceof KeywordLiteral)){
	            	staticGeneratedVarAnalyserString+=analyseVariable(func, nodeForVarLog, statementCategory);
	            
	          

	
	            }
			}
		 
		 
			else if (node instanceof WhileLoop){
			 
	            FunctionNode func=node.getEnclosingFunction();
	            String statementCategory="WhileLoopCondition";
	            AstNode nodeForVarLog=((WhileLoop) node).getCondition();
	            if (!(nodeForVarLog instanceof KeywordLiteral)){
	            	staticGeneratedVarAnalyserString+=analyseVariable(func, nodeForVarLog, statementCategory);
	 
	            }
			 
			}
			else if (node instanceof SwitchStatement){
	            FunctionNode func=node.getEnclosingFunction();
	            AstNode nodeForVarLog=((SwitchStatement) node).getExpression();
	            String statementCategory="SwitchStatementCondition";
	            if (!(nodeForVarLog instanceof KeywordLiteral)){
	            	staticGeneratedVarAnalyserString+=analyseVariable(func, nodeForVarLog, statementCategory);
	
	            }
			}
			
			

			return true;
			
		}
		
		
		public String analyseVariable(FunctionNode function, AstNode nodeForVarLog, String statementCategory){
			String functionName=getFunctionName(function);
			int lineNo=nodeForVarLog.getLineno();
			List<String>varName=new ArrayList<String>();
			List<String> varType=new ArrayList<String>();
			List<String> varCategory=new ArrayList<String>();
			String code="";
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
				code = "";
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
					getScopeName() + "::" + functionName + "::" + "nodeForVarLogSource" + "::" +
					lineNo +"\n";
			
			
				String vars = "";
				for (int i = 0; i < varName.size(); i++) {
					/* only instrument variables that should not be excluded */
					if (shouldInstrument(varName.get(i))) {
						vars += varName.get(i) + "::" + 
						varType.get(i) + "::" + varCategory.get(i) + "::" 
						 + statementCategory +"\n";
					}
				}
				
			
			
				
				if(vars.length()>0){
					code+=vars;
					code+="================================================"+"\n";
				}
				else {
					/* no variables to instrument here, return an empty node */
					code = "";
				}
				
			}


			return code;
			
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

		
		

}
