package jsOperators;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import systemProperties.RandomGenerator;
import systemProperties.SystemProps;



public class JsCodeOperators {
	
	

	public static final Map<String, ArrayList<String>> jsCodeOps=new HashMap<String, ArrayList<String>>();
	public static final ArrayList<Integer> relationalOps=new ArrayList<Integer>(Arrays.asList(12, 13, 14, 15,
															16, 17, 46, 47));
	public static final ArrayList<Integer> unaryArithmeticOps=new ArrayList<Integer>(Arrays.asList(106, 107));
	public static final ArrayList<Integer> logicOps=new ArrayList<Integer>(Arrays.asList(104, 105));
	private static Random rnd= new Random(10);


	
	
	
	
	public JsCodeOperators(){

		jsCodeOps.put("FunctionCall",  
				new ArrayList<String> (Arrays.asList("c_funcName",
				"s_arg",
				"r_arg"
				)));
		
		
		jsCodeOps.put("FunctionNode", 
				new ArrayList<String> (Arrays.asList("c_funcName",
				"s_param",
				"r_param")));
		
		
		
		jsCodeOps.put("IfStatement",
				new ArrayList<String> (Arrays.asList("c_const",
						"c_name",
						"relOps",
						"logOps",
						"arithOps",
						"r_else",
						"0_false",
						"false_0",
						"1_true",
						"true_1",
						"r_else_keyword",
						"true_false"
						)));
		
		jsCodeOps.put("SwitchStatement",
				new ArrayList<String> (Arrays.asList("s_case"
						)));
		
		jsCodeOps.put("VariableDeclaration",
				new ArrayList<String> (Arrays.asList("r_declaration"
						)));
		
		jsCodeOps.put("Assignment",
				new ArrayList<String> (Arrays.asList(
						"r_assign",
						"c_value",
						"x_'x'",
						"arithOps"
						)));
		
		jsCodeOps.put("ForLoop",
				new ArrayList<String> (Arrays.asList("c_init",
						"c_increment_opsType",
						"c_increment_unrOpPos",
						"c_const",
						"c_name",
						"relOps",
						"arithOps",
						"logOps",
						"s_for"
						
						)));
		
		jsCodeOps.put("WhileLoop",
				new ArrayList<String> (Arrays.asList("c_const",
						"c_name",
						"c_unrOpType",
						"c_unrOpPos",
						"relOps",
						"logOps",
						"s_while",
						"arithOps"
						)));
		

	
		jsCodeOps.put("ReturnStatement",
				new ArrayList<String> (Arrays.asList("r_return",
						"c_retValue")));
		
		jsCodeOps.put("BreakStatement",
				new ArrayList<String> (Arrays.asList("r_break")));
		
		jsCodeOps.put("ContinueStatement",
				new ArrayList<String> (Arrays.asList("r_continue")));
		
		jsCodeOps.put("jsSpecific",		
				new ArrayList<String> (Arrays.asList("/g_replace",
						"r_parseInt",
						"r_this",
						"setTimeout",	
						"setInterval",
						"funcCallRemoveParen",
						"add_var",
						"r_var",
						"null_undefined",
						"x!==false_x",
						"x===true_x",
						"x===false_!x",
						"x!==true_!x",
						"ajax_callback_status",
						"ajax_c_reqType")));	
		
	}
	
	
/*	public String getRandomJsCodeOperator(){
		
		int keySize= jsCodeOps.keySet().size();
		String codeSeg=(String) jsCodeOps.keySet().toArray()[SystemProps.rnd.getNextRandomInt(keySize)];
		int entrySize= jsCodeOps.get(codeSeg).size();
		String jsOps= jsCodeOps.get(codeSeg).get(SystemProps.rnd.getNextRandomInt(entrySize));
		return (jsOps);

	}
*/	
/*	public String getJsCodeSeg(String operator){
	    for (Entry entry : jsCodeOps.entrySet()) {
	        if (operator.equals(entry.getValue())) {
	            return (String) entry.getKey();
	        }
	    }
	    return "";
	}
*/	
	public String getRandomJsCodeOperator(String codeSeg){
		
		int entrySize= jsCodeOps.get(codeSeg).size();
		return jsCodeOps.get(codeSeg).get(SystemProps.oprSelecRnd.getNextRandomInt(entrySize));
		
	}
	
	public int getRandomRelOps(){
		
		int randOps=relationalOps.get(rnd.nextInt(relationalOps.size()));
		return(randOps);
	}
	
	
	public int getRandomLogicOps(){
	
		int randOps=logicOps.get(rnd.nextInt(logicOps.size()));
		return(randOps);
	}
	
	

	public int getRandomUnaryArithmeticOps(){

		int randOps=unaryArithmeticOps.get(rnd.nextInt(unaryArithmeticOps.size()));
		return(randOps);
	}
	
}
