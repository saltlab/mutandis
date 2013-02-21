package analyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.crawljax.util.Helper;

import exectionTracer.JSFuncExecutionTracer;
import exectionTracer.JSVarExecutionTracer;

public class FunctionCallTraceAnalyser {
	
	/* caller1-><callee,usagePercentage>,<...> caller2-><callee,usagePercentage>,<...> ... */
	public TreeMap<String, ArrayList<ArrayList<Object>>> functionCallMap=new TreeMap<String, ArrayList<ArrayList<Object>>>();
	private List<String> traceFilenameAndPath;
	private String 	resultFilenameAndPath;
	private String outputFolder;
	
	public FunctionCallTraceAnalyser(String outputFolder){
		this.outputFolder=Helper.addFolderSlashIfNeeded(outputFolder);
		resultFilenameAndPath=this.outputFolder + "functionCallTraceResult.txt";
		traceFilenameAndPath=allTraceFiles();
		
	}
	
	
	public void startAnalysingTraceFiles(){
		try{
			List<String>filenameAndPathList=getTraceFilenameAndPath();
			for (String filenameAndPath:filenameAndPathList){
				BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
				
				String line="", callerName="";
			  while ((line = input.readLine()) != null){
			
				if ("".equals(line))
					break;
				
			
				String[] callerInfo=line.split("::");
		//		callerName=callerInfo[0]+"::"+callerInfo[1];
				callerName=callerInfo[1];
				while (!(line = input.readLine()).equals("================================================")){
					
									
					String[] callerCallee=line.split("::");
			
					String callee=callerCallee[1];
					
					if (functionCallMap.containsKey(callerName)){
						ArrayList<ArrayList<Object>> calleeList=functionCallMap.get(callerName);        
				
						boolean bool=false;
						for (int i=0;i<calleeList.size();i++){
							if (callee.equals(calleeList.get(i).get(0))){
								bool=true;
								double usage= (Double)calleeList.get(i).get(1)+1;
								calleeList.get(i).set(1, usage);
								break;
							}
						}
				
						if (!bool){
							/*the if check is for ommiting self loops as page rank does not support self loops*/
							if(!(callerInfo[0]+"::"+callee).equals(callerName)){
								ArrayList<Object> newCallee=new ArrayList<Object>();
								newCallee.add(0,callee);
								newCallee.add(1,1.0);
								calleeList.add(newCallee);
							}
						}
						
						functionCallMap.put(callerName, calleeList);
					}
					
					else{
						/*the if check is for ommiting self loops as page rank does not support self loops*/
						if(!(callerInfo[0]+"::"+callee).equals(callerName)){
							ArrayList<Object> newCallee=new ArrayList<Object>();
							newCallee.add(0,callee);
							newCallee.add(1,1.0);
							ArrayList<ArrayList<Object>> newCalleeList=new ArrayList<ArrayList<Object>>();
							newCalleeList.add(newCallee);
							functionCallMap.put(callerName, newCalleeList);
						}
					}
				}
			  }
					
			}		
							
			writingResultsintoFile();
					
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public TreeMap<String, ArrayList<ArrayList<Object>>> getFunctionCallMap(){
		return functionCallMap;
	}
	
	public List<String> getTraceFilenameAndPath() {
		return traceFilenameAndPath;
	}
	
	private void writingResultsintoFile(){
		
		try{
			PrintWriter file = new PrintWriter(resultFilenameAndPath);
			file.write(getResult());
			file.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getResult(){
	
		StringBuffer result = new StringBuffer();
		
		Set <String> keySet=functionCallMap.keySet();
		Iterator<String> it=keySet.iterator();
		while(it.hasNext()){
			
			String callerNameAndPath=it.next();
			ArrayList<ArrayList<Object>> calleeList=functionCallMap.get(callerNameAndPath);
			double totalcalls=getTotalNoCalls(callerNameAndPath);
			for(ArrayList<Object> callee:calleeList){
				
				
				double usagePerc=(Double)callee.get(1)/totalcalls;
				callee.set(1, usagePerc);	
				double roundedUsagePerc=(double)(Math.round(usagePerc*1000))/1000;
				
				result.append(callerNameAndPath + "::" + callee.get(0)
						+ "::" + roundedUsagePerc+"\n");
				result.append("================================================" + "\n");
									
			}
			
			
				
		}
		
		
			
		
		return result.toString();		
		
	}
	
	public double getTotalNoCalls(String callerName){
		

	
		double noTotalCalls=0;
		
		ArrayList<ArrayList<Object>> calleeList= functionCallMap.get(callerName);
		for(ArrayList<Object> callee:calleeList){
				
				noTotalCalls+=(Double)callee.get(1);
				
				
			}
		
		return noTotalCalls;
	}
	
	private List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(outputFolder +  JSFuncExecutionTracer.EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(outputFolder + JSFuncExecutionTracer.EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}
	

}
