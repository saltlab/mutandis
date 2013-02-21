package mutator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.apache.log4j.Logger;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WhileLoop;
import com.crawljax.core.CrawljaxController;

public class JSSpecificVisitor implements NodeVisitor{
	
	protected static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());
	
	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;
	

	/* forexample for variable x we have: typeOfCode -> <sourceCode> i.e: 
	 * IfStatement -> <[if(...)...],[if(...)...],...> */
	private HashMap<String,ArrayList<AstNode>> jsSpecificMap;
	private List<String> ajax=new ArrayList<String>();
	private ArrayList<ArrayList<Object>> jsSpecList=new ArrayList<ArrayList<Object>>();
	private ArrayList<String> jsSpecNotToChange=new ArrayList<String>();
	public JSSpecificVisitor(){
		
		
		jsSpecificMap=new HashMap<String,ArrayList<AstNode>>();
		ajax.add("readyState");
		ajax.add("status");
		
		jsSpecNotToChange.add("Array");
		jsSpecNotToChange.add("Math.random");
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
	
	

	
	@Override
	public boolean visit(AstNode node) {	
		
		if(node instanceof FunctionCall){
			String source=node.toSource();
			if(source.contains(".replace"))
				setJsSpecificMap(node, "/g_replace");
			else if(source.contains("parseInt"))
				setJsSpecificMap(node, "r_parseInt");
			else if(source.contains("setTimeout"))
				setJsSpecificMap(node, "setTimeout");
			else if(source.contains("setInterval"))
				setJsSpecificMap(node, "setInterval");
/*			else if(((FunctionCall)node).getArguments().size()==0){
				String src=((FunctionCall)node).getTarget().toSource();
				if(!jsSpecNotToChange.contains(src))
					setJsSpecificMap(node, "funcCallRemoveParen");
			}
*/			

			
		
		}
		if (node instanceof Name) {
			
			if (node.getParent() instanceof PropertyGet
					&& node.getParent().getParent() instanceof FunctionCall
					&& !node.getParent().toSource().contains("function")){
				
				if(((Name)node).getIdentifier().equals("open")){
					setJsSpecificMap(node.getParent().getParent(), "ajax_c_reqType");
				}
			}
			else if( node.getParent() instanceof PropertyGet 
					&& node.getParent().getParent() instanceof InfixExpression){
				if(ajax.contains(((Name)node).getIdentifier())){
					 InfixExpression infix=(InfixExpression) node.getParent().getParent();
					 setJsSpecificMap(infix.getRight(), "ajax_callback_status");
				}
				
			}
		}
		else if(node instanceof StringLiteral){
			if(((StringLiteral) node).getValue().contains("()")){
				setJsSpecificMap(node, "funcCallRemoveParen");
			}
		}
		else if(node instanceof PropertyGet){
			if(node.toSource().startsWith("this."))
				setJsSpecificMap(node, "r_this");
		}
		else if(node instanceof Assignment 
				&& ((Assignment) node).getLeft() instanceof Name
				&& ((Assignment) node).getOperator()==Token.ASSIGN){
			if (node.getParent() instanceof ExpressionStatement)
				setJsSpecificMap(node.getParent(), "add_var");
			else
				setJsSpecificMap(node, "add_var");
		}
		else if(node instanceof VariableDeclaration){
			if (node.getParent() instanceof ExpressionStatement)
				setJsSpecificMap(node.getParent(), "r_var");
			else
				setJsSpecificMap(node, "r_var");
		}
		else if(node instanceof IfStatement){
			String source=((IfStatement)node).getCondition().toSource();
			if(source.contains("null") || source.contains("undefined"))
				setJsSpecificMap(((IfStatement)node).getCondition(), "null_undefined");
			else if(source.contains("!==false") || source.contains("!== false"))
				setJsSpecificMap(((IfStatement)node).getCondition(), "x!==false_x");
			else if(source.contains("===true") || source.contains("=== true"))
				setJsSpecificMap(((IfStatement)node).getCondition(), "x===true_x");
			else if(source.contains("===false") || source.contains("=== false"))
				setJsSpecificMap(((IfStatement)node).getCondition(), "x===false_!x");
			else if(source.contains("!==true") || source.contains("!== true"))
				setJsSpecificMap(((IfStatement)node).getCondition(), "x!==true_!x");
		}
		else if(node instanceof ForLoop){
			String source=((ForLoop)node).getCondition().toSource();
			if(source.contains("null") || source.contains("undefined"))
				setJsSpecificMap(((ForLoop)node).getCondition(), "null_undefined");
			else if(source.contains("!==false") || source.contains("!== false"))
				setJsSpecificMap(((ForLoop)node).getCondition(), "x!==false_x");
			else if(source.contains("===true") || source.contains("=== true"))
				setJsSpecificMap(((ForLoop)node).getCondition(), "x===true_x");
			else if(source.contains("===false") || source.contains("=== false"))
				setJsSpecificMap(((ForLoop)node).getCondition(), "x===false_!x");
			else if(source.contains("!==true") || source.contains("!== true"))
				setJsSpecificMap(((ForLoop)node).getCondition(), "x!==true_!x");
		}
		else if(node instanceof WhileLoop){
			String source=((WhileLoop)node).getCondition().toSource();
			if(source.contains("null") || source.contains("undefined"))
				setJsSpecificMap(((WhileLoop)node).getCondition(), "null_undefined");
			else if(source.contains("!==false") || source.contains("!== false"))
				setJsSpecificMap(((WhileLoop)node).getCondition(), "x!==false_x");
			else if(source.contains("===true") || source.contains("=== true"))
				setJsSpecificMap(((WhileLoop)node).getCondition(), "x===true_x");
			else if(source.contains("===false") || source.contains("=== false"))
				setJsSpecificMap(((WhileLoop)node).getCondition(), "x===false_!x");
			else if(source.contains("!==true") || source.contains("!== true"))
				setJsSpecificMap(((WhileLoop)node).getCondition(), "x!==true_!x");
		}

			
			
		return true;
	}
	
	private void setJsSpecificMap(AstNode node,String codeType){
			
	
		ArrayList<AstNode> list=new ArrayList<AstNode>();
		if(jsSpecificMap.get(codeType)!=null){
				
			list=jsSpecificMap.get(codeType);
								
		}				
		list.add(node);
		jsSpecificMap.put(codeType, list);					
		
	}
	
	public HashMap<String,ArrayList<AstNode>> getJsSpecificMap(){
		
		return jsSpecificMap;
		
	}
		
	public void setJsSpecList(){
		if(jsSpecList.size()>0 || jsSpecificMap.size()==0)
			return;
		Set<String> keys=jsSpecificMap.keySet();
		Iterator<String> it=keys.iterator();
		while(it.hasNext()){
			String codeType=it.next();
			ArrayList<AstNode> nodes=jsSpecificMap.get(codeType);
			for(AstNode node:nodes){
				ArrayList<Object> list=new ArrayList<Object>();
				list.add(codeType);
				list.add(node);
				jsSpecList.add(list);
				
			}
		}
	}
		
	public ArrayList<Object> getElementfromJsSpecList(int index){
		if(jsSpecList.size()==0 || index>=jsSpecList.size()) return null;
		return jsSpecList.get(index);
	}

	
}

