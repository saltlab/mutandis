package mutator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.*;

import com.crawljax.util.Helper;

import systemProperties.SystemProps;
import jsOperators.JsCodeOperators;

public class NodeMutator {
	
	public static JsCodeOperators jsCodeOperator=new JsCodeOperators();
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
	private static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");
	private String dir;
	private String resultFilenameAndPath;
	private static String format=String.format("%s", sdf.format(new Date())) +".txt";
	private String scopeName;
	
	public NodeMutator(String outputFolder,String scopename){
		try{
			String folder=Helper.addFolderSlashIfNeeded(outputFolder);
			dir=folder + Helper.addFolderSlashIfNeeded("mutationStuff");
			resultFilenameAndPath=dir+format;
			Helper.directoryCheck(dir);
			scopeName=scopename;

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//target is variable a: selectedVarForMutation >> for example <"FunctionCall","test(a,b)", AstNode for a>
	public boolean mutateVariable(List<Object> selectedVarForMutation){
		
		StringBuffer result=new StringBuffer();
		boolean ischanged=false;
		String typeOfcode=(String)selectedVarForMutation.get(0);
		AstNode nodeToMutate=(AstNode)selectedVarForMutation.get(1);
		AstNode target=(AstNode)selectedVarForMutation.get(2);
		result.append(scopeName+"\n"+"----------"+"\n");
		result.append(getFunctionName(nodeToMutate.getEnclosingFunction())+"\n"+"----------"+"\n");
		result.append(typeOfcode+"\n"+"----------"+"\n");
		result.append(nodeToMutate.toSource()+"\n"+"----------"+"\n");
	/*	if(nodeToMutate.getEnclosingFunction()!=null)
			result.append(nodeToMutate.getEnclosingFunction().toSource()+"\n"+"----------"+"\n");
*/	//	TargetVarFinder targetVarFinder=new TargetVarFinder(target);
		String operator="";
		while(!ischanged){
			operator=jsCodeOperator.getRandomJsCodeOperator(typeOfcode);
			
			if(typeOfcode.equals("FunctionCall")){
				if(operator.equals("c_funcName")){
					ischanged=functionCallChangeName(nodeToMutate);
				}
				else if(operator.equals("s_arg")){
					ischanged=functionCallSwapArg(nodeToMutate,target);
				}
				else if(operator.equals("r_arg")){
					ischanged=functionCallRemoveArg(nodeToMutate,target);
				}
			
			}
			else if(typeOfcode.equals("FunctionNode")){
				if(operator.equals("c_funcName")){
					ischanged=functionNodeChangeName(nodeToMutate);
				}
				else if(operator.equals("s_param")){
					ischanged=functionNodeSwapParam(nodeToMutate,target);
				}
				else if(operator.equals("r_param")){
					ischanged=functionNodeRemoveParam(nodeToMutate,target);
				}
			
			}
			else if(typeOfcode.equals("Assignment")){
				if(operator.equals("r_assign")){
					ischanged=removeAssignment(nodeToMutate);
				}
				else if(operator.equals("c_value")){
					ischanged=changeAssignedValue(nodeToMutate);
				}
				else if(operator.equals("x_'x'")){
					ischanged=changeVariableTypeNumbStr(nodeToMutate);
				}
				else if(operator.equals("arithOps")){
					ischanged=changeAssignmentArithOps(nodeToMutate);
					
				}
			}
			else if(typeOfcode.equals("VariableDeclaration")){
					if(operator.equals("r_declaration")){
						ischanged=removeVarDeclaration(nodeToMutate,target);
					}
					if(!ischanged){
						result.append(operator+"\n"+"----------"+"\n");
						result.append("ischanged: "+ischanged +"\n");
						result.append("================"+"\n");
						writeResultsToFile(result.toString());
						return ischanged;
					}
			}
			else if(typeOfcode.equals("ReturnStatement")){
					if(operator.equals("r_return")){
						ischanged=removeReturnStatement(nodeToMutate);
					}
			}
		}
		result.append(operator+"\n"+"----------"+"\n");
		result.append("ischanged: "+ischanged +"\n");
		result.append("================"+"\n");
		writeResultsToFile(result.toString());
		return ischanged;
	}
		
	public void writeResultsToFile(String result){
		try{
			Helper.writeToFile(resultFilenameAndPath, result, true);
				
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean mutateBranchStatements(List<Object> selectedBranchForMutation){
		StringBuffer result=new StringBuffer();
		boolean ischanged=false;
		String typeOfcode=(String)selectedBranchForMutation.get(0);
		AstNode nodeToMutate=(AstNode)selectedBranchForMutation.get(1);
		result.append(scopeName+"\n"+"----------"+"\n");
		result.append(getFunctionName(nodeToMutate.getEnclosingFunction())+"\n"+"----------"+"\n");
		result.append(typeOfcode+"\n"+"----------"+"\n");
		result.append(nodeToMutate.toSource()+"\n"+"----------"+"\n");
/*		if(nodeToMutate.getEnclosingFunction()!=null)
			result.append(nodeToMutate.getEnclosingFunction().toSource()+"\n"+"----------"+"\n");
*/		String operator="";
		while(!ischanged){
			operator=jsCodeOperator.getRandomJsCodeOperator(typeOfcode);
			if(typeOfcode.equals("IfStatement")){
				if(operator.equals("c_const")){
					ischanged=ifStatementChangeConst(((IfStatement)nodeToMutate).getCondition());
				}
				else if(operator.equals("c_name")){
					ischanged=ifStatementChangeName(((IfStatement)nodeToMutate).getCondition());
				}
				else if(operator.equals("relOps")){
					ischanged=ifStatementChangeRelOps(((IfStatement)nodeToMutate).getCondition());
				}
				else if(operator.equals("logOps")){
					ischanged=ifStatementChangeLogicOps(((IfStatement)nodeToMutate).getCondition());
				}
				else if(operator.equals("arithOps")){
					ischanged=ifStatementChangeArithOps(((IfStatement)nodeToMutate).getCondition());
				}
				else if(operator.equals("r_else")){
					ischanged=ifStatementRemoveElsePart(nodeToMutate);
				}
				else if(operator.equals("0_false") || operator.equals("1_true")){
					ischanged=ifStatementSwapNumBoolean(((IfStatement)nodeToMutate).getCondition());
				}
				else if(operator.equals("false_0") || operator.equals("true_1")){
					ischanged=ifStatementSwapBooleanNum(((IfStatement)nodeToMutate).getCondition());
				}
				else if(operator.equals("r_else_keyword")){
					ischanged=ifStatementRemoveElseKeyword(nodeToMutate);
				}
				else if(operator.equals("true_false")){
					ischanged=ifStatementSwapTrueFalse(((IfStatement)nodeToMutate).getCondition());
				}
			}
			else if(typeOfcode.equals("ForLoop")){
				if(operator.equals("c_init")){
					ischanged=changeForLoopInitPart(nodeToMutate);
				}
				else if(operator.equals("c_increment_opsType")){
					ischanged=changeForLoopIncPartOprType(nodeToMutate);
				}
				else if(operator.equals("c_increment_unrOpPos")){
					ischanged=changeForLoopIncPartUnrOprPosition(nodeToMutate);
				}
				else if(operator.equals("c_const")){
					ischanged=changeForLoopNumInCndn(nodeToMutate);
				}
				else if(operator.equals("c_name")){
					ischanged=changeForLoopNameInCndn(nodeToMutate);
				}
				else if(operator.equals("relOps")){
					ischanged=changeForLoopRelOpsInCndn(nodeToMutate);
				}
				else if(operator.equals("arithOps")){
					ischanged=changeForLoopArithOpsInCndn(nodeToMutate);
				}
				else if(operator.equals("logOps")){
					ischanged=changeForLoopLogOpsInCndn(nodeToMutate);
				}
				else if(operator.equals("s_for")){
					ischanged=changeConsecForLoopsOrder((ForLoop)nodeToMutate);
				}
						
			}
			else if(typeOfcode.equals("WhileLoop")){
				if(operator.equals("c_const")){
					ischanged=changeWhileLoopNum(nodeToMutate);
				}
				else if(operator.equals("c_name")){
					ischanged=changeWhileLoopName(nodeToMutate);
				}
				else if(operator.equals("c_unrOpType")){
					ischanged=changeWhileLoopUnrOprType(nodeToMutate);
				}
				else if(operator.equals("c_unrOpPos")){
					ischanged=changeWhileLoopUnrOprPosition(nodeToMutate);
				}
				else if(operator.equals("relOps")){
					ischanged=changeWhileLoopRelOpsInCndn(nodeToMutate);
				}
				else if(operator.equals("logOps")){
					ischanged=changeWhileLoopLogOpsInCndn(nodeToMutate);
				}
				else if(operator.equals("arithOps")){
					ischanged=changeWhileLoopArithOpsInCndn(nodeToMutate);
				}
				else if(operator.equals("s_while")){
					ischanged=changeConsecWhileLoopsOrder((WhileLoop)nodeToMutate);
				}
				
			}
			else if(typeOfcode.equals("SwitchStatement")){
				if(operator.equals("s_case")){
					ischanged=switchCaseSwapCase(nodeToMutate);
				}
				
			}
			else if(typeOfcode.equals("ReturnStatement")){
				if(operator.equals("r_return")){
					ischanged=removeReturnStatement(nodeToMutate);
				}
				else if(operator.equals("c_retValue")){
					ischanged=changeBoolReturnVal(nodeToMutate);
				}
				
			}
			else if(typeOfcode.equals("BreakStatement")){
				if(operator.equals("r_break")){
					ischanged=removeBreakStatement(nodeToMutate);
				}
			}
			else if(typeOfcode.equals("ContinueStatement")){
				if(operator.equals("r_continue")){
					ischanged=removeContinueStatement(nodeToMutate);
				}
			}
			
		}
		result.append(operator+"\n");
		result.append("================"+"\n");
		writeResultsToFile(result.toString());
		return ischanged;
		
	}
	
	public boolean mutateJsSpecfic(List<Object> jsSpecific){
		StringBuffer result=new StringBuffer();
		boolean ischanged=false;
		String typeOfcode=(String)jsSpecific.get(0);
		AstNode nodeToMutate=(AstNode)jsSpecific.get(1);

		
		if(typeOfcode.equals("/g_replace")){
			ischanged=functionCallChangeReplaceMethod(nodeToMutate);
		}
		else if(typeOfcode.equals("r_parseInt")){
			ischanged=functionCallChangeParseIntMethod(nodeToMutate);
		}
		else if(typeOfcode.equals("r_this")){
			ischanged=removePropertyGetThis(nodeToMutate);
		}
		else if(typeOfcode.equals("setTimeout") 
				|| typeOfcode.equals("setInterval")){
			ischanged=functionCallChangeSetTimeout(nodeToMutate);
		}

		else if(typeOfcode.equals("funcCallRemoveParen")){
			ischanged=functionCallRemoveParen(nodeToMutate);
		}
		else if(typeOfcode.equals("add_var")){
			ischanged=addVarKeyword(nodeToMutate);
		}
		else if(typeOfcode.equals("r_var")){
			ischanged=removeVarKeyword(nodeToMutate);
		}
		else if(typeOfcode.equals("null_undefined")){
			ischanged=ifStatementSwapNullUndef(nodeToMutate);
			if(!ischanged)
				ischanged=ifStatementSwapUndefNull(nodeToMutate);
		}
		else if(typeOfcode.equals("x===false_!x")
				|| typeOfcode.equals("x!==true_!x")){
			ischanged=ifStatementSwapFalseBoolPattern(nodeToMutate);
		}
		else if(typeOfcode.equals("x!==false_x")
				|| typeOfcode.equals("x===true_x")){
			ischanged=ifStatementSwapTrueBoolPattern(nodeToMutate);
		}
		else if(typeOfcode.equals("ajax_callback_status")){
			ischanged=ajaxChangeCallbackStatus(nodeToMutate);
		}
		else if(typeOfcode.equals("ajax_c_reqType")){
			ischanged=ajaxChangeRequestType(nodeToMutate);
		}
		
		if(ischanged){
			result.append(scopeName+"\n"+"----------"+"\n");
			result.append(getFunctionName(nodeToMutate.getEnclosingFunction())+"\n"+"----------"+"\n");
			result.append(typeOfcode+"\n"+"----------"+"\n");
			result.append(nodeToMutate.toSource()+"\n");
			result.append("================"+"\n");
			writeResultsToFile(result.toString());
		}
		return ischanged;
		
	}
	
	
	public boolean mutateDomJsCodeLevel(List<Object> domJsCodeLevel){
		StringBuffer result=new StringBuffer();
		boolean ischanged=false;
		String typeOfcode=(String)domJsCodeLevel.get(0);
		AstNode nodeToMutate=(AstNode)domJsCodeLevel.get(1);

		
		if(typeOfcode.equals("jquery_c_arg")){
			ischanged=jqueryMethodChangeArg(nodeToMutate);
		}
		else if(typeOfcode.equals("jquery_r_dollar")){
			ischanged=jqueryRemoveDollarSign(nodeToMutate);
		}
		else if(typeOfcode.equals("jquery_c_selSign")){
			ischanged=jqueryChangeSelectorSign(nodeToMutate);
		}
		else if(typeOfcode.equals("js_c_id_tag")){
			ischanged=jsChangeGetElemByIdTag(nodeToMutate);
		}
		else if(typeOfcode.equals("js_s_arg")){
			ischanged=jsMethodSwapArgs(nodeToMutate);
		}
		else if(typeOfcode.equals("js_innerHTML_innerText")){
			ischanged=jsSwapInnerHtmlInnerText(nodeToMutate);
		}
		
		
		if(ischanged){
			result.append(scopeName+"\n"+"----------"+"\n");
			result.append(getFunctionName(nodeToMutate.getEnclosingFunction())+"\n"+"----------"+"\n");
			result.append(typeOfcode+"\n"+"----------"+"\n");
			result.append(nodeToMutate.toSource()+"\n");
			result.append("================"+"\n");
			writeResultsToFile(result.toString());
		}
		
		return ischanged;
	}
	
		
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
	private String getFunctionName(FunctionNode f) {
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
	
	
	
	private Block createBlockWithNode(AstNode node) {
		Block b = new Block();
		b.addChild(node);
		return b;
	}

				
	


	
	/**
	 * 
	 * public methods: Mutating the passed AST node
	 */
	
	
	/*
	 * function call
	 */
	
	public boolean functionCallChangeName(AstNode node){
		
		if(node==null)
			return false;
		FunctionCall funcCall=(FunctionCall) node;
		AstNode nameNode;
		if (funcCall.getTarget() instanceof PropertyGet ) {
		
			nameNode=((PropertyGet)funcCall.getTarget()).getRight();
			while(!(nameNode instanceof Name) && nameNode instanceof PropertyGet){
				nameNode=((PropertyGet)nameNode).getRight();
			}
			
		}
		else nameNode=funcCall.getTarget();
		String name=((Name) nameNode).getIdentifier();
		if(name.equals("$")) return false;
		String newName=name + "_changed";
		((Name) nameNode).setIdentifier(newName);
		return true;
		
		
	}
	
	
	public boolean functionCallSwapArg(AstNode node,AstNode target){
		TargetVarFinder targetVarFinder=new TargetVarFinder(target);
		
		List<AstNode> argList=((FunctionCall) node).getArguments();
		if (argList.size()<2)
			return false;
		for(AstNode arg:argList){
			targetVarFinder.setRootNode(arg);
			arg.visit(targetVarFinder);
			
		}
		AstNode targNode=targetVarFinder.getRandomTarget();
		if(targNode==null) return false;
		int firstIndex=argList.indexOf(targNode);
		int secondIndex=SystemProps.rnd.getNextRandomInt(argList.size());
		while(secondIndex==firstIndex)
			secondIndex=SystemProps.rnd.getNextRandomInt(argList.size());
		Collections.swap(argList, firstIndex, secondIndex);
//		((FunctionCall) node).setArguments(argList);
		return true;
		
		
	}
	
	public boolean functionCallRemoveArg(AstNode node,AstNode target){
		TargetVarFinder targetVarFinder=new TargetVarFinder(target);
		List<AstNode> argList=((FunctionCall) node).getArguments();
		if (argList.size()==0)
			return false;
		for(AstNode arg:argList){
			targetVarFinder.setRootNode(arg);
			arg.visit(targetVarFinder);
			
		}
		AstNode targNode=targetVarFinder.getRandomTarget();
		if(targNode==null) return false;
		int argindex=argList.indexOf(targNode);
		argList.remove(argindex);
//		((FunctionCall) node).setArguments(argList);
		return true;
		
	}
	


	public boolean functionCallChangeReplaceMethod(AstNode node){
		
		List<AstNode> argList=((FunctionCall) node).getArguments();	
		
		if (argList.get(0).toSource().contains("/g")){
			
			String oldcode=argList.get(0).toSource();
			String newcode=oldcode.replace("/gi","").replace("/g", "").replace("/", "");
			StringLiteral newArg=new StringLiteral();
			newArg.setValue(newcode);
			newArg.setQuoteCharacter('\"');
			argList.set(0, newArg);
			return true;
		}
		return false;
		
	}
	
	public boolean functionCallChangeParseIntMethod(AstNode node){
		
		List<AstNode> argList=((FunctionCall) node).getArguments();
		
		if (argList.size()<2)
			return false;
		//removing second arg which is the base for parseInt
		argList.remove(1);
		return true;
	}
	
	
	public boolean functionCallChangeSetTimeout(AstNode node){
		
		List<AstNode> argList=((FunctionCall) node).getArguments();
		AstNode firstArg=argList.get(0);
		
		if (firstArg instanceof StringLiteral){
			String arg=((StringLiteral)firstArg).getValue();
			if (arg.indexOf(")")-arg.indexOf("(")>=2){
				
				String funcArg=arg.substring(arg.indexOf("("), arg.indexOf(")")+1);
				((StringLiteral)firstArg).setValue(arg.replace(funcArg, ""));
				argList.set(0, firstArg);
	//			((FunctionCall) node.getParent()).setArguments(argList);
			}
		
		
			else
				if (arg.contains("()")){
				
					AstNode newArg=parse(arg.replace("()", ""));
					argList.set(0, (AstNode) newArg.getFirstChild());
//					((FunctionCall) node.getParent()).setArguments(argList);
				}
				else
					if (!arg.contains("(")){
						AstNode newArg=parse(arg+"()");
						ExpressionStatement exp=(ExpressionStatement) newArg.getFirstChild();
						argList.set(0, exp.getExpression());
	//					((FunctionCall) node.getParent()).setArguments(argList);
					}
			return true;
			
		}
		
		else
			if (firstArg instanceof Name){
				
				AstNode newArg=parse(firstArg.toSource()+"()");
				ExpressionStatement exp=(ExpressionStatement) newArg.getFirstChild();
				argList.set(0, exp.getExpression());
//				((FunctionCall) node.getParent()).setArguments(argList);
				return true;
							
			}
		return false;
		
		
	}
	
	
	/*
	 * function node
	 */
	
	public boolean functionNodeChangeName(AstNode node){
		if(node==null)
			return false;
		Name funcName=((FunctionNode) node).getFunctionName();
		if (funcName==null){
			Name funcname=new Name();
			funcname.setIdentifier("funcNameCreated");
			((FunctionNode) node).setFunctionName(funcname);
			
		}
		else{
			String newName=funcName.toSource()+"_changed";
			funcName.setIdentifier(newName);
			((FunctionNode) node).setFunctionName(funcName);
		}
		return true;
		
	}
	
	public boolean functionNodeSwapParam(AstNode node, AstNode target){
		TargetVarFinder targetVarFinder=new TargetVarFinder(target);
		List<AstNode> paramList=((FunctionNode) node).getParams();
		if (paramList==null || paramList.size()<2)
			return false;
		for(AstNode param:paramList){
			targetVarFinder.setRootNode(param);
			param.visit(targetVarFinder);
			
		}
		AstNode targNode=targetVarFinder.getRandomTarget();
		if(targNode==null) return false;
		int firstIndex=paramList.indexOf(targNode);
		int secondIndex=SystemProps.rnd.getNextRandomInt(paramList.size());
		while(firstIndex==secondIndex)
			secondIndex=SystemProps.rnd.getNextRandomInt(paramList.size());

		Collections.swap(paramList, firstIndex, secondIndex);
		return true;
		
	}
	
	public boolean functionNodeRemoveParam(AstNode node,AstNode target){
		TargetVarFinder targetVarFinder=new TargetVarFinder(target);
		List<AstNode> paramList=((FunctionNode) node).getParams();
		if (paramList==null || paramList.size()==0)
			return false;
		for(AstNode param:paramList){
			targetVarFinder.setRootNode(param);
			param.visit(targetVarFinder);
			
		}
		AstNode targNode=targetVarFinder.getRandomTarget();
		if(targNode==null) return false;
		int paramindex=paramList.indexOf(targNode);
		paramList.remove(paramindex);
//		((FunctionNode) node).setParams(paramList);
		return true;
		
	}
	
	
	/*
	 * if statement
	 */
	
	public boolean ifStatementChangeConst(AstNode node){
		List<NumberLiteral> numbersInConditionStatm=new ArrayList<NumberLiteral>();
		List<NumberLiteral> numList=searchConditionForNumbers(node,numbersInConditionStatm);
		if (numList.size()==0)
			return false;
		NumberLiteral numNode=numList.get(SystemProps.rnd.getNextRandomInt(numList.size()));
		double newNumber=numNode.getNumber()+(double)SystemProps.rnd.getNextRandomInt(10)+1;
		numNode.setNumber(newNumber);
		numNode.setValue(String.valueOf(newNumber));
		return true;

		
	}
	
	
	public boolean ifStatementChangeName(AstNode node){
		ArrayList<Name> namesInConditionStatm=new ArrayList<Name>();
		List<Name> nameList=searchConditionForNames(node,namesInConditionStatm);
		if (nameList.size()==0)
			return false;
		Name nameNode=nameList.get(SystemProps.rnd.getNextRandomInt(nameList.size()));
		String newName=nameNode.getIdentifier()+"_changed";
		nameNode.setIdentifier(newName);
		return true;

		
	}
	
	public boolean ifStatementChangeRelOps(AstNode node){
		List<InfixExpression> relOpsInConditionStatm=new ArrayList<InfixExpression>();
		List<InfixExpression> infixList=searchConditionForRelOps(node,relOpsInConditionStatm);
		if (infixList.size()==0)
			return false;
		InfixExpression infixNode=infixList.get(SystemProps.rnd.getNextRandomInt(infixList.size()));
		int randOp=jsCodeOperator.getRandomRelOps();
		while(randOp==infixNode.getOperator()){
			randOp=jsCodeOperator.getRandomRelOps();
		}
		infixNode.setOperator(randOp);
		return true;
		
	}
	
	public boolean ifStatementChangeArithOps(AstNode node){
			List<InfixExpression> arithOpsInConditionStatm=new ArrayList<InfixExpression>();
			List<InfixExpression> infixList=searchConditionForArithOps(node,arithOpsInConditionStatm);
			if (infixList.size()==0)
				return false;
			InfixExpression infixNode=infixList.get(SystemProps.rnd.getNextRandomInt(infixList.size()));
			int opr=infixNode.getOperator();
				
			switch(opr){
			
				case Token.ADD: opr=Token.SUB;
				break;
			
				case Token.SUB: opr=Token.ADD;
				break;
				
				case Token.MUL: opr=Token.DIV;
				break;
				
				case Token.DIV: opr=Token.MUL;
				break;
				
				default: return false; 
					
										
			}
			infixNode.setOperator(opr);
			return true;
	}
	
	public boolean ifStatementChangeLogicOps(AstNode node){
		List<InfixExpression> logicOpsInConditionStatm=new ArrayList<InfixExpression>();
		List<InfixExpression> infixList=searchConditionForLogicOps(node,logicOpsInConditionStatm);
		if (infixList.size()==0)
			return false;
		InfixExpression infixNode=infixList.get(SystemProps.rnd.getNextRandomInt(infixList.size()));
		int randOp=jsCodeOperator.getRandomLogicOps();
		while(randOp==infixNode.getOperator()){
			randOp=jsCodeOperator.getRandomLogicOps();
		}
		infixNode.setOperator(randOp);
		return true;
	}
	
	
	public boolean ifStatementRemoveElsePart(AstNode node){
		
		if (((IfStatement) node).getElsePart()==null)
			return false;
		((IfStatement) node).setElsePart(null);
		return true;
	}
	
	public boolean ifStatementSwapNullUndef(AstNode node){
		ArrayList<KeywordLiteral> keyLiteralsInConditionStatm=new ArrayList<KeywordLiteral>();
		List<KeywordLiteral> keyLitList=searchConditionForKeyLiterals(node,keyLiteralsInConditionStatm);
		List<StringLiteral> strList=new ArrayList<StringLiteral>();
		strList=searchConditionForUndefOrNullString(node,strList);
		if (keyLitList.size()==0 && strList.size()==0)
			return false;
		if(keyLitList.size()!=0){
			List<KeywordLiteral> nullList=getKeyLiteralNodes(Token.NULL, keyLitList);
			if (nullList.size()==0)
				return false;
			KeywordLiteral nullNode=nullList.get(SystemProps.rnd.getNextRandomInt(nullList.size()));
			Name undefNode=new Name();
			undefNode.setLineno(nullNode.getLineno());
			undefNode.setIdentifier("undefined");
			if (((InfixExpression)nullNode.getParent()).getRight().equals(nullNode)){
				((InfixExpression)nullNode.getParent()).setRight(undefNode);
			}
			else
				((InfixExpression)nullNode.getParent()).setLeft(undefNode);
			return true;
		}
		else{
	
			StringLiteral nullStrNode=strList.get(SystemProps.rnd.getNextRandomInt(strList.size()));
			nullStrNode.setValue("undefined");
/*			StringLiteral undefNode=new StringLiteral();
			undefNode.setLineno(nullStrNode.getLineno());
			undefNode.setValue("undefined");
			undefNode.setQuoteCharacter('"');
			if (((InfixExpression)nullStrNode.getParent()).getRight().equals(nullStrNode)){
				((InfixExpression)nullStrNode.getParent()).setRight(undefNode);
			}
			else
				((InfixExpression)nullStrNode.getParent()).setLeft(undefNode);
*/			return true;
			
		}
	}
	
	
	public boolean ifStatementSwapUndefNull(AstNode node){
		String identifier="undefined";
		ArrayList<Name> namesInConditionStatm=new ArrayList<Name>();
		List<Name> nameList=searchConditionForNames(node,namesInConditionStatm);
		List<StringLiteral> strList=new ArrayList<StringLiteral>();
		strList=searchConditionForUndefOrNullString(node,strList);
		List<Name> undefList=new ArrayList<Name>();
		for (int i=0;i<nameList.size();i++){
			if (identifier.equals(nameList.get(i).getIdentifier())){
				undefList.add(nameList.get(i));
			}
		}
		if (undefList.size()==0 && strList.size()==0)
			return false;
		if(undefList.size()!=0){
			Name undefNode=undefList.get(SystemProps.rnd.getNextRandomInt(undefList.size()));
			undefNode.setIdentifier("null");
/*			KeywordLiteral nullNode=new KeywordLiteral();
			nullNode.setLineno(undefNode.getLineno());
			nullNode.setType(Token.NULL);
			if (((InfixExpression)undefNode.getParent()).getRight().equals(undefNode)){
				((InfixExpression)undefNode.getParent()).setRight(nullNode);
			}
			else
				((InfixExpression)undefNode.getParent()).setLeft(nullNode);
*/			return true;
		}
		else{
			
			StringLiteral undefStrNode=strList.get(SystemProps.rnd.getNextRandomInt(strList.size()));
			undefStrNode.setValue("null");
/*			StringLiteral nullNode=new StringLiteral();
			nullNode.setLineno(undefStrNode.getLineno());
			nullNode.setValue("null");
			nullNode.setQuoteCharacter('"');
			if (((InfixExpression)undefStrNode.getParent()).getRight().equals(undefStrNode)){
				((InfixExpression)undefStrNode.getParent()).setRight(nullNode);
			}
			else
				((InfixExpression)undefStrNode.getParent()).setLeft(nullNode);
*/			return true;
			
		}
	}
	
	public boolean ifStatementSwapTrueFalse(AstNode node){
		ArrayList<KeywordLiteral> keyLiteralsInConditionStatm=new ArrayList<KeywordLiteral>();
		List<KeywordLiteral> keyLitList=searchConditionForKeyLiterals(node,keyLiteralsInConditionStatm);
		List<KeywordLiteral> boolList=new ArrayList<KeywordLiteral>();
		for (int i=0;i<keyLitList.size();i++){
			if (keyLitList.get(i).getType()==Token.TRUE || keyLitList.get(i).getType()==Token.FALSE){
			
				boolList.add(keyLitList.get(i));
			
			}
		}
		if (boolList.size()==0){
			ArrayList<UnaryExpression> unaryExpInConditionStatm=new ArrayList<UnaryExpression>();
			ArrayList<UnaryExpression> unrList=new ArrayList<UnaryExpression>();
			unaryExpInConditionStatm=(ArrayList<UnaryExpression>) searchNodeForUnaryExp(node, unaryExpInConditionStatm);
			for (int i=0;i<unaryExpInConditionStatm.size();i++){
				if (unaryExpInConditionStatm.get(i).getOperator()==Token.NOT){
				
					unrList.add(unaryExpInConditionStatm.get(i));
				
				}
			}
			if(unrList.size()==0) 
				return false;
			UnaryExpression unrNode=unrList.get(SystemProps.rnd.getNextRandomInt(unrList.size()));
			String unr=unrNode.toSource();
			String newUnr=unr.replace("!", "");
			IfStatement ifstatement=(IfStatement) node.getParent();
			String condSource=ifstatement.getCondition().toSource();
			AstNode newCond=parse(condSource.replace(unr, newUnr));
			ExpressionStatement expNode=(ExpressionStatement) newCond.getFirstChild();
			ifstatement.setCondition(expNode.getExpression());
			return true;
			
		}
			
		KeywordLiteral boolNode=boolList.get(SystemProps.rnd.getNextRandomInt(boolList.size()));
		if (boolNode.getType()==Token.TRUE)
			boolNode.setType(Token.FALSE);
		else
			boolNode.setType(Token.TRUE);
		return true;
		
	}
	
	public boolean ifStatementRemoveElseKeyword(AstNode node){
	
		IfStatement ifNode=(IfStatement) node;
		if (ifNode.getElsePart()==null)
			return false;
		makeSureBlockExistsAround(ifNode);
		
		String withoutElse=ifNode.getElsePart().toSource().replaceFirst("\\{", "");
		String tempWithoutElse=withoutElse.replace("\n", "").replace("\r", "");
		withoutElse=tempWithoutElse.substring(0, tempWithoutElse.length()-1);
		ifNode.setElsePart(null);
		Parser p = new Parser(compilerEnvirons, null);
		AstNode newNodeWithoutElse=p.parse(withoutElse, null, 0);
		if(newNodeWithoutElse.getFirstChild() instanceof ExpressionStatement){
			ExpressionStatement exp=(ExpressionStatement) newNodeWithoutElse.getFirstChild();
			ifNode.getParent().addChildAfter(exp.getExpression(), ifNode);
			return true;
		}
		else{
			ifNode.getParent().addChildAfter(newNodeWithoutElse.getFirstChild(), ifNode);
			return true;
		}
//		return false;
	
	}
	

	
	public boolean ifStatementSwapNumBoolean(AstNode node){
		List<InfixExpression> relOpsInConditionStatm=new ArrayList<InfixExpression>();
		List<InfixExpression> equalityNodeList=getDesiredInfixNodes(Token.EQ, searchConditionForRelOps(node,relOpsInConditionStatm));
		List<InfixExpression> numNodeList=new ArrayList<InfixExpression> ();
		for (int i=0;i<equalityNodeList.size();i++){
			if (equalityNodeList.get(i).getRight() instanceof NumberLiteral)
				if (((NumberLiteral)equalityNodeList.get(i).getRight()).getNumber()==1
						||((NumberLiteral)equalityNodeList.get(i).getRight()).getNumber()==0 ){
					numNodeList.add(equalityNodeList.get(i));
				}
		}
		if (numNodeList.size()==0)
			return false;
		InfixExpression numInfixNode=numNodeList.get(SystemProps.rnd.getNextRandomInt(numNodeList.size()));
		NumberLiteral numNode=(NumberLiteral) numInfixNode.getRight();
				
		KeywordLiteral booleanNode=new KeywordLiteral();
		booleanNode.setLineno(numNode.getLineno());
		if (numNode.getNumber()==1){
			booleanNode.setType(Token.TRUE);
			numInfixNode.setRight(booleanNode);
		}
		else{
			if (numNode.getNumber()==0){
				booleanNode.setType(Token.FALSE);
				numInfixNode.setRight(booleanNode);
			}
			else return false;
		}
		return true;
						
				
		
	}
	
	
	public boolean ifStatementSwapBooleanNum(AstNode node){
		
		List<InfixExpression> relOpsInConditionStatm=new ArrayList<InfixExpression>();
		List<InfixExpression> equalityNodeList=getDesiredInfixNodes(Token.EQ, searchConditionForRelOps(node,relOpsInConditionStatm));
		List<InfixExpression> boolNodeList=new ArrayList<InfixExpression> ();
		for (int i=0;i<equalityNodeList.size();i++){
			if (equalityNodeList.get(i).getRight() instanceof KeywordLiteral)
				if (((KeywordLiteral)equalityNodeList.get(i).getRight()).getType()==Token.TRUE
						||((KeywordLiteral)equalityNodeList.get(i).getRight()).getType()==Token.FALSE ){
					boolNodeList.add(equalityNodeList.get(i));
				}
		}
		if (boolNodeList.size()==0)
			return false;
		InfixExpression boolInfixNode=boolNodeList.get(SystemProps.rnd.getNextRandomInt(boolNodeList.size()));
		
		KeywordLiteral boolNode=(KeywordLiteral) boolInfixNode.getRight();			
				
		NumberLiteral numNode=new NumberLiteral();
		numNode.setLineno(node.getLineno());
		if (boolNode.getType()==Token.TRUE){
				
			numNode.setDouble(1);
			numNode.setValue("1");
			boolInfixNode.setRight(numNode);
		}
		else{
			if (boolNode.getType()==Token.FALSE){
						
				numNode.setDouble(0);
				numNode.setValue("0");
				boolInfixNode.setRight(numNode);
			}
			else return false;
		}
		return true;
						
						
			
	}

	
	public boolean ifStatementSwapTrueBoolPattern(AstNode node){
		List<InfixExpression> relOpsInConditionStatm1=new ArrayList<InfixExpression>();
		List<InfixExpression> equalityNodeList=getDesiredInfixNodes(46, searchConditionForRelOps(node,relOpsInConditionStatm1));
		List<InfixExpression> relOpsInConditionStatm2=new ArrayList<InfixExpression>();
		equalityNodeList.addAll(getDesiredInfixNodes(47, searchConditionForRelOps(node,relOpsInConditionStatm2)));
		List<InfixExpression> boolNodeList=new ArrayList<InfixExpression> ();
		for (int i=0;i<equalityNodeList.size();i++){
			if (equalityNodeList.get(i).getRight() instanceof KeywordLiteral)
				if ( (equalityNodeList.get(i).getOperator()==46 && ((KeywordLiteral)equalityNodeList.get(i).getRight()).getType()==Token.TRUE)
						||(equalityNodeList.get(i).getOperator()==47 && ((KeywordLiteral)equalityNodeList.get(i).getRight()).getType()==Token.FALSE) )
				{
					 
					boolNodeList.add(equalityNodeList.get(i));
				}
		}
		if (boolNodeList.size()==0)
			return false;
		InfixExpression trueInfixNode=boolNodeList.get(SystemProps.rnd.getNextRandomInt(boolNodeList.size()));
	//	KeywordLiteral trueNode=(KeywordLiteral) trueInfixNode.getRight();	
		Name newNode=new Name();
		newNode.setLineno(node.getLineno());
		newNode.setIdentifier(trueInfixNode.getLeft().toSource());
		String newCondition=node.toSource().replace(trueInfixNode.toSource(), newNode.toSource());
		AstNode newConditionNode=parse(newCondition);
		ExpressionStatement exp=(ExpressionStatement) newConditionNode.getFirstChild();
		((IfStatement)node.getParent()).setCondition(exp.getExpression());
		return true;
		
	}
	
	
	public boolean ifStatementSwapFalseBoolPattern(AstNode node){
		
		List<InfixExpression> relOpsInConditionStatm1=new ArrayList<InfixExpression>();
		List<InfixExpression> equalityNodeList=getDesiredInfixNodes(46, searchConditionForRelOps(node,relOpsInConditionStatm1));
		List<InfixExpression> relOpsInConditionStatm2=new ArrayList<InfixExpression>();
		equalityNodeList.addAll(getDesiredInfixNodes(47, searchConditionForRelOps(node,relOpsInConditionStatm2)));
		List<InfixExpression> boolNodeList=new ArrayList<InfixExpression> ();
		for (int i=0;i<equalityNodeList.size();i++){
			if (equalityNodeList.get(i).getRight() instanceof KeywordLiteral)
				if ( (equalityNodeList.get(i).getOperator()==46 && ((KeywordLiteral)equalityNodeList.get(i).getRight()).getType()==Token.FALSE)
						||(equalityNodeList.get(i).getOperator()==47 && ((KeywordLiteral)equalityNodeList.get(i).getRight()).getType()==Token.TRUE) )
				{
					 
					boolNodeList.add(equalityNodeList.get(i));
				}
		}
		if (boolNodeList.size()==0)
			return false;
		InfixExpression falseInfixNode=boolNodeList.get(SystemProps.rnd.getNextRandomInt(boolNodeList.size()));
		
		UnaryExpression newNode=new UnaryExpression();
		newNode.setLineno(node.getLineno());
		newNode.setType(Token.NOT);
		Name operand=new Name();
		operand.setIdentifier(falseInfixNode.getLeft().toSource());
		newNode.setOperand(operand);
		String newCondition=node.toSource().replace(falseInfixNode.toSource(), newNode.toSource());
		AstNode newConditionNode=parse(newCondition);
		ExpressionStatement exp=(ExpressionStatement) newConditionNode.getFirstChild();
		((IfStatement)node.getParent()).setCondition(exp.getExpression());
		return true;
	}
	

	public boolean switchCaseSwapCase(AstNode node){
		
		SwitchStatement switchCaseNode=(SwitchStatement) node;
		List<SwitchCase> caseList=switchCaseNode.getCases();
		if (caseList.size()<2)
			return false;
		int firstRandElm=SystemProps.rnd.getNextRandomInt(caseList.size());
		int secRandElm=SystemProps.rnd.getNextRandomInt(caseList.size());
		while(firstRandElm==secRandElm)
			secRandElm=SystemProps.rnd.getNextRandomInt(caseList.size());
		
		AstNode firstExp=caseList.get(firstRandElm).getExpression();
		AstNode secExp=caseList.get(secRandElm).getExpression();
		caseList.get(firstRandElm).setExpression(secExp);
		caseList.get(secRandElm).setExpression(firstExp);
		
		return true;
		
		
	}
	
	/*
	 * variables (assignment...)
	 */
	
	public boolean removeVarDeclaration(AstNode node,AstNode target){
		TargetVarFinder targetVarFinder=new TargetVarFinder(target);
		AstNode parent=node.getParent();
		if (parent instanceof ForLoop){
			
			if (((ForLoop) parent).getInitializer()!=null 
					&& ((ForLoop)parent).getInitializer().equals(node)){
				AstNode emptyNode=parse("");
				((ForLoop)parent).setInitializer(emptyNode);
			}
			else return false;

			
		}
		else{
			if (node instanceof ExpressionStatement){
		
				ExpressionStatement varNode=(ExpressionStatement) node;
				int varSize=((VariableDeclaration)varNode.getExpression()).getVariables().size();
				if (varSize>1){
			
					List <VariableInitializer> varInitList=((VariableDeclaration)varNode.getExpression()).getVariables();
					for(VariableInitializer varinit:varInitList){
						targetVarFinder.setRootNode(varinit);
						varinit.visit(targetVarFinder);
					}
					int index=varInitList.indexOf(targetVarFinder.getRandomTarget());
					varInitList.remove(index);
		
		
				}
				else if(varSize==1)
				{
					
					makeSureBlockExistsAround(varNode);
					parent.removeChild(varNode);
		
		
				}
			}
			else return false;
		}
		return true;
		

		
	}
	
	public boolean removeAssignment(AstNode node){
/*		if(node instanceof ExpressionStatement)
			node=((ExpressionStatement)node).getExpression();
		Assignment assignNode=(Assignment) node;
*/		makeSureBlockExistsAround(node);
		AstNode parent=node.getParent();
	
		if (parent instanceof ForLoop){
			
			if (((ForLoop) parent).getInitializer()!=null 
					&& ((ForLoop)parent).getInitializer().equals(node)){
				AstNode emptyNode=parse("");
				((ForLoop)parent).setInitializer(emptyNode);
				return true;
			}
			else{
				if (((ForLoop)parent).getIncrement()!=null && 
						((ForLoop)parent).getIncrement().equals(node)){
					
					AstNode emptyNode=parse("");
					((ForLoop)parent).setIncrement(emptyNode);
					return true;
				}
			
			}

			
		}
		else{
			if(parent.hasChildren()){
				if (parent.getFirstChild().equals(node) || parent.getLastChild().equals(node)){
					parent.removeChild(node);
					return true;
					
				}
				
			}
		
		}
		return false;
					
		
		
	}
	
	
	private boolean changeAssignedNumber(AstNode node){
		if (node instanceof Assignment){
			Assignment assignNode=(Assignment) node;
			List<NumberLiteral> numbersInAssignment=new ArrayList<NumberLiteral>();
			List<NumberLiteral> numList=searchAssignmentForNumbers(assignNode.getRight(),numbersInAssignment);
			if (numList.size()==0)
				return false;
			NumberLiteral numNode=numList.get(SystemProps.rnd.getNextRandomInt(numList.size()));
			double newValue=numNode.getDouble()+SystemProps.rnd.getNextRandomInt(10)+1;
			numNode.setDouble(newValue);
			numNode.setValue(String.valueOf(newValue));
			return true;
		}
		return false;
	}
			/*
		if (assignNode.getRight() instanceof NumberLiteral){
			double newValue=assignNode.getRight().getDouble()+SystemProps.rnd.getNextRandomInt(10)+1;
			((NumberLiteral)assignNode.getRight()).setDouble(newValue);
			((NumberLiteral)assignNode.getRight()).setValue(String.valueOf(newValue));
		}
		*/
	
	private boolean changeAssignedString(AstNode node){
		
		if (node instanceof Assignment){
			Assignment assignNode=(Assignment) node;
			List<StringLiteral> stringsInAssignment=new ArrayList<StringLiteral>();
			List<StringLiteral> strList=searchAssignmentForStrings(assignNode.getRight(),stringsInAssignment);
			if (strList.size()==0)
				return false;
			StringLiteral strNode=strList.get(SystemProps.rnd.getNextRandomInt(strList.size()));
			String newString=strNode.getValue()+"_changed";
			strNode.setValue(newString);
			return true;
		}
		return false;
		
	}
	
	private boolean changeAssignedArrayElem(AstNode node){
		if (node instanceof Assignment){
			Assignment assignNode=(Assignment) node;
			List<ElementGet> arrays=new ArrayList<ElementGet>();
			List<ElementGet> arrayList=searchAssignmentForArrayElem(assignNode.getRight(),arrays);
			if (arrayList.size()==0)
				return false;
			ElementGet arrayNode=arrayList.get(SystemProps.rnd.getNextRandomInt(arrayList.size()));
	
			if (arrayNode.getElement() instanceof NumberLiteral){
				
				double newIndex=Double.parseDouble(((NumberLiteral)arrayNode.getElement()).getValue())+1;			
				((NumberLiteral)arrayNode.getElement()).setValue(String.valueOf(newIndex));
			}
			
			if (arrayNode.getElement() instanceof Name){
				
				String newName=((Name)arrayNode.getElement()).getIdentifier()+"_changed";			
				((Name)arrayNode.getElement()).setIdentifier(newName);
				
			}
	
			
		}
		return true;
		
	}
	
	private boolean changeAssignedName(AstNode node){
		if (node instanceof Assignment){
			Assignment assignNode=(Assignment) node;
			List<Name> namesInAssignment=new ArrayList<Name>();
			List<Name> nameList=searchAssignmentForNames(assignNode.getRight(),namesInAssignment);
			if (nameList.size()==0)
				return false;
			Name nameNode=nameList.get(SystemProps.rnd.getNextRandomInt(nameList.size()));
			String newName=nameNode.getIdentifier()+"_changed";
			nameNode.setIdentifier(newName);
			return true;
		}
		return false;
		
	}
	
	
	public boolean changeAssignedValue(AstNode node){
		if(node instanceof ExpressionStatement)
			node=((ExpressionStatement)node).getExpression();
		if(node instanceof Assignment)
			if(((Assignment)node).getRight() instanceof FunctionNode){
				return functionNodeChangeName(((Assignment)node).getRight());
				
			}
		int randFunc;
		boolean changed=false;
		ArrayList<Integer> rand=new ArrayList<Integer>(4);
		rand.addAll(new ArrayList<Integer> (Arrays.asList(-1,-1,-1,-1)));
	
		while (!changed && rand.contains(-1)){
			randFunc=SystemProps.rnd.getNextRandomInt(4);
			switch(randFunc){
			case 0:	changed=changeAssignedNumber(node);
			break;
			
			case 1:	changed=changeAssignedName(node);
			break;	
			
			case 2:	changed=changeAssignedString(node);
			break;
				
			default:changed=changeAssignedArrayElem(node);
			break;
			
			}
			rand.set(randFunc, 0);
			
		}
		return changed;
		
	}
	
	public boolean addVarKeyword(AstNode node){
		
	//	Assignment assignNode=(Assignment) node;
		AstNode parent=makeSureBlockExistsAround(node);
	//	AstNode parent=assignSt.getParent();
		if (parent instanceof ForLoop){
			
			if (((ForLoop) parent).getInitializer()!=null 
					&& ((ForLoop)parent).getInitializer().equals(node)){
				String varSource="var "+((ForLoop)parent).getInitializer().toSource();
				AstNode varNode=parse(varSource);
				ExpressionStatement exp=(ExpressionStatement) varNode.getFirstChild();
				((ForLoop)parent).setInitializer(exp.getExpression());
				return true;
			}
		}
		else
			if(parent.hasChildren())
				if (parent.getFirstChild().equals(node) || parent.getLastChild().equals(node)){
					String varSource="var "+node.toSource();
					AstNode varNode=parse(varSource);
					parent.replaceChild(node, varNode.getFirstChild());
					return true;
				}
		return false;
			
		
		
	}
	
	// the initializer is not null in this case (**the type of the node is still VariableDeclaration not Assignment)
	// we need to check to make sure that the initialize value is not null otherwise removing var does not make sense
	
	public boolean removeVarKeyword(AstNode node){
		
		
	//	AstNode parent=node.getParent();
		AstNode parent=makeSureBlockExistsAround(node);
		if (parent instanceof ForLoop){
			
			if (((ForLoop) parent).getInitializer()!=null 
					&& ((ForLoop)parent).getInitializer().equals(node)){
				if(!node.toSource().contains("var ")) return false;
				AstNode newNode=parse(node.toSource().replace("var", ""));
				ExpressionStatement exp=(ExpressionStatement) newNode.getFirstChild();
				((ForLoop)parent).setInitializer(exp.getExpression());
			}

			
		}
		
		else{
			if (node instanceof ExpressionStatement){
				ExpressionStatement varNode=(ExpressionStatement) node;
				List <VariableInitializer> varInitList=((VariableDeclaration)varNode.getExpression()).getVariables();
				VariableInitializer desiredVarInit=null;
				for (VariableInitializer varinit:varInitList){
			
					if (varinit.getInitializer()!=null){
						desiredVarInit=varinit;
						break;
					}
			
				}
				if (desiredVarInit==null)
					return false;
		
				else{
			
					AstNode newNode=parse(desiredVarInit.toSource().replace("var", ""));
					parent.addChildAfter(newNode.getFirstChild(), varNode);
					parent.removeChild(varNode);
			
				}
			}
			else return false;
		}
		return true;
		
	}
	
	
	public boolean changeVariableTypeNumbStr(AstNode node){
		Assignment assignNode;
		if(node instanceof ExpressionStatement){
			ExpressionStatement expNode=(ExpressionStatement) node;
			assignNode=(Assignment)expNode.getExpression();
		}
		else if(node instanceof Assignment)
			assignNode=(Assignment) node;
		else return false;
		if (assignNode.getRight() instanceof StringLiteral){
			
			NumberLiteral numberNode=new NumberLiteral();
			numberNode.setLineno(node.getLineno());
			int num=SystemProps.rnd.getNextRandomInt(10);
			numberNode.setNumber(num);
			numberNode.setValue(String.valueOf(num));
			assignNode.setRight(numberNode);
		}
		
		else{
			if (assignNode.getRight() instanceof NumberLiteral){
				
				StringLiteral stringNode=new StringLiteral();
				stringNode.setLineno(node.getLineno());
				stringNode.setValue("_changed");
				stringNode.setQuoteCharacter('"');
				assignNode.setRight(stringNode);
				
			}
		else 
			if(assignNode.getRight() instanceof Name){
				Name name=(Name) assignNode.getRight();
				InfixExpression infix=new InfixExpression();
				infix.setOperator(Token.ADD);
				infix.setLeft(name);
				StringLiteral str=new StringLiteral();
				str.setValue("");
				str.setQuoteCharacter('"');
				infix.setRight(str);
				assignNode.setRight(infix);
			
			}
			else return false;
		}
		return true;
	}
	
	public boolean changeAssignmentArithOps(AstNode node){
		if(node instanceof ExpressionStatement)
			node=((ExpressionStatement)node).getExpression();
		if (node instanceof Assignment){
			
			Assignment assignNode=(Assignment) node;
			if(assignNode.getRight() instanceof FunctionNode)
				return false;
			List<InfixExpression> arithOpsInAssignStatm=new ArrayList<InfixExpression>();
			List<InfixExpression> infixList=searchAssignmentForArithOps(assignNode,arithOpsInAssignStatm);
			if (infixList.size()==0)
				return false;
			InfixExpression infixNode=infixList.get(SystemProps.rnd.getNextRandomInt(infixList.size()));
						
			int opr=infixNode.getOperator();
				
			switch(opr){
			
				case Token.ADD: opr=Token.SUB;
				break;
			
				case Token.SUB: opr=Token.ADD;
				break;
				
				case Token.MUL: opr=Token.DIV;
				break;
				
				case Token.DIV: opr=Token.MUL;
				break;
				
				case Token.ASSIGN_ADD: opr=Token.ASSIGN_SUB;
				break;
				
				case Token.ASSIGN_SUB: opr=Token.ASSIGN_ADD;
				break;
				
				case Token.ASSIGN_MUL: opr=Token.ASSIGN_DIV;
				break;
				
				case Token.ASSIGN_DIV: opr=Token.ASSIGN_MUL;
				break;
				
				default: return false;
					
			}
			infixNode.setOperator(opr);
				
			
		}
		else{
			if (node instanceof UnaryExpression){
				
				changeUnaryExpOperatorType(node);
			}
			else return false;
		}
		return true;
			
		
	}
	
	//////////////////
	
	/*
	 * for loop
	 */
	
	public boolean changeForLoopInitPart(AstNode forNode){
		
		AstNode node=((ForLoop) forNode).getInitializer();
		if (node==null)
			return false;
		
		List<String> randOps=new ArrayList<String>();
		randOps.add("r_assign");
		randOps.add("c_value");
		
		if (node instanceof Assignment){			
//			while (oldSource.equals(node.toSource())){
			String randOp=randOps.get(SystemProps.rnd.getNextRandomInt(randOps.size()));
			if (randOp=="r_assign")
				return removeAssignment(node);
			else 
				return changeAssignedValue(node);
//			}
		}
		else if (node instanceof VariableDeclaration)
			return removeVarKeyword(node);
						
		return false;		
		
	}
	
	public boolean changeForLoopIncPartOprType(AstNode forNode){
		
		AstNode node=((ForLoop) forNode).getIncrement();
		if (node==null)
			return false;
		String oldSource=node.toSource();
		
		
		if (node instanceof Assignment)
			changeAssignmentArithOps(node);
		
		else{
			ArrayList<UnaryExpression> unaryExpInConditionStatm=new ArrayList<UnaryExpression>();
			List<UnaryExpression> unrList=searchNodeForUnaryExp(node,unaryExpInConditionStatm);


			if(unrList.size()>0){
				UnaryExpression unrExpNode=unrList.get(SystemProps.rnd.getNextRandomInt(unrList.size()));
				changeUnaryExpOperatorType(unrExpNode);
			}


		}
		String newSource=node.toSource();
		if(oldSource.equals(newSource))
			return false;
		
		return true;
	}
	
	public boolean changeForLoopIncPartUnrOprPosition(AstNode forNode){
		
		AstNode node=((ForLoop) forNode).getIncrement();
		if (node==null)
			return false;
		String oldSource=node.toSource();
	/*	if (node instanceof UnaryExpression){
			
			//only cares about changing when it is like ++x: change ++x to x++
			if (((UnaryExpression)node).isPrefix()){
				changeUnaryExpOperatorPosition(node);
			
			}
			else return false;
		}
	*/	//else if (node instanceof InfixExpression){
		ArrayList<UnaryExpression> unaryExpInConditionStatm=new ArrayList<UnaryExpression>();
		List<UnaryExpression> unrList=searchNodeForUnaryExp(node,unaryExpInConditionStatm);
		List<UnaryExpression> prefixUnrExpList=new ArrayList<UnaryExpression>();
		for (UnaryExpression unr:unrList){
			if (unr.isPrefix())
				prefixUnrExpList.add(unr);
		}
			
		if(prefixUnrExpList.size()>0){
			UnaryExpression prefixUnrNode=prefixUnrExpList.get(SystemProps.rnd.getNextRandomInt(prefixUnrExpList.size()));
			changeUnaryExpOperatorPosition(prefixUnrNode);
		}
//		}
		String newSource=node.toSource();
		if(oldSource.equals(newSource))
			return false;
		
		return true;
	}
	
	
	/*
	 * the node is the condition part of the for loop
	 */
	
	public boolean changeForLoopNumInCndn(AstNode forNode){
		
		AstNode node=((ForLoop) forNode).getCondition();
		return ifStatementChangeConst(node);
	
	}
	
	public boolean changeForLoopNameInCndn(AstNode forNode){
		
		AstNode node=((ForLoop) forNode).getCondition();
		return ifStatementChangeName(node);	
	}
	

	
	public boolean changeForLoopRelOpsInCndn(AstNode forNode){
		
		AstNode node=((ForLoop) forNode).getCondition();
		return ifStatementChangeRelOps(node);	
	}
	
	public boolean changeForLoopLogOpsInCndn(AstNode forNode){
		
		AstNode node=((ForLoop) forNode).getCondition();
		return ifStatementChangeLogicOps(node);	
	}
	
	public boolean changeForLoopArithOpsInCndn(AstNode forNode){
		
		AstNode node=((ForLoop) forNode).getCondition();
		return ifStatementChangeArithOps(node);	
	}
	

	
	public boolean changeConsecForLoopsOrder(ForLoop forLoopNode){
		
		List<ForLoop> forList=new ArrayList<ForLoop>();
		forList.add(forLoopNode);
		List<ForLoop> consecFors=new ArrayList<ForLoop>();
		consecFors=searchforConsecForLoops((Scope)forLoopNode,consecFors);
		if(consecFors==null) return false;
		if (consecFors.size()>0){
			
			forList.addAll(1, consecFors);
			int randIndex=SystemProps.rnd.getNextRandomInt(forList.size()-1);
			ForLoop firstForLoop=forList.get(randIndex);
			ForLoop secondForLoop=forList.get(randIndex+1);
			AstNode secForInit=secondForLoop.getInitializer();
			AstNode secForCndn=secondForLoop.getCondition();
			AstNode secForInc=secondForLoop.getIncrement();
			secondForLoop.setInitializer(firstForLoop.getInitializer());
			secondForLoop.setCondition(firstForLoop.getCondition());
			secondForLoop.setIncrement(firstForLoop.getIncrement());
			firstForLoop.setInitializer(secForInit);
			firstForLoop.setCondition(secForCndn);
			firstForLoop.setIncrement(secForInc);
			return true;
		}
		
		return false;
			
		
		
	}
	

	
	
	/*
	 * while loop
	 */
	
	
	public boolean changeWhileLoopNum(AstNode whileNode){
		
		AstNode node=((WhileLoop) whileNode).getCondition();
		return ifStatementChangeConst(node);	
	}
	
	public boolean changeWhileLoopName(AstNode whileNode){
		
		AstNode node=((WhileLoop) whileNode).getCondition();
		return ifStatementChangeName(node);	
	}
	
	
	public boolean changeWhileLoopRelOpsInCndn(AstNode whileNode){
		
		AstNode node=((WhileLoop) whileNode).getCondition();
		return ifStatementChangeRelOps(node);	
	}
	
	public boolean changeWhileLoopLogOpsInCndn(AstNode whileNode){
		
		AstNode node=((WhileLoop) whileNode).getCondition();
		return ifStatementChangeLogicOps(node);	
	}
	
	public boolean changeWhileLoopArithOpsInCndn(AstNode whileNode){
		
		AstNode node=((WhileLoop) whileNode).getCondition();
		return ifStatementChangeArithOps(node);	
	}
	
	
	
	public boolean changeWhileLoopUnrOprType(AstNode whileNode){
		
		AstNode node=((WhileLoop) whileNode).getCondition();
		if (node==null)
			return false;
		String oldSource=node.toSource();
/*		if (node instanceof UnaryExpression){
			
			changeUnaryExpOperatorType(node);
		}
*/
//		else if (node instanceof InfixExpression){
		ArrayList<UnaryExpression> unaryExpInConditionStatm=new ArrayList<UnaryExpression>();
		List<UnaryExpression> unrList=searchNodeForUnaryExp(node,unaryExpInConditionStatm);
		if(unrList.size()>0){
			UnaryExpression unrExpNode=unrList.get(SystemProps.rnd.getNextRandomInt(unrList.size()));
			changeUnaryExpOperatorType(unrExpNode);
		}
//		}
	/*	else if (node instanceof ElementGet && ((ElementGet)node).getElement() instanceof UnaryExpression)
			changeUnaryExpOperatorType(((ElementGet)node).getElement());
	*/	
		String newSource=node.toSource();
		if(newSource.equals(oldSource))
			return false;
		
		return true;
			
	}
	
	public boolean changeWhileLoopUnrOprPosition(AstNode whileNode){
		
		AstNode node=((WhileLoop) whileNode).getCondition();
		if (node==null)
			return false;
		String oldSource=((WhileLoop) whileNode).getCondition().toSource();
/*		if (node instanceof UnaryExpression){
			
			//only cares about changing when it is like ++x: change ++x to x++
			if (((UnaryExpression)node).isPrefix()){
				changeUnaryExpOperatorPosition(node);
			}
		}
*///		else if (node instanceof InfixExpression){
		ArrayList<UnaryExpression> unaryExpInConditionStatm=new ArrayList<UnaryExpression>();
		List<UnaryExpression> unrList=searchNodeForUnaryExp(node,unaryExpInConditionStatm);
		List<UnaryExpression> prefixUnrExpList=new ArrayList<UnaryExpression>();
		for (UnaryExpression unr:unrList){
			if (unr.isPrefix())
					prefixUnrExpList.add(unr);
		}
		if(prefixUnrExpList.size()>0){	
			UnaryExpression prefixUnrNode=prefixUnrExpList.get(SystemProps.rnd.getNextRandomInt(prefixUnrExpList.size()));
			changeUnaryExpOperatorPosition(prefixUnrNode);
		}
	//}
	/*	else if (node instanceof ElementGet && ((ElementGet)node).getElement() instanceof UnaryExpression){
			if (((UnaryExpression)((ElementGet)node).getElement()).isPrefix()){
				changeUnaryExpOperatorPosition(((ElementGet)node).getElement());
			}
			
		}
	*/	String newSource=((WhileLoop) whileNode).getCondition().toSource();
		if (oldSource.equals(newSource))
			return false;
		return true;
	}
	
	
	public boolean changeConsecWhileLoopsOrder(WhileLoop whileLoopNode){
		
		List<WhileLoop> whileList=new ArrayList<WhileLoop>();
		whileList.add(whileLoopNode);
		List<WhileLoop> consecWhiles=new ArrayList<WhileLoop>();
		consecWhiles=searchforConsecWhileLoops((Scope)whileLoopNode,consecWhiles);
		if(consecWhiles==null) return false;
		if (consecWhiles.size()>0){
			
			whileList.addAll(1, consecWhiles);
			int randIndex=SystemProps.rnd.getNextRandomInt(whileList.size()-1);
			WhileLoop firstWhileLoop=whileList.get(randIndex);
			WhileLoop secondWhileLoop=whileList.get(randIndex+1);
		
			AstNode secWhileCndn=secondWhileLoop.getCondition();
		
			secondWhileLoop.setCondition(firstWhileLoop.getCondition());
			firstWhileLoop.setCondition(secWhileCndn);

		}
		else
			return false;
		return true;
		
		
		
	}
	
	/*
	 * branching statements
	 */
	
	public boolean removeReturnStatement(AstNode returnNode){
		
		makeSureBlockExistsAround(returnNode);
		AstNode parent=returnNode.getParent();
		parent.removeChild(returnNode);
		return true;
	}
	
	public boolean changeBoolReturnVal(AstNode returnNode){
		String oldSource=returnNode.toSource();
		AstNode retVal=((ReturnStatement) returnNode).getReturnValue();
		if(retVal instanceof KeywordLiteral){
			if(retVal.getType()==Token.TRUE){
				((KeywordLiteral)retVal).setType(Token.FALSE);
			}
			else if(retVal.getType()==Token.FALSE){
				((KeywordLiteral)retVal).setType(Token.TRUE);
			}
		}
		String newSource=returnNode.toSource();
		if(newSource.equals(oldSource))
			return false;
		return true;
				
	}
	
	public boolean removeBreakStatement(AstNode breakNode){
		makeSureBlockExistsAround(breakNode);
		AstNode parent=breakNode.getParent();
		
		if(breakNode.getParent() instanceof SwitchCase){
			ArrayList<AstNode> list=(ArrayList<AstNode>) ((SwitchCase)breakNode.getParent()).getStatements();
			if(list!=null)
				list.remove(breakNode);
			else return false;
		}
		else{
			if(parent.getChildBefore(breakNode)!=null)
				parent.removeChild(breakNode);
			else{
				parent=parent.getParent();
				makeSureBlockExistsAround(parent);
				AstNode grandParent=parent.getParent();
				grandParent.removeChild(parent);
			}
		}
		return true;
	}
	
	public boolean removeContinueStatement(AstNode continueNode){
		makeSureBlockExistsAround(continueNode);
		AstNode parent=continueNode.getParent();
		if(parent.getChildBefore(continueNode)!=null)
			parent.removeChild(continueNode);
		else{
			parent=parent.getParent();
			makeSureBlockExistsAround(parent);
			AstNode grandParent=parent.getParent();
			grandParent.removeChild(parent);
		}
	
		return true;
	}
	
	/*
	 * this.xxx
	 */
	public boolean removePropertyGetThis(AstNode node){
		
		AstNode parent=node;
		while (parent!=null && ! (parent instanceof ReturnStatement) && ! (parent instanceof ExpressionStatement)){
			
			parent=parent.getParent();
			
		}
		if(parent==null) return false;	
		AstNode grandParent=makeSureBlockExistsAround(parent);
		String newSource=parent.toSource().replace("this.", "");
		AstNode newNode=parse(newSource);
		grandParent.replaceChild(parent, newNode);
		return true;
	}
	/**
	 * private methods:searching for specific terms in the nodes/conditions (for internal use only)
	 */
	
	
	private List<NumberLiteral> searchConditionForNumbers(AstNode condition,List<NumberLiteral> numbersInConditionStatm){
		
		if(condition instanceof ConditionalExpression){
			searchConditionForNumbers(((ConditionalExpression)condition).getTestExpression(),numbersInConditionStatm);
			searchConditionForNumbers(((ConditionalExpression)condition).getFalseExpression(),numbersInConditionStatm);
		}
		
		if (condition instanceof ParenthesizedExpression){
			condition= ((ParenthesizedExpression) condition).getExpression();
			searchConditionForNumbers(condition, numbersInConditionStatm);
		}
		if (condition instanceof UnaryExpression){
			condition= ((UnaryExpression) condition).getOperand();
			searchConditionForNumbers(condition, numbersInConditionStatm);
		}
		if (condition instanceof ElementGet){
			searchConditionForNumbers(((ElementGet) condition).getTarget(), numbersInConditionStatm);
			searchConditionForNumbers(((ElementGet) condition).getElement(), numbersInConditionStatm);
		}
		if (condition instanceof InfixExpression){
			
			if (((InfixExpression)condition).getLeft() instanceof InfixExpression){
				searchConditionForNumbers(((InfixExpression)condition).getLeft(),numbersInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getLeft() instanceof ParenthesizedExpression){
				searchConditionForNumbers(((ParenthesizedExpression)((InfixExpression)condition).getLeft()).getExpression()
						, numbersInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getLeft() instanceof UnaryExpression){
				searchConditionForNumbers(((UnaryExpression)((InfixExpression)condition).getLeft()).getOperand()
						, numbersInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getLeft() instanceof ElementGet){
				searchConditionForNumbers(((ElementGet)((InfixExpression)condition).getLeft()).getTarget()
						, numbersInConditionStatm);
				searchConditionForNumbers(((ElementGet)((InfixExpression)condition).getLeft()).getElement()
						, numbersInConditionStatm);
			}
		
			if (((InfixExpression)condition).getRight() instanceof InfixExpression){
				searchConditionForNumbers(((InfixExpression)condition).getRight(),numbersInConditionStatm);
			}
		
			else if (((InfixExpression)condition).getRight() instanceof ParenthesizedExpression){
				searchConditionForNumbers(((ParenthesizedExpression)((InfixExpression)condition).getRight()).getExpression(),
						numbersInConditionStatm);
			}	
			
			else if (((InfixExpression)condition).getRight() instanceof ElementGet){
				searchConditionForNumbers(((ElementGet)((InfixExpression)condition).getRight()).getTarget(),
						numbersInConditionStatm);
				searchConditionForNumbers(((ElementGet)((InfixExpression)condition).getRight()).getElement(),
						numbersInConditionStatm);
			}
			else if (((InfixExpression)condition).getRight() instanceof UnaryExpression){
				searchConditionForNumbers(((UnaryExpression)((InfixExpression)condition).getRight()).getOperand(),
						numbersInConditionStatm);
			}
			if (((InfixExpression)condition).getLeft() instanceof NumberLiteral){
				NumberLiteral n=(NumberLiteral) ((InfixExpression)condition).getLeft();
				numbersInConditionStatm.add(n);
			}
		
			if (((InfixExpression)condition).getRight() instanceof NumberLiteral){
				NumberLiteral n=(NumberLiteral) ((InfixExpression)condition).getRight();
				numbersInConditionStatm.add(n);
			}
		}
		else if(condition instanceof NumberLiteral){
			numbersInConditionStatm.add((NumberLiteral) condition);
		}
		
		return numbersInConditionStatm;
		
	}
	
	
	
	private List<InfixExpression> searchConditionForRelOps(AstNode condition,List<InfixExpression> relOpsInConditionStatm){
		
		if (condition instanceof ParenthesizedExpression){
			condition= ((ParenthesizedExpression) condition).getExpression();
			searchConditionForRelOps(condition, relOpsInConditionStatm);
		}
		if (condition instanceof UnaryExpression){
			condition= ((UnaryExpression) condition).getOperand();
			searchConditionForRelOps(condition, relOpsInConditionStatm);
		}

		
	
		if (condition instanceof InfixExpression){
			int op=((InfixExpression)condition).getOperator();
			if((op>=12 && op<=17) || (op==47) || (op==46))
				relOpsInConditionStatm.add((InfixExpression) condition);
			if (((InfixExpression) condition).getLeft() instanceof ParenthesizedExpression)
					searchConditionForRelOps(((ParenthesizedExpression)((InfixExpression) condition).getLeft()).getExpression()
							,relOpsInConditionStatm);
			else
				if (((InfixExpression) condition).getLeft() instanceof InfixExpression)
						searchConditionForRelOps(((InfixExpression) condition).getLeft(),
								relOpsInConditionStatm);
			else if (((InfixExpression) condition).getLeft() instanceof UnaryExpression)
				searchConditionForRelOps(((UnaryExpression)((InfixExpression) condition).getLeft()).getOperand()
						,relOpsInConditionStatm);
				
			if (((InfixExpression) condition).getRight() instanceof ParenthesizedExpression)
					searchConditionForRelOps(((ParenthesizedExpression)((InfixExpression) condition).getRight()).getExpression()
							, relOpsInConditionStatm); 
			else
				if (((InfixExpression) condition).getRight() instanceof InfixExpression)
						searchConditionForRelOps(((InfixExpression) condition).getRight(),relOpsInConditionStatm);
			
			else if (((InfixExpression) condition).getRight() instanceof UnaryExpression)
				searchConditionForRelOps(((UnaryExpression)((InfixExpression) condition).getRight()).getOperand()
						, relOpsInConditionStatm);  	
			
		}
	

		return relOpsInConditionStatm;
		
	}
	
	
	private List<InfixExpression> searchConditionForLogicOps(AstNode condition,List<InfixExpression> logicOpsInConditionStatm){
		
		if (condition instanceof ParenthesizedExpression){
			condition= ((ParenthesizedExpression) condition).getExpression();
			searchConditionForLogicOps(condition, logicOpsInConditionStatm);
		}
		if (condition instanceof UnaryExpression){
			condition= ((UnaryExpression) condition).getOperand();
			searchConditionForLogicOps(condition, logicOpsInConditionStatm);
		}
	
		if (condition instanceof InfixExpression){
			int op=((InfixExpression)condition).getOperator();
			if(op==104 || op==105){
				logicOpsInConditionStatm.add((InfixExpression) condition);
				searchConditionForLogicOps(((InfixExpression) condition).getLeft(),logicOpsInConditionStatm);
				searchConditionForLogicOps(((InfixExpression) condition).getRight(),logicOpsInConditionStatm);
			}
				
			
		}
		

		return logicOpsInConditionStatm;
		
	}
	
	
	
	private List<InfixExpression> searchConditionForArithOps(AstNode condition,List<InfixExpression> arithOpsInConditionStatm){
		
		if (condition instanceof ParenthesizedExpression){
			condition= ((ParenthesizedExpression) condition).getExpression();
			searchConditionForArithOps(condition, arithOpsInConditionStatm);
		}
		else if (condition instanceof UnaryExpression){
			condition= ((UnaryExpression) condition).getOperand();
			searchConditionForArithOps(condition, arithOpsInConditionStatm);
		}
		else if (condition instanceof ElementGet){
		
			searchConditionForArithOps(((ElementGet) condition).getTarget(), arithOpsInConditionStatm);
			searchConditionForArithOps(((ElementGet) condition).getElement(), arithOpsInConditionStatm);
		}
		
		if (condition instanceof InfixExpression){
			InfixExpression cndn=(InfixExpression) condition;
			if (cndn.getLeft() instanceof InfixExpression){
				searchConditionForArithOps(cndn.getLeft(),arithOpsInConditionStatm);
			}
			else if (((InfixExpression)condition).getLeft() instanceof ParenthesizedExpression){
				searchConditionForArithOps(((ParenthesizedExpression)((InfixExpression)condition).getLeft()).getExpression(),
						arithOpsInConditionStatm);
			}	
			
			else if (((InfixExpression)condition).getLeft() instanceof UnaryExpression){
				searchConditionForArithOps(((UnaryExpression)((InfixExpression)condition).getLeft()).getOperand(),
						arithOpsInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getLeft() instanceof ElementGet){
				searchConditionForArithOps(((ElementGet)((InfixExpression)condition).getLeft()).getTarget(),
						arithOpsInConditionStatm);
				searchConditionForArithOps(((ElementGet)((InfixExpression)condition).getLeft()).getElement(),
						arithOpsInConditionStatm);
			}
			if (cndn.getRight() instanceof InfixExpression){
				searchConditionForArithOps(cndn.getRight(),arithOpsInConditionStatm);
			}
			else if (((InfixExpression)condition).getRight() instanceof ParenthesizedExpression){
				searchConditionForArithOps(((ParenthesizedExpression)((InfixExpression)condition).getRight()).getExpression(),
						arithOpsInConditionStatm);
			}	
			
			else if (((InfixExpression)condition).getRight() instanceof UnaryExpression){
				searchConditionForArithOps(((UnaryExpression)((InfixExpression)condition).getRight()).getOperand(),
						arithOpsInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getRight() instanceof ElementGet){
				searchConditionForArithOps(((ElementGet)((InfixExpression)condition).getRight()).getTarget(),
						arithOpsInConditionStatm);
				searchConditionForArithOps(((ElementGet)((InfixExpression)condition).getRight()).getElement(),
						arithOpsInConditionStatm);
			}
			if (cndn.getOperator()>=21
					&& cndn.getOperator()<=24){
				
				arithOpsInConditionStatm.add(cndn);
			}
		
		}
		return arithOpsInConditionStatm;
		
	}
	
	
	
	private List<KeywordLiteral> searchConditionForKeyLiterals(AstNode condition,ArrayList<KeywordLiteral> keyLiteralsInConditionStatm){
		
		if(condition instanceof ConditionalExpression){
			searchConditionForKeyLiterals(((ConditionalExpression)condition).getTestExpression(),keyLiteralsInConditionStatm);
			searchConditionForKeyLiterals(((ConditionalExpression)condition).getFalseExpression(),keyLiteralsInConditionStatm);
		}
		
		if (condition instanceof ParenthesizedExpression){
			condition= ((ParenthesizedExpression) condition).getExpression();
			searchConditionForKeyLiterals(condition, keyLiteralsInConditionStatm);
		}
		if (condition instanceof UnaryExpression){
			condition= ((UnaryExpression) condition).getOperand();
			searchConditionForKeyLiterals(condition, keyLiteralsInConditionStatm);
		}

		
		if (condition instanceof InfixExpression){
		
			if (((InfixExpression)condition).getLeft() instanceof InfixExpression){
				searchConditionForKeyLiterals(((InfixExpression)condition).getLeft(),keyLiteralsInConditionStatm);
			}
			else if (((InfixExpression)condition).getLeft() instanceof ParenthesizedExpression){
				searchConditionForKeyLiterals(((ParenthesizedExpression)((InfixExpression)condition).getLeft()).getExpression(),
						keyLiteralsInConditionStatm);
			}	
			
			else if (((InfixExpression)condition).getLeft() instanceof UnaryExpression){
				searchConditionForKeyLiterals(((UnaryExpression)((InfixExpression)condition).getLeft()).getOperand(),
						keyLiteralsInConditionStatm);
			}
			
			if (((InfixExpression)condition).getRight() instanceof InfixExpression){
				searchConditionForKeyLiterals(((InfixExpression)condition).getRight(),keyLiteralsInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getRight() instanceof ParenthesizedExpression){
				searchConditionForKeyLiterals(((ParenthesizedExpression)((InfixExpression)condition).getRight()).getExpression(),
						keyLiteralsInConditionStatm);
			}	
			
			else if (((InfixExpression)condition).getRight() instanceof UnaryExpression){
				searchConditionForKeyLiterals(((UnaryExpression)((InfixExpression)condition).getRight()).getOperand(),
						keyLiteralsInConditionStatm);
			}
			
			if (((InfixExpression)condition).getLeft() instanceof KeywordLiteral){
				KeywordLiteral keyLit=(KeywordLiteral) ((InfixExpression)condition).getLeft();
				keyLiteralsInConditionStatm.add(keyLit);
			}
		
			if (((InfixExpression)condition).getRight() instanceof KeywordLiteral){
				KeywordLiteral keyLit=(KeywordLiteral) ((InfixExpression)condition).getRight();
				keyLiteralsInConditionStatm.add(keyLit);
			}
		}
		else if(condition instanceof KeywordLiteral)
			keyLiteralsInConditionStatm.add((KeywordLiteral) condition);
		
		return keyLiteralsInConditionStatm;
		
	}
	
	
	
	private List<Name> searchConditionForNames(AstNode condition,ArrayList<Name> namesInConditionStatm){
		
		if(condition instanceof FunctionCall){
			searchConditionForNames(((FunctionCall)condition).getTarget(),namesInConditionStatm);
		}
		
		if(condition instanceof ConditionalExpression){
			searchConditionForNames(((ConditionalExpression)condition).getTestExpression(),namesInConditionStatm);
			searchConditionForNames(((ConditionalExpression)condition).getFalseExpression(),namesInConditionStatm);
		}
		
		if (condition instanceof PropertyGet){
			condition= ((PropertyGet) condition).getProperty();
			searchConditionForNames(condition, namesInConditionStatm);
		}
		if (condition instanceof ParenthesizedExpression){
			condition= ((ParenthesizedExpression) condition).getExpression();
			searchConditionForNames(condition, namesInConditionStatm);
		}
		if (condition instanceof UnaryExpression){
			condition= ((UnaryExpression) condition).getOperand();
			searchConditionForNames(condition, namesInConditionStatm);
		}
		if (condition instanceof ElementGet){
			
			searchConditionForNames(((ElementGet) condition).getTarget(), namesInConditionStatm);
			searchConditionForNames(((ElementGet) condition).getElement(), namesInConditionStatm);
		}
		
		if (condition instanceof InfixExpression){
		
			if (((InfixExpression)condition).getLeft() instanceof InfixExpression){
				searchConditionForNames(((InfixExpression)condition).getLeft(),namesInConditionStatm);
			}
			else if (((InfixExpression)condition).getLeft() instanceof ParenthesizedExpression){
				searchConditionForNames(((ParenthesizedExpression)((InfixExpression)condition).getLeft()).getExpression(),
						namesInConditionStatm);
			}	
			
			else if (((InfixExpression)condition).getLeft() instanceof UnaryExpression){
				searchConditionForNames(((UnaryExpression)((InfixExpression)condition).getLeft()).getOperand(),
						namesInConditionStatm);
			}
			else if (((InfixExpression)condition).getLeft() instanceof PropertyGet){
				searchConditionForNames((PropertyGet)((InfixExpression)condition).getLeft(),
						namesInConditionStatm);
			}
			else if (((InfixExpression)condition).getLeft() instanceof ElementGet){
				searchConditionForNames(((ElementGet)((InfixExpression)condition).getLeft()).getTarget(),
						namesInConditionStatm);
				searchConditionForNames(((ElementGet)((InfixExpression)condition).getLeft()).getElement(),
						namesInConditionStatm);
			}
			
			if (((InfixExpression)condition).getRight() instanceof InfixExpression){
				searchConditionForNames(((InfixExpression)condition).getRight(),namesInConditionStatm);
			}
			else if (((InfixExpression)condition).getRight() instanceof ParenthesizedExpression){
				searchConditionForNames(((ParenthesizedExpression)((InfixExpression)condition).getRight()).getExpression(),
						namesInConditionStatm);
			}	
			else if (((InfixExpression)condition).getRight() instanceof PropertyGet){
				searchConditionForNames((PropertyGet)((InfixExpression)condition).getRight(),
						namesInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getRight() instanceof UnaryExpression){
				searchConditionForNames(((UnaryExpression)((InfixExpression)condition).getRight()).getOperand(),
						namesInConditionStatm);
			}
			else if (((InfixExpression)condition).getRight() instanceof ElementGet){
				searchConditionForNames(((ElementGet)((InfixExpression)condition).getRight()).getTarget(),
						namesInConditionStatm);
				searchConditionForNames(((ElementGet)((InfixExpression)condition).getRight()).getElement(),
						namesInConditionStatm);
			}
		
			if (((InfixExpression)condition).getLeft() instanceof Name){
				Name name=(Name) ((InfixExpression)condition).getLeft();
				namesInConditionStatm.add(name);
			}
		
			if (((InfixExpression)condition).getRight() instanceof Name){
				Name name=(Name) ((InfixExpression)condition).getRight();
				namesInConditionStatm.add(name);
			}
		}
		
		else
			if (condition instanceof Name)
				namesInConditionStatm.add((Name)condition);
		
		return namesInConditionStatm;
		
	}
	
	
	
	private List<UnaryExpression> searchNodeForUnaryExp(AstNode condition,ArrayList<UnaryExpression> unaryExpInConditionStatm){
		
		
		if (condition instanceof ParenthesizedExpression){
			condition= ((ParenthesizedExpression) condition).getExpression();
			searchNodeForUnaryExp(condition, unaryExpInConditionStatm);
		}
		if (condition instanceof ElementGet){
			searchNodeForUnaryExp(((ElementGet) condition).getTarget(), unaryExpInConditionStatm);
			searchNodeForUnaryExp(((ElementGet) condition).getElement(), unaryExpInConditionStatm);
		}

		
		if (condition instanceof InfixExpression){
		
			if (((InfixExpression)condition).getLeft() instanceof InfixExpression){
				searchNodeForUnaryExp(((InfixExpression)condition).getLeft(), unaryExpInConditionStatm);
			}
			else if (((InfixExpression)condition).getLeft() instanceof ParenthesizedExpression){
				searchNodeForUnaryExp(((ParenthesizedExpression)((InfixExpression)condition).getLeft()).getExpression(),
						unaryExpInConditionStatm);
			}	
			
			else if (((InfixExpression)condition).getLeft() instanceof ElementGet){
				searchNodeForUnaryExp(((ElementGet)((InfixExpression)condition).getLeft()).getTarget(),
						unaryExpInConditionStatm);
				searchNodeForUnaryExp(((ElementGet)((InfixExpression)condition).getLeft()).getElement(),
						unaryExpInConditionStatm);
			}	
			

			
			if (((InfixExpression)condition).getRight() instanceof InfixExpression){
				searchNodeForUnaryExp(((InfixExpression)condition).getRight(),unaryExpInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getRight() instanceof ParenthesizedExpression){
				searchNodeForUnaryExp(((ParenthesizedExpression)((InfixExpression)condition).getRight()).getExpression(),
						unaryExpInConditionStatm);
			}
		
			else if (((InfixExpression)condition).getRight() instanceof ElementGet){
				searchNodeForUnaryExp(((ElementGet)((InfixExpression)condition).getRight()).getTarget(),
						unaryExpInConditionStatm);
				searchNodeForUnaryExp(((ElementGet)((InfixExpression)condition).getRight()).getElement(),
						unaryExpInConditionStatm);
			}
			if (((InfixExpression)condition).getLeft() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((InfixExpression)condition).getLeft();
				unaryExpInConditionStatm.add(unaryExp);
			}
	/*		else if (((InfixExpression)condition).getLeft() instanceof ElementGet 
					&& ((ElementGet)((InfixExpression)condition).getLeft()).getElement() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((ElementGet)((InfixExpression)condition).getLeft()).getElement();
				unaryExpInConditionStatm.add(unaryExp);
				
			}
	*/	
			if (((InfixExpression)condition).getRight() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((InfixExpression)condition).getRight();
				unaryExpInConditionStatm.add(unaryExp);
			}
			
	/*		else if (((InfixExpression)condition).getRight() instanceof ElementGet 
					&& ((ElementGet)((InfixExpression)condition).getRight()).getElement() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((ElementGet)((InfixExpression)condition).getRight()).getElement();
				unaryExpInConditionStatm.add(unaryExp);
				
			}
	*/	}
		
		else
			if (condition instanceof UnaryExpression){
				unaryExpInConditionStatm.add((UnaryExpression) condition);
				searchNodeForUnaryExp(((UnaryExpression) condition).getOperand(), unaryExpInConditionStatm);
			}
		
		return unaryExpInConditionStatm;
		
	}
	
	
	
	/**
	 * search for specific terms in the assignment statements 
	 */
	
	
	private List<InfixExpression> searchAssignmentForArithOps(AstNode infixExp,List<InfixExpression> arithOpsInAssignStatm){
		
		if (infixExp instanceof ParenthesizedExpression){
			infixExp= ((ParenthesizedExpression) infixExp).getExpression();
			searchAssignmentForArithOps(infixExp,arithOpsInAssignStatm);
		}
		if (infixExp instanceof ElementGet){
			
			searchAssignmentForArithOps(((ElementGet) infixExp).getTarget(),arithOpsInAssignStatm);
			searchAssignmentForArithOps(((ElementGet) infixExp).getElement(),arithOpsInAssignStatm);
		}
		
		if (infixExp instanceof InfixExpression){
			InfixExpression infix=(InfixExpression) infixExp;
			if (infix.getLeft() instanceof InfixExpression){
				searchAssignmentForArithOps(infix.getLeft(),arithOpsInAssignStatm);
			}
			else if (((InfixExpression)infixExp).getLeft() instanceof ParenthesizedExpression){
				searchAssignmentForArithOps(((ParenthesizedExpression)((InfixExpression)infixExp).getLeft()).getExpression(),
						arithOpsInAssignStatm);
			}
			else if (((InfixExpression)infixExp).getLeft() instanceof ElementGet){
				searchAssignmentForArithOps(((ElementGet)((InfixExpression)infixExp).getLeft()).getTarget(),
						arithOpsInAssignStatm);
				searchAssignmentForArithOps(((ElementGet)((InfixExpression)infixExp).getLeft()).getElement(),
						arithOpsInAssignStatm);
			}
			if (infix.getRight() instanceof InfixExpression){
				searchAssignmentForArithOps(infix.getRight(),arithOpsInAssignStatm);
			}
			else if (((InfixExpression)infixExp).getRight() instanceof ParenthesizedExpression){
				searchAssignmentForArithOps(((ParenthesizedExpression)((InfixExpression)infixExp).getRight()).getExpression(),
						arithOpsInAssignStatm);
			}
			else if (((InfixExpression)infixExp).getRight() instanceof ElementGet){
				searchAssignmentForArithOps(((ElementGet)((InfixExpression)infixExp).getRight()).getTarget(),
						arithOpsInAssignStatm);
				searchAssignmentForArithOps(((ElementGet)((InfixExpression)infixExp).getRight()).getElement(),
						arithOpsInAssignStatm);
			}
		
			if ((infix.getOperator()>=21
					&& infix.getOperator()<=24) || (infix.getOperator()>=97
					&& infix.getOperator()<=100)){
				
				arithOpsInAssignStatm.add(infix);
			}
		
		}
		return arithOpsInAssignStatm;
		
	}
	
	
	private List<Name> searchAssignmentForNames(AstNode infixExp, List<Name> namesInAssignment){
		
		if(infixExp instanceof ConditionalExpression){
			searchAssignmentForNames(((ConditionalExpression)infixExp).getTestExpression(),namesInAssignment);
			searchAssignmentForNames(((ConditionalExpression)infixExp).getFalseExpression(),namesInAssignment);
		}
		
		if (infixExp instanceof ParenthesizedExpression){
			infixExp= ((ParenthesizedExpression) infixExp).getExpression();
			searchAssignmentForNames(infixExp,namesInAssignment);
		}
		
		else if (infixExp instanceof ElementGet){
			
			searchAssignmentForNames(((ElementGet) infixExp).getTarget(), namesInAssignment);
			searchAssignmentForNames(((ElementGet) infixExp).getElement(), namesInAssignment);
		}
		else if (infixExp instanceof PropertyGet){
			infixExp= ((PropertyGet) infixExp).getProperty();
			searchAssignmentForNames(infixExp,namesInAssignment);
		}
		else if (infixExp instanceof FunctionCall){
			infixExp= ((FunctionCall) infixExp).getTarget();
			searchAssignmentForNames(infixExp,namesInAssignment);
		}
		
		
		if (infixExp instanceof InfixExpression){
			
			if (((InfixExpression)infixExp).getLeft() instanceof InfixExpression){
				searchAssignmentForNames(((InfixExpression)infixExp).getLeft(),namesInAssignment);
			}
			else if (((InfixExpression)infixExp).getLeft() instanceof ParenthesizedExpression){
				searchAssignmentForNames(((ParenthesizedExpression)((InfixExpression)infixExp).getLeft()).getExpression(),
						namesInAssignment);
			}
			else if (((InfixExpression)infixExp).getLeft() instanceof PropertyGet){
				searchAssignmentForNames((PropertyGet)((InfixExpression)infixExp).getLeft(),
						namesInAssignment);
			}
			else if (((InfixExpression)infixExp).getLeft() instanceof ElementGet){
				searchAssignmentForNames(((ElementGet)((InfixExpression)infixExp).getLeft()).getTarget(),
						namesInAssignment);
				searchAssignmentForNames(((ElementGet)((InfixExpression)infixExp).getLeft()).getElement(),
						namesInAssignment);
			}
			if (((InfixExpression)infixExp).getRight() instanceof InfixExpression){
				searchAssignmentForNames(((InfixExpression)infixExp).getRight(),namesInAssignment);
			}
			
			else if (((InfixExpression)infixExp).getRight() instanceof ParenthesizedExpression){
				searchAssignmentForNames(((ParenthesizedExpression)((InfixExpression)infixExp).getRight()).getExpression(),
						namesInAssignment);
			}
			else if (((InfixExpression)infixExp).getRight() instanceof ElementGet){
				searchAssignmentForNames(((ElementGet)((InfixExpression)infixExp).getRight()).getTarget(),
						namesInAssignment);
				searchAssignmentForNames(((ElementGet)((InfixExpression)infixExp).getRight()).getElement(),
						namesInAssignment);
			}
			else if (((InfixExpression)infixExp).getRight() instanceof PropertyGet){
				searchAssignmentForNames((PropertyGet)((InfixExpression)infixExp).getRight(),
						namesInAssignment);
			}
			if (((InfixExpression)infixExp).getLeft() instanceof Name){
				Name name=(Name) ((InfixExpression)infixExp).getLeft();
				namesInAssignment.add(name);
			}
		
			if (((InfixExpression)infixExp).getRight() instanceof Name){
				Name name=(Name) ((InfixExpression)infixExp).getRight();
				namesInAssignment.add(name);
			}
		}
		
		else
			if (infixExp instanceof Name)
				namesInAssignment.add((Name)infixExp);
		
		return namesInAssignment;
		
	}
	
	

	private List<NumberLiteral> searchAssignmentForNumbers(AstNode infixExp,List<NumberLiteral> numbersInAssignment){
		
		if(infixExp instanceof ConditionalExpression){
			searchAssignmentForNumbers(((ConditionalExpression)infixExp).getTestExpression(),numbersInAssignment);
			searchAssignmentForNumbers(((ConditionalExpression)infixExp).getFalseExpression(),numbersInAssignment);
		}
		if (infixExp instanceof ParenthesizedExpression){
			infixExp= ((ParenthesizedExpression) infixExp).getExpression();
			searchAssignmentForNumbers(infixExp,numbersInAssignment);
		}
		
		if (infixExp instanceof ElementGet){
			searchAssignmentForNumbers( ((ElementGet) infixExp).getTarget(),numbersInAssignment);
			searchAssignmentForNumbers( ((ElementGet) infixExp).getElement(),numbersInAssignment);
		}		
		
		if (infixExp instanceof InfixExpression){
			
			if (((InfixExpression)infixExp).getLeft() instanceof InfixExpression){
				searchAssignmentForNumbers(((InfixExpression)infixExp).getLeft(),numbersInAssignment);
			}
			else if (((InfixExpression)infixExp).getLeft() instanceof ParenthesizedExpression){
				searchAssignmentForNumbers(((ParenthesizedExpression)((InfixExpression)infixExp).getLeft()).getExpression(),
						numbersInAssignment);
			}
			
			else if (((InfixExpression)infixExp).getLeft() instanceof ElementGet){
				searchAssignmentForNumbers(((ElementGet)((InfixExpression)infixExp).getLeft()).getTarget(),
						numbersInAssignment);
				searchAssignmentForNumbers(((ElementGet)((InfixExpression)infixExp).getLeft()).getElement(),
						numbersInAssignment);
			}
			
			if (((InfixExpression)infixExp).getRight() instanceof InfixExpression){
				searchAssignmentForNumbers(((InfixExpression)infixExp).getRight(),numbersInAssignment);
			}
			
			else if (((InfixExpression)infixExp).getRight() instanceof ParenthesizedExpression){
				searchAssignmentForNumbers(((ParenthesizedExpression)((InfixExpression)infixExp).getRight()).getExpression(),
						numbersInAssignment);
			}
			else if (((InfixExpression)infixExp).getRight() instanceof ElementGet){
				searchAssignmentForNumbers(((ElementGet)((InfixExpression)infixExp).getRight()).getTarget(),
						numbersInAssignment);
				searchAssignmentForNumbers(((ElementGet)((InfixExpression)infixExp).getRight()).getElement(),
						numbersInAssignment);
			}
			if (((InfixExpression)infixExp).getLeft() instanceof NumberLiteral){
				NumberLiteral num=(NumberLiteral) ((InfixExpression)infixExp).getLeft();
				numbersInAssignment.add(num);
			}
		
			if (((InfixExpression)infixExp).getRight() instanceof NumberLiteral){
				NumberLiteral num=(NumberLiteral) ((InfixExpression)infixExp).getRight();
				numbersInAssignment.add(num);
			}
		}
		
		else
			if (infixExp instanceof NumberLiteral)
				numbersInAssignment.add((NumberLiteral)infixExp);
		
		return numbersInAssignment;
		
	}
	
private List<StringLiteral> searchAssignmentForStrings(AstNode infixExp,List<StringLiteral> stringsInAssignment){
		
		if(infixExp instanceof ConditionalExpression){
			searchAssignmentForStrings(((ConditionalExpression)infixExp).getTestExpression(),stringsInAssignment);
			searchAssignmentForStrings(((ConditionalExpression)infixExp).getFalseExpression(),stringsInAssignment);
		}	
	
		if (infixExp instanceof ParenthesizedExpression){
			infixExp= ((ParenthesizedExpression) infixExp).getExpression();
			searchAssignmentForStrings(infixExp,stringsInAssignment);
		}
		
		if (infixExp instanceof InfixExpression){
			
			if (((InfixExpression)infixExp).getLeft() instanceof InfixExpression){
				searchAssignmentForStrings(((InfixExpression)infixExp).getLeft(),stringsInAssignment);
			}
			
			else if (((InfixExpression)infixExp).getLeft() instanceof ParenthesizedExpression){
				searchAssignmentForStrings(((ParenthesizedExpression)((InfixExpression)infixExp).getLeft()).getExpression(),
						stringsInAssignment);
			}
			if (((InfixExpression)infixExp).getRight() instanceof InfixExpression){
				searchAssignmentForStrings(((InfixExpression)infixExp).getRight(),stringsInAssignment);
			}
			
			else if (((InfixExpression)infixExp).getRight() instanceof ParenthesizedExpression){
				searchAssignmentForStrings(((ParenthesizedExpression)((InfixExpression)infixExp).getRight()).getExpression(),
						stringsInAssignment);
			}
			if (((InfixExpression)infixExp).getLeft() instanceof StringLiteral){
				StringLiteral str=(StringLiteral) ((InfixExpression)infixExp).getLeft();
				stringsInAssignment.add(str);
			}
		
			if (((InfixExpression)infixExp).getRight() instanceof NumberLiteral){
				StringLiteral str=(StringLiteral) ((InfixExpression)infixExp).getRight();
				stringsInAssignment.add(str);
			}
		}
		
		else
			if (infixExp instanceof StringLiteral)
				stringsInAssignment.add((StringLiteral)infixExp);
		
		return stringsInAssignment;
		
	}
	
	
	private List<ElementGet> searchAssignmentForArrayElem(AstNode infixExp,List<ElementGet> arrays){
		
		if (infixExp instanceof ParenthesizedExpression){
			infixExp= ((ParenthesizedExpression) infixExp).getExpression();
			searchAssignmentForArrayElem(infixExp,arrays);
		}
		
		if (infixExp instanceof InfixExpression){
			
			if (((InfixExpression)infixExp).getLeft() instanceof InfixExpression){
				searchAssignmentForArrayElem(((InfixExpression)infixExp).getLeft(), arrays);
			}
			else if (((InfixExpression)infixExp).getLeft() instanceof ParenthesizedExpression){
				searchAssignmentForArrayElem(((ParenthesizedExpression)((InfixExpression)infixExp).getLeft()).getExpression(),
						arrays);
			}
			
			if (((InfixExpression)infixExp).getRight() instanceof InfixExpression){
				searchAssignmentForArrayElem(((InfixExpression)infixExp).getRight(), arrays);
			}
			
			else if (((InfixExpression)infixExp).getRight() instanceof ParenthesizedExpression){
				searchAssignmentForArrayElem(((ParenthesizedExpression)((InfixExpression)infixExp).getRight()).getExpression(),
						arrays);
			}
			if (((InfixExpression)infixExp).getLeft() instanceof ElementGet)
				if (((ElementGet)((InfixExpression)infixExp).getLeft()).getElement() instanceof Name 
						|| ((ElementGet)((InfixExpression)infixExp).getLeft()).getElement() instanceof NumberLiteral ){
				ElementGet elem=(ElementGet) ((InfixExpression)infixExp).getLeft();
				arrays.add(elem);
			}
		
			if (((InfixExpression)infixExp).getRight() instanceof ElementGet)
				if (((ElementGet)((InfixExpression)infixExp).getRight()).getElement() instanceof Name 
						|| ((ElementGet)((InfixExpression)infixExp).getRight()).getElement() instanceof NumberLiteral ){
				ElementGet elem=(ElementGet) ((InfixExpression)infixExp).getRight();
				arrays.add(elem);
			}
		}		
		else
			if (infixExp instanceof ElementGet){
			
				arrays.add((ElementGet)infixExp);
				searchAssignmentForArrayElem(((ElementGet)infixExp).getTarget(), arrays);
				searchAssignmentForArrayElem(((ElementGet)infixExp).getElement(), arrays);
				
			}
		
		return arrays;
		
	}
	
	
	private List<UnaryExpression> searchAssignmentForUnaryExp(AstNode infixExp,ArrayList<UnaryExpression> unaryExpInAssignment){
		
		
		if (infixExp instanceof ParenthesizedExpression){
			infixExp= ((ParenthesizedExpression) infixExp).getExpression();
			searchAssignmentForUnaryExp(infixExp,unaryExpInAssignment);
		}
		
		if (infixExp instanceof ElementGet){
			searchAssignmentForUnaryExp(((ElementGet) infixExp).getTarget(),unaryExpInAssignment);
			searchAssignmentForUnaryExp(((ElementGet) infixExp).getElement(),unaryExpInAssignment);
		}
		
		if (infixExp instanceof InfixExpression){
		
			if (((InfixExpression)infixExp).getLeft() instanceof InfixExpression){
				searchAssignmentForUnaryExp(((InfixExpression)infixExp).getLeft(),unaryExpInAssignment);
				
			}
			else if (((InfixExpression)infixExp).getLeft() instanceof ParenthesizedExpression){
				searchAssignmentForUnaryExp(((ParenthesizedExpression)((InfixExpression)infixExp).getLeft()).getExpression(),
						unaryExpInAssignment);
			}
			
			else if (((InfixExpression)infixExp).getLeft() instanceof ElementGet){
				searchAssignmentForUnaryExp(((ElementGet)((InfixExpression)infixExp).getLeft()).getTarget(),
						unaryExpInAssignment);
				searchAssignmentForUnaryExp(((ElementGet)((InfixExpression)infixExp).getLeft()).getElement(),
						unaryExpInAssignment);
			}
			
			if (((InfixExpression)infixExp).getRight() instanceof InfixExpression){
				searchAssignmentForUnaryExp(((InfixExpression)infixExp).getRight(),unaryExpInAssignment);
			}
			else if (((InfixExpression)infixExp).getRight() instanceof ParenthesizedExpression){
				searchAssignmentForUnaryExp(((ParenthesizedExpression)((InfixExpression)infixExp).getRight()).getExpression(),
						unaryExpInAssignment);
			}
			
			else if (((InfixExpression)infixExp).getRight() instanceof ElementGet){
				searchAssignmentForUnaryExp(((ElementGet)((InfixExpression)infixExp).getRight()).getTarget(),
						unaryExpInAssignment);
				searchAssignmentForUnaryExp(((ElementGet)((InfixExpression)infixExp).getRight()).getElement(),
						unaryExpInAssignment);
			}
			if (((InfixExpression)infixExp).getLeft() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((InfixExpression)infixExp).getLeft();
				unaryExpInAssignment.add(unaryExp);
			}
		
			if (((InfixExpression)infixExp).getRight() instanceof UnaryExpression){
				UnaryExpression unaryExp=(UnaryExpression) ((InfixExpression)infixExp).getRight();
				unaryExpInAssignment.add(unaryExp);
			}
		}
		
		else
			if (infixExp instanceof UnaryExpression){
				unaryExpInAssignment.add((UnaryExpression) infixExp);
				searchAssignmentForUnaryExp(infixExp, unaryExpInAssignment);
			}
		return unaryExpInAssignment;
		
	}
	
	
	
	private List<ForLoop> searchforConsecForLoops(Scope forLoop,List<ForLoop> forLoopList){
		
		if(forLoop.getChildScopes()==null)
			return null;
		for (int i=0;i<forLoop.getChildScopes().size();i++){
			if (forLoop.getChildScopes().get(i) instanceof ForLoop){
				ForLoop desiredforLoop=(ForLoop)forLoop.getChildScopes().get(i);
				forLoopList.add(desiredforLoop);
			}
			else
				searchforConsecForLoops(forLoop.getChildScopes().get(i), forLoopList);
		}
		return forLoopList;
		
	}
	
	
	private List<WhileLoop> searchforConsecWhileLoops(Scope WhileLoop, List<WhileLoop> whileLoopList){
		if(WhileLoop.getChildScopes()==null)
			return null;
		for (int i=0;i<WhileLoop.getChildScopes().size();i++){
			if (WhileLoop.getChildScopes().get(i) instanceof WhileLoop){
				WhileLoop desiredWhileLoop=(WhileLoop)WhileLoop.getChildScopes().get(i);
				whileLoopList.add(desiredWhileLoop);
			}
			else
				searchforConsecWhileLoops(WhileLoop.getChildScopes().get(i),whileLoopList);
		}
		return whileLoopList;
		
	}
	





		
		
	
	
	/*
	 * get some useful information from different type of node lists/some useful private methods (for internal use only)
	 */
	
	private List<KeywordLiteral> getKeyLiteralNodes(int token, List<KeywordLiteral> literalList){
		
		List<KeywordLiteral> keyLitlist=new ArrayList<KeywordLiteral>();
		for (KeywordLiteral litNode:literalList){
			if (litNode.getType()==token){
				keyLitlist.add(litNode);
			}
		}
		
		return keyLitlist;
		
		
	}
	
	private List<InfixExpression> getDesiredInfixNodes(int token, List<InfixExpression> infixExpList){
		
		List<InfixExpression> infixList=new ArrayList<InfixExpression>();
		for (InfixExpression infixNode:infixExpList){
			if (infixNode.getOperator()==token){
				infixList.add(infixNode);
			}
		}
		
		return infixList;
		
		
	}
	
	
	/* search for "undefined" in the condition */
private List<StringLiteral> searchConditionForUndefOrNullString(AstNode condition,List<StringLiteral> undefOrNullStrInConditionStatm){
		
		if (condition instanceof ParenthesizedExpression){
			condition= ((ParenthesizedExpression) condition).getExpression();
			searchConditionForUndefOrNullString(condition, undefOrNullStrInConditionStatm);
		}
		if (condition instanceof UnaryExpression){
			condition= ((UnaryExpression) condition).getOperand();
			searchConditionForUndefOrNullString(condition, undefOrNullStrInConditionStatm);
		}
		if (condition instanceof ElementGet){
			searchConditionForUndefOrNullString(((ElementGet) condition).getTarget(), undefOrNullStrInConditionStatm);
			searchConditionForUndefOrNullString(((ElementGet) condition).getElement(), undefOrNullStrInConditionStatm);
		}
		if (condition instanceof InfixExpression){
			
			if (((InfixExpression)condition).getLeft() instanceof InfixExpression){
				searchConditionForUndefOrNullString(((InfixExpression)condition).getLeft(),undefOrNullStrInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getLeft() instanceof ParenthesizedExpression){
				searchConditionForUndefOrNullString(((ParenthesizedExpression)((InfixExpression)condition).getLeft()).getExpression()
						, undefOrNullStrInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getLeft() instanceof UnaryExpression){
				searchConditionForUndefOrNullString(((UnaryExpression)((InfixExpression)condition).getLeft()).getOperand()
						, undefOrNullStrInConditionStatm);
			}
			
			else if (((InfixExpression)condition).getLeft() instanceof ElementGet){
				searchConditionForUndefOrNullString(((ElementGet)((InfixExpression)condition).getLeft()).getTarget()
						,undefOrNullStrInConditionStatm);
				searchConditionForUndefOrNullString(((ElementGet)((InfixExpression)condition).getLeft()).getElement()
						, undefOrNullStrInConditionStatm);
			}
		
			if (((InfixExpression)condition).getRight() instanceof InfixExpression){
				searchConditionForUndefOrNullString(((InfixExpression)condition).getRight(),undefOrNullStrInConditionStatm);
			}
		
			else if (((InfixExpression)condition).getRight() instanceof ParenthesizedExpression){
				searchConditionForUndefOrNullString(((ParenthesizedExpression)((InfixExpression)condition).getRight()).getExpression(),
						undefOrNullStrInConditionStatm);
			}	
			
			else if (((InfixExpression)condition).getRight() instanceof ElementGet){
				searchConditionForUndefOrNullString(((ElementGet)((InfixExpression)condition).getRight()).getTarget(),
						undefOrNullStrInConditionStatm);
				searchConditionForUndefOrNullString(((ElementGet)((InfixExpression)condition).getRight()).getElement(),
						undefOrNullStrInConditionStatm);
			}
			else if (((InfixExpression)condition).getRight() instanceof UnaryExpression){
				searchConditionForUndefOrNullString(((UnaryExpression)((InfixExpression)condition).getRight()).getOperand(),
						undefOrNullStrInConditionStatm);
			}
			if (((InfixExpression)condition).getLeft() instanceof StringLiteral){
				StringLiteral str=(StringLiteral) ((InfixExpression)condition).getLeft();
				if(str.getValue().equals("undefined") || str.getValue().equals("null"))
					undefOrNullStrInConditionStatm.add(str);
			}
		
			if (((InfixExpression)condition).getRight() instanceof StringLiteral){
				StringLiteral str=(StringLiteral) ((InfixExpression)condition).getRight();
				if(str.getValue().equals("undefined") || str.getValue().equals("null"))
					undefOrNullStrInConditionStatm.add(str);
			}
		}
		else if(condition instanceof StringLiteral){
			if(((StringLiteral) condition).getValue().equals("undefined") 
					|| ((StringLiteral) condition).getValue().equals("null"))
				undefOrNullStrInConditionStatm.add((StringLiteral) condition);
		}
		
		return undefOrNullStrInConditionStatm;
		
	}
	
	
	
	
	/*
	private InfixExpression getRandomInfixExpression(List<InfixExpression> infixList){
		
		return (infixList.get(SystemProps.rnd.getNextRandomInt(infixList.size())));
	}
	
	private KeywordLiteral getRandomKeywordLiteral(List<KeywordLiteral> keyLiteralList){
		
		return (keyLiteralList.get(SystemProps.rnd.getNextRandomInt(keyLiteralList.size())));
	}
	*/
	
	private void changeUnaryExpOperatorType(AstNode node){
		
		if (node instanceof UnaryExpression){
			UnaryExpression unrExp=(UnaryExpression) node;
			if (unrExp.getOperator()==Token.INC){
				unrExp.setOperator(Token.DEC);
			}
			else
				unrExp.setOperator(Token.INC);
		}
	}

	private void changeUnaryExpOperatorPosition(AstNode node){
		
		if (node instanceof UnaryExpression){
		
			UnaryExpression unrExp=(UnaryExpression) node;
			if (unrExp.getOperator()==Token.INC || unrExp.getOperator()==Token.DEC){
				if (!unrExp.isPostfix()){
					unrExp.setIsPostfix(true);
				}
				else
					unrExp.setIsPostfix(false);
			}
		}
	
	
	}
	
	
	
	///////
	
	
	
	
	/* Removing parentheses from the function calls like test() or "text()" */
	
	public boolean functionCallRemoveParen(AstNode node){
		String oldsource="",newsource="";
		if(node instanceof FunctionCall){
			String funcSource=node.toSource();
			String newfuncSource=funcSource.replace("()", "");
			AstNode parent = node;
    		    		
    		while (parent!=null   && !(parent instanceof ReturnStatement) &&! (parent instanceof ExpressionStatement)){
    			
    			parent=parent.getParent();
    			
    		}
    		
    		oldsource=parent.toSource();
    		if(parent instanceof ReturnStatement){
    			String retSource=((ReturnStatement)parent).getReturnValue().toSource();
    			AstNode newRetValue=parse(retSource.replace(funcSource, newfuncSource));
    			ExpressionStatement exp=(ExpressionStatement) newRetValue.getFirstChild();
    			((ReturnStatement)parent).setReturnValue(exp.getExpression());
    		}
    		else if (parent!=null){
    			ExpressionStatement exp=(ExpressionStatement) parent;
    			AstNode newNode=parse(exp.toSource().replace(funcSource, newfuncSource));
    			ExpressionStatement newExp=(ExpressionStatement) newNode.getFirstChild();
    			exp.setExpression(newExp.getExpression());
    			
    		}
    		newsource=parent.toSource();
    	
			
			
		}
		else if(node instanceof StringLiteral){
			oldsource=node.toSource();
			String newStr=((StringLiteral)node).getValue().replace("()", "");
			((StringLiteral) node).setValue(newStr);
			newsource=node.toSource();
			
		}
		
		if(newsource.equals(oldsource))
			return false;
		
		return true;
		
	}
	
	public boolean jsChangeGetElemByIdTag(AstNode node){
		FunctionCall funcNode=(FunctionCall) node;
		List<AstNode> args=new ArrayList<AstNode>();
		args=funcNode.getArguments();
		AstNode argNode=args.get(0);
		if(argNode instanceof StringLiteral){
			String newStr=((StringLiteral)argNode).getValue()+"_changed";
			((StringLiteral)argNode).setValue(newStr);
			return true;
		}
		return false;
		
	}
	
	public boolean jsMethodSwapArgs(AstNode node){
		FunctionCall funcNode=(FunctionCall) node;
		List<AstNode> args=new ArrayList<AstNode>();
		args=funcNode.getArguments();
		if(args.size()<2)
			return false;
		Collections.swap(args, 0, 1);
		return true;
	}
	
	public boolean jsSwapInnerHtmlInnerText(AstNode node){
		Name nameNode=(Name) node;
		String oldSource=nameNode.getIdentifier();
		if(nameNode.getIdentifier().equals("innerHTML")){
			nameNode.setIdentifier("innerText");
		}
		else if(nameNode.getIdentifier().equals("innerText")){
			nameNode.setIdentifier("innerHTML");
		}
		String newSource=nameNode.getIdentifier();
		if(newSource.equals(oldSource))
			return false;
		return true;
	}
	
	/* mutating ajax stuffs... */
	
	public boolean ajaxChangeCallbackStatus(AstNode node){
		if (node instanceof NumberLiteral){
			NumberLiteral num=(NumberLiteral)node;
			double numValue=num.getNumber();
			double oldVal=num.getNumber();
			if(0<=numValue && numValue<=4 ){
				double newVal=SystemProps.rnd.getNextRandomInt(5);
				while(newVal==numValue)
					newVal=SystemProps.rnd.getNextRandomInt(5);
				num.setNumber(newVal);
				num.setValue(String.valueOf(newVal));
			}
			else if(numValue==200){
				double newVal=404;
				num.setNumber(newVal);
				num.setValue(String.valueOf(newVal));
			}
			else if(numValue==404){
				double newVal=200;
				num.setNumber(newVal);
				num.setValue(String.valueOf(newVal));
			}
			double newVal=num.getNumber();
			if(newVal==oldVal)
				return false;
			return true;
		}
		return false;
	}
	
	public boolean ajaxChangeRequestType(AstNode node){
		FunctionCall funcNode=(FunctionCall) node;
		List<AstNode> args=new ArrayList<AstNode>();
		args=funcNode.getArguments();
		if(args.size()==0 || !(args.get(0) instanceof StringLiteral))
			return false;
		StringLiteral reqType=(StringLiteral) args.get(0);
		String oldVal=reqType.getValue();
		if(oldVal.toLowerCase().equals("get"))
			reqType.setValue("POST");
		else if(oldVal.toLowerCase().equals("post"))
			reqType.setValue("GET");
		String newVal=reqType.getValue();
		if(newVal.equals(oldVal))
			return false;
		return true;
	}
	
	/* Mutating jquery specific methods such as: .css, .addClass, .removeClass,... 
	 * Mutation type: We mutate only the first arg if they have two args, otherwise the only arg will
	 * be changed (changing the arg's name) */
	
	public boolean jqueryMethodChangeArg(AstNode node){
		
		FunctionCall funcCall=(FunctionCall) node;
		List<AstNode> args=new ArrayList<AstNode>();
		args= funcCall.getArguments();
		if(args.size()==0)
			return false;
		AstNode toMutate=args.get(0);
		if(toMutate instanceof ExpressionStatement)
			toMutate=((ExpressionStatement)toMutate).getExpression();
		if(toMutate instanceof FunctionNode){
			return functionNodeChangeName(toMutate);
		}
		else if(toMutate instanceof ObjectLiteral){
			List<ObjectProperty> elements=((ObjectLiteral)toMutate).getElements();
			if(elements!=null){
				List<StringLiteral> strList=new ArrayList<StringLiteral>();
				for(ObjectProperty op:elements){
					if(op.getLeft() instanceof StringLiteral)
						strList.add((StringLiteral) op.getLeft());
				}
				if(strList.size()==0)
					return false;
				int randIndex=SystemProps.rnd.getNextRandomInt(strList.size());
				String newStr=strList.get(randIndex).getValue()+"_changed";
				strList.get(randIndex).setValue(newStr);
			
			}
		}
		else if(toMutate instanceof StringLiteral){
			
			String newStr=((StringLiteral) toMutate).getValue()+"_changed";
			((StringLiteral) toMutate).setValue(newStr);
		}
		else return false;
		
		return true;
		
	}
	
	public boolean jqueryRemoveDollarSign(AstNode node){
		
		Name nameNode=(Name)node;
		nameNode.setIdentifier("");
		return true;
	}
	
	public boolean jqueryChangeSelectorSign(AstNode node){
		StringLiteral strnode=(StringLiteral)node;
		String oldsource=strnode.getValue();
		String strSource=strnode.getValue();
		if(!strSource.startsWith(".") && !strSource.startsWith("#"))
			return false;
		if(strSource.startsWith(".")){
			strSource=strSource.replaceFirst(".", "#");
		}
		else if(strSource.startsWith("#")){
			strSource=strSource.replaceFirst("#", ".");
		}
			
		strnode.setValue(strSource);
		String newsource=strnode.getValue();
		if(oldsource.equals(newsource))
			return false;
		return true;
			
	}
}
