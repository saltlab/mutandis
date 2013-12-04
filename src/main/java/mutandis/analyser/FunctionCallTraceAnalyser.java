package mutandis.analyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import mutandis.astModifier.JSASTModifier;
import mutandis.exectionTracer.JSFuncExecutionTracer;
import mutandis.exectionTracer.JSVarExecutionTracer;

import com.crawljax.util.Helper;
import com.google.common.base.Charsets;
import com.google.common.io.Files;


public class FunctionCallTraceAnalyser {
	
	/* caller1-><callee,usagePercentage>,<...> caller2-><callee,usagePercentage>,<...> ... */
	public TreeMap<String, ArrayList<ArrayList<Object>>> functionCallMap=new TreeMap<String, ArrayList<ArrayList<Object>>>();
	private List<String> traceFilenameAndPath;
	private String 	resultFilenameAndPath;
	private String outputFolder;
	private String jsFilesFolder;
	public static List<String> NotExecutedFuncs=new ArrayList<String>();
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
	
	
	
	public FunctionCallTraceAnalyser(String outputFolder, String jsFilesFolder){
		this.outputFolder=Helper.addFolderSlashIfNeeded(outputFolder);
		resultFilenameAndPath=this.outputFolder + "functionCallTraceResult.txt";
		traceFilenameAndPath=allTraceFiles();
		this.jsFilesFolder=jsFilesFolder;
		
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
				
			
		//		String[] callerInfo=line.split("::");
		
		//		callerName=callerInfo[1];
				while (!(line = input.readLine()).equals("================================================")){
					
									
					String[] callerCallee=line.split("::");
					callerName=callerCallee[0];
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
							if(!callee.equals(callerName)){
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
						if(!callee.equals(callerName)){
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
			
			/* adding not executed functions caller-callee relations to the dynamic function-call map */
			List<String> notExecedFuncs=getNotExecutedFunctions();
			for(String notExecFunc:notExecedFuncs){
				List<String> callers=getCallersOfNotExecutedFunc(notExecFunc);
				for(String caller:callers){
					if (functionCallMap.containsKey(caller)){
						ArrayList<ArrayList<Object>> calleeList=functionCallMap.get(caller);        
								
							/*the if check is for ommiting self loops as page rank does not support self loops*/
						if(!notExecFunc.equals(caller)){
							ArrayList<Object> newCallee=new ArrayList<Object>();
							newCallee.add(0,notExecFunc);
							newCallee.add(1,1.0);
							calleeList.add(newCallee);
						}
						
						
						functionCallMap.put(caller, calleeList);
					}
					
					else{
						/*the if check is for ommiting self loops as page rank does not support self loops*/
						if(!notExecFunc.equals(caller)){
							ArrayList<Object> newCallee=new ArrayList<Object>();
							newCallee.add(0,notExecFunc);
							newCallee.add(1,1.0);
							ArrayList<ArrayList<Object>> newCalleeList=new ArrayList<ArrayList<Object>>();
							newCalleeList.add(newCallee);
							functionCallMap.put(caller, newCalleeList);
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
	
	private ArrayList<String> getJSFilesContents() {
		ArrayList<String> contentList = new ArrayList<String>();
		
		try {
			
			
			File dir = new File(jsFilesFolder);
	
			String[] files = dir.list();
			if (files == null) {
				return contentList;
			}
			for (String file : files) {
				if (file.endsWith(".js")) {
					String content = Files.toString(new File(jsFilesFolder+file), Charsets.UTF_8);
					contentList.addAll(getJSPartFromFile(content));
			//		contentList.add(content);
				}
			}

		
		}
		
		 catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }
		return contentList;
	}
	
	private ArrayList<String> getJSPartFromFile(String fileContent){
		ArrayList<String> contentList = new ArrayList<String>();
		if(fileContent.contains("<html>") || fileContent.contains("<HTML>")){
			Document dom;
			try {
				dom = Helper.getDocument(new String(fileContent));
			
			/* find script nodes in the html */
				NodeList nodes = dom.getElementsByTagName("script");
		
				for (int i = 0; i < nodes.getLength(); i++) {
					Node nType = nodes.item(i).getAttributes().getNamedItem("type");
					/* instrument if this is a JavaScript node */
					if ((nType != null && nType.getTextContent() != null && nType
					        .getTextContent().toLowerCase().contains("javascript"))) {
						String content = nodes.item(i).getTextContent();
						if (content.length() > 0) {
							contentList.add(content);
							continue;
						}
					}
		
					/* also check for the less used language="javascript" type tag */
					nType = nodes.item(i).getAttributes().getNamedItem("language");
					if ((nType != null && nType.getTextContent() != null && nType
					        .getTextContent().toLowerCase().contains("javascript"))) {
						String content = nodes.item(i).getTextContent();
						if (content.length() > 0) {
							contentList.add(content);
						}
		
					}
				}
			}
			catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			contentList.add(fileContent);
			return contentList;
		}
		return contentList;
	}
	
	private List<String> getCallersOfNotExecutedFunc(String notExecedFuncName){
		List<String> callersOfNotExecutedFunc=new ArrayList<String>();
		StaticFunctionCallGraph sFuncCallFinder=new StaticFunctionCallGraph(notExecedFuncName);
		ArrayList<String> contens=getJSFilesContents();
		for(String content:contens){
			AstNode node=parse(content);
			sFuncCallFinder.visit(node);
			callersOfNotExecutedFunc.addAll(sFuncCallFinder.getCallerOfNotExecutedFunc());
		}
		return callersOfNotExecutedFunc;
		
	}
	private List<String> getNotExecutedFunctions(){

		Set<String> executedFuncs=new HashSet<String>();
		Set<String> keys=functionCallMap.keySet();
		Iterator<String> it=keys.iterator();
		while(it.hasNext()){
			String executedFuncName=it.next();
			executedFuncs.add(executedFuncName);
		}
		
		for(String func:JSASTModifier.functionNodes){
			if(!executedFuncs.contains(func)){
				NotExecutedFuncs.add(func);
			}
		}
		
		return NotExecutedFuncs;
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
		double totalcalls=getTotalNoCalls();
		while(it.hasNext()){
			
			String callerNameAndPath=it.next();
			ArrayList<ArrayList<Object>> calleeList=functionCallMap.get(callerNameAndPath);
			
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
	
	public double getTotalNoCalls(){
		

	
		double noTotalCalls=0;
		Set<String> keySet=functionCallMap.keySet();
		Iterator<String> it=keySet.iterator();
		while(it.hasNext()){
			String callerName=it.next();
			ArrayList<ArrayList<Object>> calleeList= functionCallMap.get(callerName);
			for(ArrayList<Object> callee:calleeList){
				
				noTotalCalls+=(Double)callee.get(1);
				
				
			}
			
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
	
	private AstNode parse(String code) {
		Parser p = new Parser(compilerEnvirons, null);
		return p.parse(code, null, 0);
		
	}
	

}
