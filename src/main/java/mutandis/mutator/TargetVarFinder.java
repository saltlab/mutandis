package mutandis.mutator;

import java.util.ArrayList;
import java.util.List;

import mutandis.systemProperties.SystemProps;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.NodeVisitor;


public class TargetVarFinder implements NodeVisitor{
	private AstNode targetVar;
	private AstNode rootNode;
	private List<AstNode> targetVarList=new ArrayList<AstNode>();
	
	public TargetVarFinder(AstNode targetVar){
		this.targetVar=targetVar;
	}
	public void setRootNode(AstNode root){
		rootNode=root;
	}
	
	public boolean visit(AstNode node) {
		if(node.toSource().equals(targetVar.toSource())){
			
			targetVarList.add(rootNode);
			
		}
		return true;
	}
	
	public AstNode getRandomTarget(){
		if(targetVarList.size()==0)
			return null;
		return(targetVarList.get(SystemProps.rnd.getNextRandomInt(targetVarList.size())));
	}

}
