package mutator;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.StringLiteral;

import com.crawljax.core.CrawljaxController;

public class DomJsCodeLevelVisitor implements NodeVisitor{
	
	protected static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());
	
	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;


	/* forexample for variable x we have: typeOfCode -> <sourceCode>*/ 
	
	private HashMap<String,ArrayList<AstNode>> jsDomMap;
	private final ArrayList<String> jqueryList=new ArrayList<String>();
	private final ArrayList<String> jsList=new ArrayList<String>();
	private ArrayList<ArrayList<Object>> jsDomList=new ArrayList<ArrayList<Object>>();
	
	public DomJsCodeLevelVisitor(){
		
		jsDomMap=new HashMap<String,ArrayList<AstNode>>();
		jqueryList.add("addClass");
		jqueryList.add("removeClass");
		jqueryList.add("removeAttr");
		jqueryList.add("css");
		jqueryList.add("attr");
		jqueryList.add("prop");
//		jqueryList.add("append");
//		jqueryList.add("appendTo");
//		jqueryList.add("prepend");
//		jqueryList.add("prependTo");
//		jqueryList.add("insertBefore");
//		jqueryList.add("insertAfter");
		jqueryList.add("detach");
		jqueryList.add("remove");
		
		jsList.add("getElementById");
		jsList.add("getElementsByTagName");
		jsList.add("setAttribute");
		jsList.add("getAttribute");
		jsList.add("removeAttribute");

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
		
		if (node instanceof Name) {
			/* function calls like .addClass, .css, .attr ... */
			if (node.getParent() instanceof PropertyGet
					&& node.getParent().getParent() instanceof FunctionCall
					&& !node.getParent().toSource().contains("function")){
				if(jqueryList.contains(node.toSource())){
					setJsDomMap(node.getParent().getParent(), "jquery_c_arg");
				}
				else if(jsList.contains(node.toSource())){
					setJsDomMap(node.getParent().getParent(), "js_c_id_tag");
				}
				else if(node.toSource().equals("insertBefore")
						|| node.toSource().equals("replaceChild")){
	//				setJsDomMap(node.getParent().getParent(), "js_s_arg");
				}
				
			}
			else if(node.getParent() instanceof PropertyGet){
				if(node.toSource().equals("innerHTML")
						|| node.toSource().equals("innerText")){
					setJsDomMap(node, "js_innerHTML_innerText");
				}
			}
		}
		else if(node instanceof FunctionCall){
			if( ((FunctionCall)node).getTarget() instanceof Name){
			
				if(((Name)((FunctionCall)node).getTarget()).getIdentifier().equals("$")){
	//				setJsDomMap(((Name)((FunctionCall)node).getTarget()), "jquery_r_dollar");
				
					if(((FunctionCall)node).getArguments().size()==1
							&& ((FunctionCall)node).getArguments().get(0) instanceof StringLiteral
							&& ((FunctionCall)node).getArguments().get(0).toSource().startsWith(".")
									|| ((FunctionCall)node).getArguments().get(0).toSource().startsWith("#") ){
						setJsDomMap(((FunctionCall)node).getArguments().get(0), "jquery_c_selSign");
					}
				
				}
			}
		}
		
		
		return true;
	}
	
	private void setJsDomMap(AstNode node,String codeType){
			
		ArrayList<AstNode> list=new ArrayList<AstNode>();
		if(jsDomMap.get(codeType)!=null){
				
			list=jsDomMap.get(codeType);
								
		}				
		list.add(node);
		jsDomMap.put(codeType, list);					
		
	}
	
	public  HashMap<String,ArrayList<AstNode>> getJsDomList(){
		return jsDomMap;
		
	}
	public void setJsDomList(){
		if(jsDomList.size()>0 || jsDomMap.size()==0)
			return;
		Set<String> keys=jsDomMap.keySet();
		Iterator<String> it=keys.iterator();
		while(it.hasNext()){
			String codeType=it.next();
			ArrayList<AstNode> nodes=jsDomMap.get(codeType);
			for(AstNode node:nodes){
				ArrayList<Object> list=new ArrayList<Object>();
				list.add(codeType);
				list.add(node);
				jsDomList.add(list);
				
			}
		}
	}
		
	public ArrayList<Object> getElementfromJsDomList(int index){
		if(jsDomList.size()==0 || index>=jsDomList.size()) return null;
		return jsDomList.get(index);
	}

}

