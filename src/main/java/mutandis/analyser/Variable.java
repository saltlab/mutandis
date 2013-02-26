package mutandis.analyser;

import java.util.ArrayList;


public class Variable {
	
	private String varName;
	private int usage;
	private double usagePercentage;
	private ArrayList<ArrayList<String>> varInfo=new ArrayList<ArrayList<String>>();
	private double threshold=0.0;
	
	public Variable(String name, String varType, String varCategory, String statementCategory, String sourceCode){
		
		usagePercentage=0;
		this.varName=name;
		usage=0;
		addVarInfo(varType, varCategory, statementCategory, sourceCode);
	
		
	}
	
	public String getVarName(){
		return varName;
	}
	
	public void addVarInfo(String varType, String varCategory, String statementCategory, String sourceCode){
		
		ArrayList<String> varinfo=new ArrayList<String>();
		varinfo.add(varType);
		varinfo.add(varCategory);
		varinfo.add(statementCategory);
		varinfo.add(sourceCode);
		usage++;
		for (ArrayList<String> infoList:varInfo){
			if (infoList.equals(varinfo)){
				return;
			}
			
		}
		varInfo.add(varinfo);
		
	}
	public int getUsage(){
		return usage;
	}

	public ArrayList<ArrayList<String>> getVarInfo(){
		return varInfo;
	}
	
	protected double getUsagePercentage(int totalUsage){
		

		double usagePerc=(double)this.usage/totalUsage;
		return (double)(Math.round(usagePerc*1000))/1000;
	
	}
	public double getUsagePercentage(){
		return usagePercentage;
	}
	
	public void setUsagePercentage(int totalUsage){
		usagePercentage=(double)(Math.round((double)usage*1000/totalUsage))/1000;
	}
	public void setThreshold(double trshld){
		threshold=trshld;
	}
	public double getThreshold(){
		return threshold;
	}
}
