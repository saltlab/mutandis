package mutandis.analyser;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.crawljax.core.CrawljaxController;

public class StaticVariableAnalyser implements NodeVisitor  {




		protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());


		
		
		// generating and keeping the list of not executed functions nodes
		public static List<FunctionNode> notExecutedFuncNodes=new ArrayList<FunctionNode>();
		
		private String allStaticAnalysedVariables="";
		
	

		

		
		/**
		 * constructor without specifying functions that should be visited
		 */
		protected StaticVariableAnalyser(){
			
		
			
			
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
			
				
			if(node instanceof FunctionNode){
				FunctionNode funcNode=(FunctionNode)node;
				if(FunctionCallTraceAnalyser.NotExecutedFuncs.contains(getFunctionName(funcNode))){
					StaticVariableFinder stVarSelc=new StaticVariableFinder();
					AstNode funcBody=funcNode.getBody();
					stVarSelc.visit(funcBody);
					allStaticAnalysedVariables+=stVarSelc.getStaticGeneratedVarAnalyserString();
					notExecutedFuncNodes.add(funcNode);
					
				}
				
			
			}

			return true;
			
		}
		
		public String getStaticAnalysedVariables(){
			return allStaticAnalysedVariables;
		}
		
		
		
		
		

		

		
		

}
