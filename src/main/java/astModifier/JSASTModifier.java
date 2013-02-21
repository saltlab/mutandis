package astModifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.LabeledStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.WhileLoop;

import com.crawljax.core.CrawljaxController;

public abstract class JSASTModifier implements NodeVisitor  {




		protected static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());
		/**
		 * list of functions that should be visited based on the function rank decision process.
		 * an empty list means that all functions should be visited.
		 */
		private static List<String> functionsToLogVars=new ArrayList<String>();
		private static List<String> functionCallsNotToLog=new ArrayList<String>();
		private static List<String> functionNodes=new ArrayList<String>();
		/**
		 * This is used by the JavaScript node creation functions that follow.
		 */
		private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

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
		
		public boolean shouldTrackFunctionCalls;
		public boolean shouldTrackFunctionNodes=true;
		

		
		/**
		 * constructor without specifying functions that should be visited
		 */
		protected JSASTModifier(boolean shouldTrackFunctionCalls){
			this.shouldTrackFunctionCalls=shouldTrackFunctionCalls;
			
			
			functionCallsNotToLog.add("parseInt");
			functionCallsNotToLog.add("jQuery");
			functionCallsNotToLog.add("setTimeout");
			functionCallsNotToLog.add("$");
			functionCallsNotToLog.add(".css");
			functionCallsNotToLog.add(".addClass");
			functionCallsNotToLog.add(".click");
			functionCallsNotToLog.add(".unbind");
			functionCallsNotToLog.add("Math.");
			functionCallsNotToLog.add(".append");
			functionCallsNotToLog.add(".attr");
			functionCallsNotToLog.add(".random");
			functionCallsNotToLog.add("push");
			functionCallsNotToLog.add(".split");
			functionCallsNotToLog.add("v");
			functionCallsNotToLog.add("send(new Array(");
			functionCallsNotToLog.add("new Array(");
			functionCallsNotToLog.add("btoa");
			functionCallsNotToLog.add("atob");
			functionCallsNotToLog.add("atob");
		}
		
		/**
		 * constructor with specifying functions that should be visited
		 */
		
		protected JSASTModifier(List<String> funcsToLogVars, boolean shouldTrackFunctionCalls){
			
			this.shouldTrackFunctionCalls=shouldTrackFunctionCalls;
			functionsToLogVars=new ArrayList<String>();
			functionsToLogVars=funcsToLogVars;
			
			
			
			
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



		private Block createBlockWithNode(AstNode node) {
			Block b = new Block();

			b.addChild(node);

			return b;
		}
	
		private AstNode makeSureBlockExistsAround(AstNode node) {
			
			AstNode parent = node.getParent();

			if (parent instanceof IfStatement) {
				/* the parent is an if and there are no braces, so we should make a new block */
				IfStatement i = (IfStatement) parent;

				/* replace the if or the then, depending on what the current node is */
				if (i.getThenPart().equals(node)) {
					i.setThenPart(createBlockWithNode(node));
				} else if (i.getElsePart()!=null){
					if (i.getElsePart().equals(node))
						i.setElsePart(createBlockWithNode(node));
				}
				
			} else if (parent instanceof WhileLoop) {
				/* the parent is a while and there are no braces, so we should make a new block */
				/* I don't think you can find this in the real world, but just to be sure */
				WhileLoop w = (WhileLoop) parent;
				if (w.getBody().equals(node))
					w.setBody(createBlockWithNode(node));
			} else if (parent instanceof ForLoop) {
				/* the parent is a for and there are no braces, so we should make a new block */
				/* I don't think you can find this in the real world, but just to be sure */
				ForLoop f = (ForLoop) parent;
				if (f.getBody().equals(node))
					f.setBody(createBlockWithNode(node));
			}

			return node.getParent();
		}
		
		
		
		private boolean shouldVisitFunction(FunctionNode function){
			if (functionsToLogVars.size()==0)
				return true;
			for (String funcName:functionsToLogVars){
				
				if (funcName==function.getName().toString()){
					return true;
				}
			}
			return false;
		}
		
		private boolean shouldVisitFunctionCall(FunctionCall function){
			if (functionCallsNotToLog.size()==0)
				return true;
			for (String funcName:functionCallsNotToLog){
				
				if (function.getTarget().toSource().contains(funcName)){
					return false;
				}
			}
			return true;
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
			
		
		
			if (!shouldTrackFunctionCalls){
				if (node instanceof FunctionNode)
					if (!shouldVisitFunction((FunctionNode) node)){
						return false;
					}
					
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
		            		AstNode newNode=createNode(func, nodeForVarLog.get(i), statementCategory);
		            		appendNode(node, newNode);
		            		
				
		            	}
		            }

				 
				}
				else if (node instanceof Assignment){
				 
		            FunctionNode func=node.getEnclosingFunction();
		            String statementCategory="AssignmentComputation";
		            AstNode nodeForVarLog=node;
		            AstNode newNode=createNode(func, nodeForVarLog, statementCategory);
		         
		            appendNode(node, newNode);
					
				 
				}
				
				else if (node instanceof UnaryExpression){
					 
		            FunctionNode func=node.getEnclosingFunction();
		            String statementCategory="UnaryExpression";
		            AstNode nodeForVarLog=node;
		            AstNode newNode=createNode(func, nodeForVarLog, statementCategory);
		            appendNode(node, newNode);
		   	
				 
				}
				
	
					if (node.getParent() instanceof ElementGet){
			            FunctionNode func=node.getEnclosingFunction();
			            String statementCategory="ElementGet";
			            AstNode nodeForVarLog=node;
			            AstNode newNode=createNode(func, nodeForVarLog, statementCategory);
			            appendElemGetNode(node, newNode);
					}
	
			 
				else if (node instanceof ReturnStatement){
				 
		            FunctionNode func=node.getEnclosingFunction();
		            String statementCategory="ReturnStatementArgument";
		            AstNode nodeForVarLog=((ReturnStatement)node).getReturnValue();
		            if (!(nodeForVarLog instanceof KeywordLiteral) && nodeForVarLog!=null){
		            	AstNode newNode=createNode(func, nodeForVarLog, statementCategory);
		            
		            	AstNode parent = makeSureBlockExistsAround(node);

					/* the parent is something we can prepend to */
		            	parent.addChildBefore(newNode, node);
		            }
				 
				}
			 
				else if (node instanceof IfStatement){
				 
		            FunctionNode func=node.getEnclosingFunction();
		            String statementCategory="IfStatementCondition";
		            AstNode nodeForVarLog=((IfStatement) node).getCondition();
		            if (!(nodeForVarLog instanceof KeywordLiteral)){
		            	AstNode newNode=createNode(func, nodeForVarLog, statementCategory);
		            
		            	AstNode parent = makeSureBlockExistsAround(node);

					/* the parent is something we can prepend to */
		            	parent.addChildAfter(newNode, node);
		            }
				 
				}
			 
				else if (node instanceof ForLoop){
				 
		            FunctionNode func=node.getEnclosingFunction();
		            String statementCategory="ForLoopCondition";
		            AstNode nodeForVarLog=((ForLoop) node).getCondition();
		            if (!(nodeForVarLog instanceof KeywordLiteral)){
		            	AstNode newNode=createNode(func, nodeForVarLog, statementCategory);
		            
		            	AstNode parent = makeSureBlockExistsAround(node);

					/* the parent is something we can prepend to */
		            	parent.addChildAfter(newNode, node);
		            }
				}
			 
			 
				else if (node instanceof WhileLoop){
				 
		            FunctionNode func=node.getEnclosingFunction();
		            String statementCategory="WhileLoopCondition";
		            AstNode nodeForVarLog=((WhileLoop) node).getCondition();
		            if (!(nodeForVarLog instanceof KeywordLiteral)){
		            	AstNode newNode=createNode(func, nodeForVarLog, statementCategory);
		            
		            	AstNode parent = makeSureBlockExistsAround(node);

					/* the parent is something we can prepend to */
		            	parent.addChildAfter(newNode, node);
		            }
				 
				}
				else if (node instanceof SwitchStatement){
		            FunctionNode func=node.getEnclosingFunction();
		            AstNode nodeForVarLog=((SwitchStatement) node).getExpression();
		            String statementCategory="SwitchStatementCondition";
		            if (!(nodeForVarLog instanceof KeywordLiteral)){
		            	AstNode newNode=createNode(func, nodeForVarLog, statementCategory);
		            
		            	AstNode parent = makeSureBlockExistsAround(node);

					/* the parent is something we can prepend to */
		            	parent.addChildAfter(newNode, node);
		            }
				}
			}
			
			else{
				if(shouldTrackFunctionNodes){
					if(node instanceof FunctionNode){
						functionNodes.add(getFunctionName((FunctionNode)node));
					}
				}
				else{
					
					if (node instanceof FunctionCall
							&& !(((FunctionCall) node).getTarget() instanceof PropertyGet)
							&& !(node instanceof NewExpression)
							&& shouldVisitFunctionCall((FunctionCall)node)
							&& functionNodes.contains(((FunctionCall)node).getTarget().toSource())){
					
						FunctionNode callerFunc=node.getEnclosingFunction();
						if(!getFunctionName(callerFunc).equals("NoFunctionNode")){
							AstNode newNode=createFunctionTrackingNode(callerFunc, (FunctionCall) node);
							appendNodeAfterFunctionCall(node, newNode);
						}
		
					}
			    else
			    	if(node instanceof Name){
			    		if(node.getParent() instanceof PropertyGet 
			    				|| (node.getParent() instanceof FunctionCall 
			    						&& !((FunctionCall)node.getParent()).getTarget().toSource().equals(node.toSource()))){
			    			if(functionNodes.contains(node.toSource())){
			    				FunctionNode callerFunc=node.getEnclosingFunction();
			    				if(!getFunctionName(callerFunc).equals("NoFunctionNode")){
			    					AstNode newNode=createFunctionTypeNameTrackingNode(callerFunc, (Name) node);
			    					appendNodeAfterFunctionCall(node, newNode);
			    				}
			    			}
			    		}
			    	}
			    	else{
				    	if(node instanceof FunctionNode){
				    		if(node.getParent() instanceof FunctionCall
				    			&& !((FunctionCall)node.getParent()).getTarget().toSource().equals(node.toSource())
				    			|| !(node.getParent() instanceof FunctionCall)){
				    		
				    			if(functionNodes.contains(getFunctionName((FunctionNode) node))){
				    				FunctionNode callerFunc=node.getEnclosingFunction();
				    				if(!getFunctionName(callerFunc).equals("NoFunctionNode")){
				    					AstNode newNode=createFunctionTypeNameTrackingNode(callerFunc,node);
				    					appendNodeAfterFunctionCall(node, newNode);
				    				}
				    			}
				    		}
				    	}
			    	
			    	}
				
				    				
				}
			}
		
		
			return true;
			
		}
		
		
		protected abstract AstNode createFunctionTypeNameTrackingNode(FunctionNode callerFunc, AstNode node);
		/**
		 *  create node for logging variable/function-parameters
		 */
		protected abstract AstNode createNode(FunctionNode function, AstNode nodeForVarLog, String statementCategory);
		
		
		/**
		 * create node for tracking function calls
		 */
		
		protected abstract AstNode createFunctionTrackingNode(FunctionNode callerFunction, FunctionCall calleeFunction);
		/**
		 * This method is called when the complete AST has been traversed.
		 * 
		 * @param node
		 *            The AST root node.
		 */
		public abstract void finish(AstRoot node);

		/**
		 * This method is called before the AST is going to be traversed.
		 */
		public abstract void start();
		
		public void appendNode(AstNode node, AstNode newNode){
    		AstNode parent = node;
    		
    		
    		while (parent!=null && ! (parent instanceof ReturnStatement) && ! (parent instanceof ExpressionStatement)){
    			
    			parent=parent.getParent();
    			
    		}
    		
    		
    		
    		if (parent instanceof ReturnStatement){
    			AstNode attachBefore=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildBefore(newNode, attachBefore);
    			
    		}
    		
    		else if (parent!=null){
    			AstNode attachAfter=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildAfter(newNode, attachAfter);
    		}
    		
    	
		}
		
		
		public void appendElemGetNode(AstNode node, AstNode newNode){
    		AstNode parent = node;
    		
    		
    		while (parent!=null && ! (parent instanceof ReturnStatement) 
    				&& ! (parent instanceof ExpressionStatement) && ! (parent instanceof InfixExpression)){
    			
    			parent=parent.getParent();
    			
    		}
    		
    		
    		if (parent instanceof ReturnStatement){
    			AstNode attachBefore=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildBefore(newNode, attachBefore);
    			return;
    		}
    		
    		if (parent instanceof InfixExpression){
    			while(parent instanceof InfixExpression || parent instanceof ParenthesizedExpression){
    				parent=parent.getParent();
    			}
    		}
    		if (parent!=null){
    			AstNode attachAfter=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildAfter(newNode, attachAfter);
    		}
		}
		
		public void appendNodeAfterFunctionCall(AstNode node, AstNode newNode){
    		AstNode parent = node;
    		
    		
    		while (parent!=null && ! (parent instanceof ReturnStatement) && ! (parent instanceof ExpressionStatement)){
    			
    			if(parent instanceof IfStatement){
        			AstNode parentToAttach=makeSureBlockExistsAround(parent);
        			parentToAttach.addChildAfter(newNode, parent);
        			return;
    			}
    			if(parent.getParent() instanceof WhileLoop){
    				WhileLoop whileLoop=(WhileLoop) parent.getParent();
    				AstNode parentToAttach=makeSureBlockExistsAround(whileLoop.getBody());
    				parentToAttach.addChildrenToFront(newNode);
        			return;
    			}
    			
    			if(parent.getParent() instanceof ForLoop){
    				ForLoop forLoop=(ForLoop) parent.getParent();
    				AstNode parentToAttach=makeSureBlockExistsAround(forLoop.getBody());
    				parentToAttach.addChildrenToFront(newNode);
        			return;
    			}
    			parent=parent.getParent();
    			
    		}
    		
    		
    		
    		if (parent instanceof ReturnStatement){
    			AstNode attachBefore=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildBefore(newNode, attachBefore);
    			
    			
    		}
    		
    		else if (parent!=null){
    			AstNode attachAfter=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildAfter(newNode, attachAfter);
    		}
		}


}
