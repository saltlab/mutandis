package mutandis.analyser;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WhileLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.crawljax.core.CrawljaxController;

/* this class is used to find callers of a given function 
 * which has not been executed during execution time/crawling */
public class StaticFunctionCallGraph implements NodeVisitor  {


		

		protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
	

		private String notExecutedFunction;
		private List<String> callerOfNotExecutedFunc=new ArrayList<String>();
		
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

		

		
		/**
		 * constructor without specifying functions that should be visited
		 */
		protected StaticFunctionCallGraph(String notExecFunction){
			
			notExecutedFunction=notExecFunction;
		
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
		
		
		

		
		
		
		/**
		 * Actual visiting method.
		 * 
		 * @param node
		 *            The node that is currently visited.
		 * @return Whether to visit the children.
		 */
		@Override
		public boolean visit(AstNode node) {
			if(!notExecutedFunction.contains("anonymous")){
				if(node instanceof FunctionCall){
					FunctionCall funcCallNode=(FunctionCall) node;
					if(funcCallNode.getTarget().toSource().equals(notExecutedFunction)){
						FunctionNode caller=funcCallNode.getEnclosingFunction();
						String callerName=getFunctionName(caller);
						callerOfNotExecutedFunc.add(callerName);
					}
					
				
				}
			}
			
			else{
				
				if(node instanceof FunctionNode){
					FunctionNode funcNode=(FunctionNode) node;
					String funcNodeName=getFunctionName(funcNode);
					if(funcNodeName.equals(notExecutedFunction)){
						/* x.click(function(){})*/
						if(funcNode.getParent() instanceof FunctionCall){
							FunctionNode callerFunc=funcNode.getEnclosingFunction();
							String callerName=getFunctionName(callerFunc);
							callerOfNotExecutedFunc.add(callerName);
						}
						/* this.f=function(){}; or var a=function(){}; */
						else if(funcNode.getParent() instanceof Assignment){
							Assignment assignNode=(Assignment)funcNode.getParent();
							if(assignNode.getLeft() instanceof PropertyGet){
								PropertyGet propGet=(PropertyGet) assignNode.getLeft();
								if(propGet.getRight() instanceof Name){
									String notExecedFuncName=((Name)propGet.getRight()).getIdentifier();
									StaticFunctionCallFinder sFuncCallFinder=new StaticFunctionCallFinder(notExecedFuncName);
									sFuncCallFinder.visit(node.getAstRoot());
									callerOfNotExecutedFunc=sFuncCallFinder.getCallerOfNotExecutedFunc();
									return false;
								}
							}
							
							else if(assignNode.getLeft() instanceof VariableDeclaration){
								VariableDeclaration varDec=(VariableDeclaration) assignNode.getLeft();
								AstNode varTarget=varDec.getVariables().get(0).getTarget();
								if(varTarget instanceof Name){
									String notExecedFuncName=((Name)varTarget).getIdentifier();
									StaticFunctionCallFinder sFuncCallFinder=new StaticFunctionCallFinder(notExecedFuncName);
									sFuncCallFinder.visit(node.getEnclosingScope());
									callerOfNotExecutedFunc=sFuncCallFinder.getCallerOfNotExecutedFunc();
									return false;
								}
							}
						}

					}
				}
				
				
			}
			
			
			return true;
			
		}
		
		public List<String> getCallerOfNotExecutedFunc(){
			return callerOfNotExecutedFunc;
		}
		
		public String getNotExecutedFunction(){
			return notExecutedFunction;
		}
		

		
		
}
