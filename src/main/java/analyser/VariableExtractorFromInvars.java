package analyser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import com.crawljax.util.Helper;


public class VariableExtractorFromInvars {
	
	/*
	 * <function name, vars>
	 */
	
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
	
	private HashMap<String, ArrayList<String>> functionVarMap;
	private String outputFolder;
	
	public VariableExtractorFromInvars (String outputFolder){
		this.outputFolder=Helper.addFolderSlashIfNeeded(outputFolder)+"daikon.assertions";
		functionVarMap=new HashMap<String, ArrayList<String>>();
		HashMap<String,ArrayList<String>> varfuncMap=startAnalysingInvarFile();
		functionVarMap=extractVars(varfuncMap);
/*		Set keys=functionVarMap.keySet();
		Iterator it=keys.iterator();
		while(it.hasNext()){
			String name=(String)it.next();
			System.out.print(name+"$$$$"+"\n");
			for(Name n:functionVarMap.get(name)){
				System.out.println(n.getIdentifier()+"\n");
			}
		}
*/	}
	
	public HashMap<String,ArrayList<String>> getFunctionVarMap(){
		return functionVarMap;
	}
	private HashMap<String,ArrayList<String>> startAnalysingInvarFile(){
		HashMap<String,ArrayList<String>> varfuncMap=new HashMap<String,ArrayList<String>>();
		try{
			
			BufferedReader input =
				new BufferedReader(new FileReader(outputFolder));
			String line="";
			  while ((line = input.readLine()) != null){
				  
				
				  if(line.contains(":::POINT")){
					  while(!line.equals("============================" +
				  		"===============================================")){
						
						  line=input.readLine();
						  if(line==null)
							  return varfuncMap;
					  }
				  }
				  
				  if(line.contains("Warning") || line.contains("warning") || line.equals("============================" +
				  		"==============================================="))
					  continue;
				  String[] firstLine=line.split(":::");
				  line=line.replace(":::"+firstLine[firstLine.length-1], "");
				  String[] funcNameandPathArray=firstLine[0].split("\\.");
				  String funcName=funcNameandPathArray[funcNameandPathArray.length-1];
				  String funcNameAndPath=line.replace("."+funcName, "::"+funcName);
				  funcNameAndPath=funcNameAndPath.split("::")[1];
				  ArrayList<String> invarList=new ArrayList<String>();
				  while((line = input.readLine()) != null && !line.equals("============================" +
				  		"===============================================")){
					  if(line.contains("Warning") || line.contains("warning")
							  || line.contains("\\old") || line.contains("orig(")
						      || line.contains("has only one value") || line.contains("unquoted")){
						  continue;
					  }
					  if ("".equals(line))
							break;
					  
					  line=getJSFormatLine(line);
				
					  invarList.add(line);
				  }
				  if(varfuncMap.containsKey(funcNameAndPath)){
					  ArrayList<String> varList=varfuncMap.get(funcNameAndPath);
					  for(String var:varList){
						  if(!invarList.contains(var)){
							  invarList.add(var);
						  }
					  }
				  }
				  varfuncMap.put(funcNameAndPath, invarList);
				  
				  
					  
			  }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return varfuncMap;
		
	}
	private HashMap<String,ArrayList<String>>  extractVars(HashMap<String, ArrayList<String>> invarMapper){
	
		HashMap<String,ArrayList<String>> varfuncMap=new HashMap<String,ArrayList<String>>();
		if(invarMapper.size()==0)
			return varfuncMap;
		Set<String> keyset=invarMapper.keySet();
		Iterator<String> keyIter= keyset.iterator();
		while(keyIter.hasNext()){
			String funcName=(String) keyIter.next();
			ArrayList<String> invars=invarMapper.get(funcName);
			ArrayList<String> vars=new ArrayList<String>();
			for (int i=0;i<invars.size();i++){
				
				vars.addAll(extractVariableFromRule(invars.get(i)));
				
			}
			varfuncMap.put(funcName, vars);
		}
		return varfuncMap;
		
		
		
	}
	
	private String getJSFormatLine(String expression){
		
		String[] s=null;
		if (Pattern.matches(".*size\\(.*\\[\\]\\).*",expression)){
			expression=expression.replace("[]", "");
			if (Pattern.matches(".*size\\(.*\\) ==.*",expression)){
				s=null;
				s=expression.split("==");
				expression="isSizeEqual"+"("+s[0]+","+s[1]+")";
			}
		}
		
		if(Pattern.matches(".*one of.*", expression)){
			    expression=expression.replace("[] ", "");
			    expression=expression.replace("elements ", "");
				s=expression.split("one of");
				
				
				expression="isElementIn"+"("+s[0]+","+s[1].replace("{", "[").replace("}", "]")+")";
				
		}
		if(expression.contains("<==>")) {
			s=null;
			expression=expression.replace("<==>", "").replace("(", "").replace(")", "");
			s=expression.split(" ");
			expression="twoWayIf" + "(" + s[0];
			for (int i=1;i<s.length;i++) {
				if (!s[i].equals("")) {
					if (s[i].equals("=="))
						s[i]="\"==\"";
					else if (s[i].equals(">"))
						s[i]="\">\"";
					else if (s[i].equals("<"))
						s[i]="\"<\"";
					expression+= "," + s[i];
				}
			}
			expression+=")";
			
		}
		
		else if(expression.contains("==>")) {
			s=null;
			expression=expression.replace("==>", "").replace("(", "").replace(")", "");
			s=expression.split(" ");
			expression="oneWayIf" + "(" + s[0];
			for (int i=1;i<s.length;i++) {
				if (!s[i].equals("")) {
					if (s[i].equals("=="))
						s[i]="\"==\"";
					else if (s[i].equals(">"))
						s[i]="\">\"";
					else if (s[i].equals("<"))
						s[i]="\"<\"";
					expression+= "," + s[i];
				}
			}
			expression+=")";
			
		}
		else if (Pattern.matches(".*>=.*", expression)){
			s=null;
			if (!Pattern.matches(".*[\\/\\\\'\"].*", expression) && !expression.contains("sorted by")){
			    expression=expression.replace("[] ", "");
			    expression=expression.replace("elements ", "");
				s=expression.split(">=");	
				expression="greaterThanOrEqual"+"("+s[0]+","+s[1]+")";
			}
			
		}
		else if (Pattern.matches(".*>.*", expression)){
			s=null;
			if (!Pattern.matches(".*[\\/\\\\'\"\\=].*", expression) && !expression.contains("sorted by")){
			    expression=expression.replace("[] ", "");
			    expression=expression.replace("elements ", "");
				s=expression.split(">");
				expression="greaterThan"+"("+s[0]+","+s[1]+")";
			}
			
		}

		else if (Pattern.matches(".*<=.*", expression)){
			s=null;
			if (!Pattern.matches(".*[\\/\\\\'\"].*", expression) && !expression.contains("sorted by")){
			    expression=expression.replace("[] ", "");
			    expression=expression.replace("elements ", "");
				s=expression.split("<=");
				expression="smallerThanOrEqual"+"("+s[0]+","+s[1]+")";
			}
		}
		else if (Pattern.matches(".*<.*", expression)){
			s=null;
			if (!Pattern.matches(".*[\\/\\\\'\"=].*", expression) && !expression.contains("sorted by")){
			    expression=expression.replace("[] ", "");
			    expression=expression.replace("elements ", "");
				s=expression.split("<");
				expression="smallerThan"+"("+s[0]+","+s[1]+")";
			}
		}

		
		else if (Pattern.matches(".*\\[\\] == .*\\[\\]", expression)){	
			s=null;
			expression=expression.replace("[]", "");
			s=expression.split("==");
			if (s[1].equals(" "))
				s[1]="[]";
			expression="pairwiseSequenceEqual"+"("+s[0]+","+s[1]+")";
		}
		
		else if (Pattern.matches(".*\\[\\] == .*", expression) || Pattern.matches(".*\\[\\] elements == .*", expression)){	
			s=null;
			expression=expression.replace("[] ", "");
			expression=expression.replace("elements ", "");
	//		s=expression.split("==");
	//		expression="isElementIn"+"("+s[0]+","+"["+s[1]+"]"+")";
		}
		else if (Pattern.matches(".*\\[\\] elements == false", expression) || Pattern.matches(".*\\[\\] elements == true", expression)){
			s=null;
			expression=expression.replace("[] ", "");
			 expression=expression.replace("elements ", "");
			s=expression.split("==");
			expression="equalBoolean"+"("+s[0]+","+s[1]+")";
		}
		else if (Pattern.matches(".*\\[\\] elements == null", expression)){
			expression=expression.replace("[] elements", "");
			
		}
		else if (Pattern.matches(".*\\[\\] elements != null", expression)){
			expression=expression.replace("[] elements", "");
			
		}
		if (expression.contains("==")){
				if (expression.contains("== \"null\"")){
					expression=expression.replace("\"null\"", "null");
				}
			expression=expression.replace("==", ",");
			if (expression.contains("||") || expression.contains("&&")){
				expression=expression.replace("||", ") || equality(").replace("&&", ") && equality(");
			}
			expression="equality" + "(" + expression + ")";
		}
		else if (expression.contains("!=")){
			if (expression.contains("!= \"null\"")){
				expression=expression.replace("\"null\"", "null");
			}
		expression=expression.replace("!=", ",");
		if (expression.contains("||") || expression.contains("&&")){
			expression=expression.replace("||", ") || inequality(").replace("&&", ") && inequality(");
		}
		expression="inequality" + "(" + expression + ")";
		}
		if (Pattern.matches(".*in .*\\[\\]", expression)){
			s=null;
			expression=expression.replace("[]", "");
			s=expression.split("in");
			expression="memberOf"+"("+s[0]+","+s[1]+")";
		}
		if (expression.contains("sorted by <=")) {
			String array=expression.replace("[] sorted by <=", "");
			expression="isIncreasingOrEqualSorted" + "(" + array + ")";
		}
		else if (expression.contains("sorted by <")) {
			String array=expression.replace("[] sorted by <", "");
			expression="isIncreasingSorted" + "(" + array + ")";
		}
		else if (expression.contains("sorted by >=")) {
			String array=expression.replace("[] sorted by >=", "");
			expression="isDecreasingOrEqualSorted" + "(" + array + ")";
		}

		else if (expression.contains("sorted by >")) {
			String array=expression.replace("[] sorted by >", "");
			expression="isDecreasingSorted" + "(" + array + ")";
		}
		return expression;
	}
	
	
	

	private AstNode parse(String code) {
		
		Parser p = new Parser(compilerEnvirons, null);
	
		return p.parse(code, null, 0);
		
		
	}
	private List<String> extractVariableFromRule(String invarRule){
		List<String> nameNodes=new ArrayList<String>();

		AstNode node=(AstNode) parse (invarRule).getFirstChild();
		ArrayList<String> namesInInfixExp=new ArrayList<String>();
		if (node instanceof ExpressionStatement){
			
			if (((ExpressionStatement)node).getExpression() instanceof InfixExpression) {
				
				InfixExpression infixNode=(InfixExpression) ((ExpressionStatement)node).getExpression();
				
				nameNodes=searchInvarForNames(infixNode,namesInInfixExp);
			}
			else
				if (((ExpressionStatement)node).getExpression() instanceof FunctionCall){
					
					FunctionCall functionCall=(FunctionCall) ((ExpressionStatement)node).getExpression();
					List<AstNode> args=new ArrayList<AstNode>();
					args=functionCall.getArguments();
					for (int i=0;i<args.size();i++){
						
						if (args.get(i) instanceof Name){
							nameNodes.add(((Name) args.get(i)).getIdentifier());
						}
					}
				}
				
		}
		
		
		
		
		
		return nameNodes;
	}
	
	

	private List<String> searchInvarForNames(AstNode node, ArrayList<String> namesInInfixExp){
		if (node instanceof InfixExpression){
		
			if (((InfixExpression)node).getLeft() instanceof InfixExpression){
				searchInvarForNames(((InfixExpression)node).getLeft(),namesInInfixExp);
			}
			
			if (((InfixExpression)node).getRight() instanceof InfixExpression){
				searchInvarForNames(((InfixExpression)node).getRight(),namesInInfixExp);
			}
		
			if (((InfixExpression)node).getLeft() instanceof Name){
				Name name=(Name) ((InfixExpression)node).getLeft();
				namesInInfixExp.add(name.getIdentifier());
			}
		
			if (((InfixExpression)node).getRight() instanceof Name){
				Name name=(Name) ((InfixExpression)node).getRight();
				namesInInfixExp.add(name.getIdentifier());
			}
		}
		
		
		return namesInInfixExp;
		
	}
	


}
