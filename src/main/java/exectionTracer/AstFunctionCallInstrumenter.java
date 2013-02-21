package exectionTracer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;

import astModifier.JSASTModifier;

import com.crawljax.util.Helper;

public class AstFunctionCallInstrumenter extends JSASTModifier {

	public static final String JSINSTRUMENTLOGNAME = "window.jsFuncCallExecTrace";




	/**
	 * Construct without patterns.
	 */
	public AstFunctionCallInstrumenter() {
		super(true);
	
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

	/**
	 * for now do nothing here
	 */
	@Override
	public AstNode createNode(FunctionNode callerFunction, AstNode nodeForVarLog, String statementCategory){


		return null;
		
	}
	
	
	@Override
	protected AstNode createFunctionTrackingNode(FunctionNode callerFunction, FunctionCall calleeFunction) {
		
		String callerFunctionName=getFunctionName(callerFunction);
		String calleeFunctionName=calleeFunction.getTarget().toSource();
		if(calleeFunctionName.contains(".")){
			String[]callee=calleeFunctionName.split("\\.");
			calleeFunctionName=callee[callee.length-1];
		}
		calleeFunctionName = Helper.removeNewLines(calleeFunctionName);
		/* escape quotes */
		calleeFunctionName = calleeFunctionName.replaceAll("\\\"", "\\\\\"");
		calleeFunctionName = calleeFunctionName.replaceAll("\\\'", "\\\\\'");
		int lineNo=calleeFunction.getLineno();
		String code=
			"send(new Array('" + getScopeName() + "::" + callerFunctionName + "', '" + lineNo +  
            "', new Array(";
		
		code += "addFunctionCallTrack('" + callerFunctionName + "'" + ", " + "'" + 
		calleeFunctionName + "'"+"))));";

		return parse(code);
	
	}
	
	@Override
	public AstNode createFunctionTypeNameTrackingNode(FunctionNode callerFunction, AstNode calleeFunction) {
		
		String callerFunctionName=getFunctionName(callerFunction);
		String calleeFunctionName="";
	
		if(calleeFunction instanceof Name)
			calleeFunctionName=calleeFunction.toSource();
		else
			if(calleeFunction instanceof FunctionNode)
				calleeFunctionName=getFunctionName((FunctionNode)calleeFunction);
		if(calleeFunctionName.contains(".")){
			String[]callee=calleeFunctionName.split("\\.");
			calleeFunctionName=callee[callee.length-1];
		}
		calleeFunctionName = Helper.removeNewLines(calleeFunctionName);
		/* escape quotes */
		calleeFunctionName = calleeFunctionName.replaceAll("\\\"", "\\\\\"");
		calleeFunctionName = calleeFunctionName.replaceAll("\\\'", "\\\\\'");
		int lineNo=calleeFunction.getLineno();
		String code=
			"send(new Array('" + getScopeName() + "::" + callerFunctionName + "', '" + lineNo +  
            "', new Array(";
		
		code += "addFunctionCallTrack('" + callerFunctionName + "'" + ", " + "'" + 
		calleeFunctionName + "'"+"))));";

		return parse(code);
	
	}
	
	
	private AstNode jsLoggingFunctions() {
		String code;

		File js = new File(this.getClass().getResource("/addFunctionCallTrack.js").getFile());
		code = Helper.getContent(js);
		return parse(code);
	}







	

	
	
	

}
