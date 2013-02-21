package analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import systemProperties.RandomGenerator;
import systemProperties.SystemProps;


public class VariableSelector {
	
	private VariableTraceAnalyser variableTraceAnalyser;
	private VariableExtractorFromInvars variableExtractorFromInvars;
	private TreeMap<String, List<Variable>> variableMap=new TreeMap<String, List<Variable>>();
	private HashMap<String,ArrayList<String>>  invarVariableMap=new HashMap<String,ArrayList<String>>();
//	private double threshold=SystemProps.threshold;
	
	
	public VariableSelector(String outputdir){
		
		variableTraceAnalyser=new VariableTraceAnalyser(outputdir);
		variableTraceAnalyser.startAnalysingTraceFiles();
		variableMap=variableTraceAnalyser.getVariableMap();
		variableExtractorFromInvars=new VariableExtractorFromInvars(outputdir);
		invarVariableMap=variableExtractorFromInvars.getFunctionVarMap();
//		this.threshold=threshold;

		
	}
	
/*	public void setThreshold(double threshold){
		this.threshold=threshold;
	}
*/	
	public String getSelectedVariable(String functionName){
		
		HashMap<String,ArrayList<String>> jointmap=getJointVarList();
		ArrayList<String> varlist=new ArrayList<String>();
		varlist=jointmap.get(functionName);
		if(varlist==null)
			return "";

		return varlist.get(SystemProps.varSelecRnd.getNextRandomInt(varlist.size()));
			
	}
	
	private HashMap<String,ArrayList<String>> getJointVarList(){
		
		removeDuplicateVarsFromTree(variableMap, invarVariableMap);
		HashMap<String,ArrayList<String>> varmapList=new HashMap<String,ArrayList<String>>();
		Set<String> keys=variableMap.keySet();
		Iterator<String> it=keys.iterator();
		while(it.hasNext()){
			String funcname=it.next();
			ArrayList<String> vars=new ArrayList<String>();
			vars=(ArrayList<String>) getVarAboveThreshold(variableMap.get(funcname));
			if(vars.size()!=0){
				varmapList.put(funcname, vars);
			}
		}
		HashMap<String,ArrayList<String>> jointList=new HashMap<String,ArrayList<String>>();
		jointList.putAll(varmapList);
		if(invarVariableMap.size()==0)
			return jointList;
		Set<String> invarkeys=invarVariableMap.keySet();
		Iterator<String> invarit=invarkeys.iterator();
		
		while(invarit.hasNext()){
			String funcname=invarit.next();
			ArrayList<String> varlist=invarVariableMap.get(funcname);
			if(jointList.containsKey(funcname)){
				ArrayList<String> list=jointList.get(funcname);
				list.addAll(varlist);
				jointList.put(funcname, list);
			}
			else{
				jointList.put(funcname, varlist);
			}
		}
		
		return jointList;
		

		
		
		
	}
	
	private void removeDuplicateVarsFromTree(TreeMap<String, List<Variable>> treemap, HashMap<String, ArrayList<String>> hashmap){
		
		if(hashmap.size()==0)
			return;
		Set<String> invarkeySet=hashmap.keySet();
		Iterator<String> invarIt=invarkeySet.iterator();
		while(invarIt.hasNext()){
			String funcname=invarIt.next();
			ArrayList<String> varlist= hashmap.get(funcname);
			HashSet<String> hashset=new HashSet<String>();
			hashset.addAll(varlist);
			varlist.clear();
			varlist.addAll(hashset);
			hashmap.put(funcname, varlist);
		}
		while(invarIt.hasNext()){
				
			String funcname=invarIt.next();
			ArrayList<Variable> treeVars=(ArrayList<Variable>) treemap.get(funcname);
			ArrayList<Variable> copy=(ArrayList<Variable>) treeVars.clone();
			ArrayList<String> hashmapVars=hashmap.get(funcname);
			if(treeVars!=null){
				for(Variable var:copy){
					for(String name:hashmapVars){
						if(var.getVarName().equals(name)){
							treeVars.remove(var);
							
						}
					}
				}
				treemap.put(funcname, treeVars);
			}
			
				
		}
		
		
	}
	private List<String> getVarAboveThreshold(List<Variable> varList){
		List<String> varAboveThreshList=new ArrayList<String>();
		for(Variable var:varList){
			if(var.getUsagePercentage()>=var.getThreshold()){
				varAboveThreshList.add(var.getVarName());
			}
		}
		return varAboveThreshList;
	}
	

}
